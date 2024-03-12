import arrow.fx.coroutines.parMapUnordered
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalTime
import java.util.zip.GZIPInputStream

data class Config(
    val importPath: String,
    val outputFilename: String,
    val linesToSkip: Int,
    val concurrentFiles: Int,
    val concurrentWrites: Int,// NOT IMPLEMENTED
    val nonIOParallelism: Int,// NOT IMPLEMENTED
) {
    fun importPathAsPath(): Path = Paths.get(importPath) ?: Path.of("./exercises/data")
}

suspend fun main() {
    val config = Config(
        importPath = "./exercises/data",
        outputFilename = "./exercises/data/output-1A.txt",
        linesToSkip = 0,
        concurrentFiles = 10,
        concurrentWrites = 20,
        nonIOParallelism = 42,
    )
    File(config.outputFilename).delete()
    println("Starting import of files from ${config.importPathAsPath()}")

    val readingsFlow = importFromFiles(config)
    // collectIndexed will consume all emitted values, we don't actually need the index
    readingsFlow.collectIndexed { _, value ->
        // Delay to simulate a slow consumer and demonstrate the back-pressure mechanism
        delay(1000)
        // Additional code for processing the collected values
        File(config.outputFilename).appendText("${LocalTime.now()} | $value\n")
    }
    println("Count: ${readingsFlow.count()}")
    println("Done.")
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
fun importFromFiles(config: Config): Flow<ValidReading> = listFiles(config.importPathAsPath())
    .filter { it.isFile && it.name.endsWith(".csv.gz") }

    // { file -> file.parse(config.linesToSkip)
    //              .computeAverage()
    //             }
    .map { _ -> ValidReading(1, 2.0) } //dummy to make compile
    // .flattenConcat()
    .onEach { println("*new value*") }

// To observe the back-pressure mechanism; if the consumer doesn't consume then no new values will be emitted

inline fun <T> File.useGzipLines(block: (Sequence<String>) -> T): T =
    BufferedReader(InputStreamReader(GZIPInputStream(FileInputStream(this))))
        .use { block(it.lineSequence()) }

fun File.parse(linesToSkip: Int): Flow<Reading> = flow {
    useGzipLines { lines ->
        lines
            .drop(linesToSkip)
            .forEach { emit(parseLine(it)) }
    }
}

fun listFiles(importPath: Path?) = flow {
    val files: Array<File> = importPath?.toFile()?.listFiles() ?: arrayOf()
    files.forEach { emit(it) }
}

fun parseLine(line: String): Reading {
    val fields = line.split(";")
    val id = fields[0].toInt()
    return try {
        val value = fields[1].toDouble()
        ValidReading(id, value)
    } catch (ex: NumberFormatException) {
//        println("Unable to parse line $line: ${ex.message ?: ""}")
        InvalidReading(id)
    }
}

fun Flow<List<Reading>>.computeAverage(): Flow<ValidReading> = transform { readings ->
    require(readings.size == 2) { "Readings size must be 2, current size: ${readings.size}" }
    val validReadings = readings.filterIsInstance<ValidReading>()
    val average = if (validReadings.isNotEmpty()) validReadings.sumOf { it.value } / validReadings.size else -1.0
    emit(ValidReading(readings.first().id, average))
}
