import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger

object ConcurrentQuickSort {
    const val BLOCK = 10_000
    private val rand = ThreadLocalRandom.current()

    suspend fun IntArray.sort(l: Int = 0, r: Int = size): IntArray {
        if (r - l <= BLOCK) {
            return sortedArray()
        }

        val x = this[rand.nextInt(l, r)]

        return coroutineScope {
            val left = async {
                parallelFilter(l, r) { it < x }.sort()
            }
            val middle = async {
                parallelFilter(l, r) { it == x }.sort()
            }
            val right = async {
                parallelFilter(l, r) { it > x }.sort()
            }

            val leftArray = left.await()
            val middleArray = middle.await()
            val rightArray = right.await()

            val res = leftArray.copyOf(leftArray.size + middleArray.size + rightArray.size)
//            System.arraycopy(leftArray, 0, this@sort, 0, leftArray.size)
            System.arraycopy(middleArray, 0, res, leftArray.size, middleArray.size)
            System.arraycopy(rightArray, 0, res, leftArray.size + middleArray.size, rightArray.size)

            return@coroutineScope res
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
//        this as MutableList<Int>
        val destination = IntArray(r - l)
        parallelFor(l, r) { i ->
            destination[i - l] = transform(this[i])
        }

        return destination

//        return destination
    }

    val myFor: DeepRecursiveFunction<Triple<Int, Int, (Int) -> Unit>, Unit>
        get() = DeepRecursiveFunction { (l, r, action) ->
            if (r - l <= BLOCK) {
                for (i in l until r) {
                    action(i)
                }
                return@DeepRecursiveFunction
            }

            val m = (l + r) / 2

            callRecursive(Triple(l, m, action))
            callRecursive(Triple(m, r, action))
        }

    suspend inline fun IntArray.parallelFor(l: Int = 0, r: Int = size, crossinline action: (Int) -> Unit) {
        coroutineScope {
            val q = ConcurrentLinkedQueue<Pair<Int, Int>>()
            val activeTasks = AtomicInteger(1)

            q.add(l to r)

            while (activeTasks.get() > 0) {
                launch {
                    val (left, right) = q.poll() ?: return@launch

                    if (right - left <= BLOCK) {
                        for (i in left until right) {
                            action(i)
                        }
                        activeTasks.decrementAndGet()
                        return@launch
                    }

                    val m = (left + right) / 2

                    q.add(left to m)
                    q.add(m to right)

                    activeTasks.incrementAndGet()
                }
            }
        }


//        coroutineScope {
//            for (i in l until r step BLOCK) {
//                launch {
//                    for (j in i until minOf(r, i + BLOCK)) {
//                        action(j)
//                    }
//                }
//            }
//        }
//        coroutineScope {
//        (l until r step BLOCK).map { i ->
//            GlobalScope.async {
//                for (j in i until minOf(r, i + BLOCK)) {
//                    action(j)
//                }
//            }
//        }.awaitAll()
//            for (i in l until r step BLOCK) {
//
//            }
//        }
    }

//    suspend fun List<Int>.parallelFor(l: Int = 0, r: Int = size, action: suspend (Int) -> Unit) {
//        if (r - l <= BLOCK) {
//            for (i in l until r) {
//                action(i)
//            }
//            return
//        }
//
//        val m = (l + r) / 2
//
//        coroutineScope {
//            launch {
//                parallelFor(l, m, action)
//            }
//            launch {
//                parallelFor(m, r, action)
//            }
//        }
//    }

    infix fun Int.divideRoundUp(divided: Int) = (this + divided - 1) / divided
}