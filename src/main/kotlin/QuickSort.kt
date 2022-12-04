import kotlin.random.Random

object QuickSort {
    fun sort(a: IntArray, l: Int = 0, r: Int = a.size - 1): IntArray {
        val destination = a.clone()
        sortInline(destination)

        return destination
    }

    fun sortInline(a: IntArray, l: Int = 0, r: Int = a.size - 1) {
        if (l < r) {
            val q = partition(a, l, r)
            sortInline(a, l, q)
            sortInline(a, q + 1, r)
        }
    }

    fun partition(a: IntArray, l: Int, r: Int): Int {
        val x = a[Random.nextInt(l, r + 1)]
        var i = l
        var j = r

        do {
            while (a[i] < x) {
                i++
            }
            while (a[j] > x) {
                j--
            }
            if (i <= j) {
                val temp = a[i]
                a[i] = a[j]
                a[j] = temp
                i++
                j--
            }
        } while (i <= j)

        return j
    }
}