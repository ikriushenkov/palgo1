import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ThreadLocalRandom

object ConcurrentQuickSort : Sort {
    const val BLOCK = 1_000
    private val rand = ThreadLocalRandom.current()

    override fun IntArray.sort(l: Int, r: Int) = runBlocking {
        val job = GlobalScope.launch {
            sortSuspend(l, r)
        }
        job.join()
    }

    suspend fun IntArray.sortSuspend(l: Int, r: Int) {
        if (r - l <= BLOCK) {
            with(SeqQuickSort) {
                sort(l, r)
            }
            return
        }

        val m = partition(l, r)

        coroutineScope {
            launch {
                sortSuspend(l, m)
            }
            launch {
                sortSuspend(m + 1, r)
            }
        }
    }

    suspend fun IntArray.sortWithFilter(l: Int = 0, r: Int = size) {
        if (r - l <= BLOCK) {
            with(SeqQuickSort) {
                sort(l, r - 1)
            }
            return
        }

        val x = this[rand.nextInt(l, r)]

        coroutineScope {
            val left = async {
                parallelFilter(l, r) { it < x }.apply { sortWithFilter() }
            }
            val middle = async {
                parallelFilter(l, r) { it == x }.apply { sortWithFilter() }
            }
            val right = async {
                parallelFilter(l, r) { it > x }.apply { sortWithFilter() }
            }

            val leftArray = left.await()
            val middleArray = middle.await()
            val rightArray = right.await()

            System.arraycopy(leftArray, 0, this@sortWithFilter, 0, leftArray.size)
            System.arraycopy(middleArray, 0, this@sortWithFilter, leftArray.size, middleArray.size)
            System.arraycopy(rightArray, 0, this@sortWithFilter, leftArray.size + middleArray.size, rightArray.size)
        }
    }

    suspend inline fun IntArray.parallelFilter(
        l: Int = 0,
        r: Int = size,
        crossinline predicate: (Int) -> Boolean,
    ): IntArray {
        val flags = parallelMap(l, r) { if (predicate(it)) 1 else 0 }
        val sums = flags.parallelScan()
        val destination = IntArray(sums.last())

        parallelFor(l, r) { i ->
            if (flags[i] == 1) {
                destination[sums[i]] = this[i]
            }
        }

        return destination
    }

    suspend fun IntArray.parallelScan(): IntArray {
        val n = size
        if (size <= BLOCK) {
            return scan(0, Int::plus).toIntArray()
        }

        val blocks = n divideRoundUp BLOCK

        var sums = IntArray(blocks)

        sums.parallelFor { i ->
            var s = 0
            for (j in BLOCK * i until minOf(n, BLOCK * (i + 1))) {
                s += this[j]
            }
            sums[i] = s
        }

        sums = sums.parallelScan()

        val s = IntArray(n + 1)

        blockedFor { i, j ->
            val prev = if (j % BLOCK == 0) {
                if (i == 0) {
                    0
                } else {
                    sums[i]
                }
            } else {
                s[j]
            }
            s[j + 1] = prev + this[j]
        }

        return s
    }

    suspend inline fun IntArray.blockedFor(crossinline action: (Int, Int) -> Unit) {
        val blocks = size divideRoundUp BLOCK

        IntArray(blocks) { it }.parallelFor { i ->
            for (j in (BLOCK * i) until minOf(size, (BLOCK * (i + 1)))) {
                action(i, j)
            }
        }
    }

    suspend inline fun IntArray.parallelMap(
        l: Int = 0,
        r: Int = size,
        crossinline transform: (Int) -> Int,
    ): IntArray {
        val destination = IntArray(r - l)
        parallelFor(l, r) { i ->
            destination[i - l] = transform(this[i])
        }

        return destination
    }

    suspend fun IntArray.parallelFor(l: Int = 0, r: Int = size, action: (Int) -> Unit) {
        if (r - l <= BLOCK) {
            for (i in l until r) {
                action(i)
            }
            return
        }

        val m = (l + r) / 2

        coroutineScope {
            launch {
                parallelFor(l, m, action)
            }
            launch {
                parallelFor(m, r, action)
            }
        }
    }

    infix fun Int.divideRoundUp(divided: Int) = (this + divided - 1) / divided
}