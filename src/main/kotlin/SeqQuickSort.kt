object SeqQuickSort : Sort {
    override fun IntArray.sort(l: Int, r: Int) {
        if (l < r) {
            val q = partition(l, r)
            sort(l, q)
            sort(q + 1, r)
        }
    }
}