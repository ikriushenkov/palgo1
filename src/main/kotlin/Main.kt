import kotlin.random.Random

fun main() {
    benchmark(5, ::generateList, getSort(SeqQuickSort), getSort(ConcurrentQuickSort))
}

fun getSort(sortType: Sort): (IntArray) -> Unit = with(sortType) { { it.sort() } }

fun generateList(size: Int = 1e8.toInt()) = IntArray(size) { Random.nextInt() }