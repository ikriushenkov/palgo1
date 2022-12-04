import kotlin.random.Random

interface Sort {
    fun IntArray.sort(l: Int = 0, r: Int = size - 1)

    fun IntArray.partition(l: Int, r: Int): Int {
        val x = this[Random.nextInt(l, r + 1)]
        var i = l
        var j = r

        do {
            while (this[i] < x) {
                i++
            }
            while (this[j] > x) {
                j--
            }
            if (i <= j) {
                val temp = this[i]
                this[i] = this[j]
                this[j] = temp
                i++
                j--
            }
        } while (i <= j)

        return j
    }
}