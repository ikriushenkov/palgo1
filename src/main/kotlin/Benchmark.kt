import kotlin.system.measureTimeMillis

fun <T> benchmark(retry: Int = 5, generateInput: () -> T, vararg actions: (T) -> Unit) {
    val inputs = List(retry) { generateInput() }

    val times = actions.mapIndexed { index, action ->
        List(retry) { tryIndex ->
            measureTimeMillis {
                action(inputs[index])
            }.also { println("function: $index\ttry: $tryIndex\ttime: $it") }
        }.average()
    }

    println("------------------------------")

    times.forEachIndexed { index, time ->
        println("function: $index\tavg time: $time")
    }

    println("------------------------------")

    val places = times.withIndex().sortedBy { it.value }

    places.forEachIndexed { place, (index, time) ->
        println("${place + 1}. function: $index\t diff: ${time / places.first().value}")
    }
}