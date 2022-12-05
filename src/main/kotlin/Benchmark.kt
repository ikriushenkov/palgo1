import kotlin.system.measureTimeMillis

fun benchmark(retry: Int = 5, generateInput: () -> IntArray, vararg actions: (IntArray) -> Unit) {
    println("Start benchmark...")
    println("Start generate input...")

    val inputs = Array(retry) { generateInput() }.map { arr -> Array(actions.size) { arr.clone() } }

    println("Finish generate input...")
    println("------------------------------")

    val times = actions.mapIndexed { index, action ->
        List(retry) { tryIndex ->
            measureTimeMillis {
                action(inputs[tryIndex][index])
            }.also { println("function: ${index + 1}\ttry: ${tryIndex + 1}\ttime: ${it.toDouble() / 1000}s") }
        }.average()
    }

    println("------------------------------")

    times.forEachIndexed { index, time ->
        println("function: $index\tavg time: ${time / 1000}s")
    }

    println("------------------------------")

    val places = times.withIndex().sortedBy { it.value }

    places.forEachIndexed { place, (index, time) ->
        println("${place + 1}. function: ${index + 1}\t diff: ${time / places.first().value}")
    }

    println("------------------------------")
    println("Finish benchmark...")
}