import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

val flow = flowOf(1,2,3,4)

suspend fun main() {
    flowOf("A","B","C","D","E")
        .onEach { println(" emits: $it") }
        .collect {
            println(" collects: $it")
            delay(1000)
        }

}
