import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningReduce
import java.io.File
import java.util.*
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextInt

const val numberOfFiles = 10
const val numberOfPairs = 1000
const val invalidLineProbability = 0.005

data class PairsFile(val name: String, val content: String)

suspend fun main() {
    generateFiles(numberOfFiles, numberOfPairs).forEach { file ->
        File("./exercises/data/${file.name}.csv").appendText(file.content)
    }
}

private fun randomPair(): String {
    val id = nextInt(1000000)
    val pair = listOf(ValidReading(id), ValidReading(id)).map {
        "${it.id};${if (nextDouble() > invalidLineProbability) it.value else "invalid_value"}\n"
    }
    return pair.first() + pair.last()
}

private suspend fun generateFiles(numberOfFiles: Int, numberOfPairs: Int) =
    (1..numberOfFiles).map { generatePairs(UUID.randomUUID().toString(), numberOfPairs) }

private suspend fun generatePairs(filename: String, numberOfPairs: Int) = (1..numberOfPairs)
    .asFlow()
    .map {
        randomPair()
    }
    .runningReduce { acc, value -> acc + value }
    .last()
    .let {
        PairsFile(filename, it)
    }
