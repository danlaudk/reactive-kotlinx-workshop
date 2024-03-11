package dayTen

import arrow.core.Either
import arrow.fx.coroutines.parMap
import arrow.optics.optics
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.fold
import kotlin.io.path.Path
import arrow.core.raise.Raise
import kotlinx.coroutines.flow.map

const val debug = false

sealed class Operations
data object Noop : Operations()
data class AddX(val value: Int) : Operations()


sealed class LogicalError
data class ParseError(val message: String) : LogicalError()
data object InvalidInputError : LogicalError()


val noopParser = parser {
    string("noop")
    Noop
}

val addXParser = parser {
    string("addx")
    whitespace()
    val value = integer()
    AddX(value.toInt())
}

val operationsParser: Parser<Operations> = parser {
    oneOf(noopParser, addXParser)
}

fun noop(executionState: ExecutionState): ExecutionState {
    val result = tick(executionState)
    return pixel(result)
}

fun addX(executionState: ExecutionState, addX: AddX): ExecutionState {
    val firstTick = pixel(tick(executionState))
    val secondTick = pixel(tick(firstTick))

    return ExecutionState.value.modify(secondTick) { it + addX.value }
}

@optics
data class ExecutionState(
    val ticks: Int = 0,
    val value: Int = 1,
    val crtBuffer: List<Char> = emptyList(),
    val screen: List<List<Char>> = emptyList()
) {
    companion object
}


fun tick(executionState: ExecutionState): ExecutionState =
    ExecutionState.ticks.modify(executionState) { it + 1 }


fun pixel(executionState: ExecutionState): ExecutionState {
    // If our current horizontal position overlaps with the register value:
    // write out '@', else '.'
    val horizontalPosition = (executionState.ticks - 1) % 40
    val sprite = listOf(executionState.value - 1, executionState.value, executionState.value + 1)
    val withBufferUpdate = pixelTheSprite(horizontalPosition, sprite, executionState)

    if (debug) {
        println("before: $executionState after: $withBufferUpdate")
    }

    // If we have a finished the line/crtBuffer, write to screen. else nothing
    return writeLineToScreenBuffer(withBufferUpdate)

}

// If our current horizontal position overlaps with the register value, pixelthesprite
fun pixelTheSprite(horizontalPosition: Int, sprite: List<Int>, executionState: ExecutionState): ExecutionState {
    return if (horizontalPosition in sprite) {
        ExecutionState.crtBuffer.modify(executionState) { it + listOf('@') } // wrap in function
    } else {
        ExecutionState.crtBuffer.modify(executionState) { it + listOf(' ') }
    }
}

fun writeLineToScreenBuffer(executionState: ExecutionState): ExecutionState {
    return if (executionState.crtBuffer.size == 40) {
        val withScreenWrite = ExecutionState.screen.modify(executionState) { it.appendList(executionState.crtBuffer) }
        ExecutionState.crtBuffer.modify(withScreenWrite) { emptyList() }
    } else {
        executionState
    }
}

fun <T> List<List<T>>.appendList(l: List<T>): List<List<T>> = this + listOf(l)

fun displayScreen(input: List<List<Char>>) {
    val columns = 40
    val rows = 6
    (0 until rows).forEach { row ->
        (0 until columns).forEach { column ->
            print(input[row][column])
        }
        println()
    }
}

fun parseCustomError(input: String): Either<ParseError, Operations> {
    return when (val r = operationsParser.parse(input)) {
        is ParserResult.Ok -> Either.Right(r.result)
        is ParserResult.Error -> Either.Left(ParseError("Unable to parse. $r "))
    }
}


suspend fun dayTen() {
    val path = Path("inputFiles/dayTen.txt")
    lines(path)
        .map {l ->
            when (val r = operationsParser.parse(l)) {
                is ParserResult.Ok -> r.result
                is ParserResult.Error -> throw Error("Unable to parse $l as operation")
            }
        }
        .fold(ExecutionState()) { accumulator, op ->
            TODO("Not implemented yet")
        }
        .also { es ->
            println(displayScreen(es.screen))
        }
}

