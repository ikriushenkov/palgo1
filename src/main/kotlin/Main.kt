import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

suspend fun main() {
    val a = generateList()
//    val b = a.toIntArray()
    val c = a.clone()
    var a1: IntArray?
    var c1: IntArray? = null


    with(QuickSort) {
        val res = measureTimeMillis {
            sort(a)
        }

        println(res)
    }

    with(ConcurrentQuickSort) {
        val t = GlobalScope.launch {
//        val a = listOf(1).scan(0, Int::plus).let(::println)
//        val t = listOf(1, 1, 0, 0, 0, 0)
//
//        t.parallelScan().let(::println)

            val time = measureTimeMillis {
                c.sort()
//                a.parallelMap { it % 2 }
//            myFor(Triple(0, a.size) { it % 2 })
//            a.parallelMap { it % 2 }
            }

//        println(res)
//
            println(time)
        }

        t.join()
    }

//    println(a1.contentEquals(c1))
}

//fun main() = runBlocking {
//    with(ConcurrentQuickSort) {
//        val a = List(100_000) { it }
//
////        a.parallelFor {
////            delay(1_000)
////            println(it)
////        }
//
//        val b = MutableList<Int>(0) { TODO() }
//
//        a.map {
//            launch {
//                coroutineScope {
//                    delay(1_000)
//                    println(it)
//                }
//            }
//        }
//
//        println(b)
//    }
//}

fun generateList(size: Int = 1e6.toInt()) = IntArray(size) { Random.nextInt() }