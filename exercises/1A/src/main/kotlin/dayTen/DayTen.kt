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
import arrow.core.raise.either

const val debug = false
const val tickBoom = false

sealed class Operations
data object Noop : Operations()
data class AddX(val value: Int) : Operations()

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

context (Raise<TickError>)
fun noop(executionState: ExecutionState): ExecutionState = run {
    val result = tick(executionState, Noop)
    pixel(result)
}

context (Raise<TickError>)
fun addX(executionState: ExecutionState, adding: AddX): ExecutionState = run {
    val firstTick = pixel(tick(executionState, adding))
    val secondTick = pixel(tick(firstTick, adding))
    ExecutionState.value.modify(secondTick) { it + adding.value }
}
@optics
data class ExecutionState(
    val ticks: Int = 0,
    val value: Int = 1,
    val crtBuffer: List<Char> = emptyList(),
    val screen: List<List<Char>> = emptyList()  // listOf(listOf('#')
) {
    companion object
}

/**
fun ExecutionState.signalStrength(): Int? =
if (ticks == 20 || (ticks > 20 && (ticks - 20) % 40 == 0)) {
// println("Cycle is: $ticks Register is: $value Result: ${ticks * value}")
ticks * value
} else {
null
}
 */

context (Raise<TickError>)
fun tick(executionState: ExecutionState, dummyOp: Operations): ExecutionState = run{
    if (executionState.ticks > 20 && tickBoom ) {
        if (dummyOp is AddX) {
            raise(TickAddError("ticks at ${executionState.ticks}"))
        }
    }
    if (executionState.ticks > 30 && tickBoom ) {
        if(dummyOp is Noop) {
            raise(TickNoopError("ticks at ${executionState.ticks}"))
        }
    }
    ExecutionState.ticks.modify(executionState) { it + 1 }
}
/** val result = ExecutionState.ticks.modify(executionState) { it + 1 }
// // Determine if we should append signal strength
//    result.signalStrength()?.let { strength ->
//        ExecutionState.signal.modify(result) { it + listOf(strength) }
//    } ?: result
 **/

fun pixel(executionState: ExecutionState): ExecutionState = run {
    // If our current horizontal position overlaps with the register value:
    // write out '#', else '.'
    val horizontalPosition = (executionState.ticks - 1) % 40
    val sprite = listOf(executionState.value - 1, executionState.value, executionState.value + 1)
    val withBufferUpdate = pixelTheSprite(horizontalPosition, sprite, executionState)
    if (debug) {
        println("before: $executionState after: $withBufferUpdate")
    }
    // If we have a finished the line/crtBuffer, write to screen. else nothing
    writeLineToScreenBuffer(withBufferUpdate)
//    if (debug) {
//        println("$sprite $horizontalPosition $withScreenUpdate")
//    }
//    withScreenUpdate
}

// report an error for pixeling the sprite, like we accidentally wrote a #
// If our current horizontal position overlaps with the register value, pixelthesprite
fun pixelTheSprite(horizontalPosition: Int, sprite: List<Int>, executionState: ExecutionState): ExecutionState =
    if (horizontalPosition in sprite) {
        ExecutionState.crtBuffer.modify(executionState) { it + listOf('@') }
    } else {
        ExecutionState.crtBuffer.modify(executionState) { it + listOf(' ') }
    }

fun writeLineToScreenBuffer(executionState: ExecutionState) =
    if (executionState.crtBuffer.size == 40) {
        val withScreenWrite = ExecutionState.screen.modify(executionState) { it.appendList(executionState.crtBuffer) }
        ExecutionState.crtBuffer.modify(withScreenWrite) { emptyList() }
    } else {
        executionState
    }
fun <T> List<List<T>>.appendList(l: List<T>): List<List<T>> = this + listOf(l)

fun displayScreen(input: List<List<Char>>) = run {
    val columns = 40
    val rows = 6
    (0 until rows).forEach { row ->
        (0 until columns).forEach { column ->
            print(input[row][column])
        }
        println()
    }
}



fun parseCustomError(input: String): Either<ParseError, Operations> =
    when (val r = operationsParser.parse(input)) {
        is ParserResult.Ok -> Either.Right(r.result)
        is ParserResult.Error -> Either.Left(ParseError("Unable to parse. $r "))
//                is ParserResult.Error -> throw Error("Unable to parse $l as operation")
    }

context (Raise<ParseError>)
fun parseCustomErrorWithRaise(input: String): Operations = parseCustomError(input).bind()

context (Raise<LogicalError>)
@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayTen() {
    val path = Path("inputFiles/dayTen.txt")
    lines(path)
        .parMap {
            either { parseCustomErrorWithRaise(it) }
        }
        .fold(ExecutionState()) { acc, eitherOp: Either<LogicalError, Operations> ->
            println("op is $eitherOp")
            when (eitherOp) {
                is Either.Right -> when (val op = eitherOp.value) {
                    is Noop -> noop(acc)
                    is AddX -> addX(acc, op)
                }
                is Either.Left -> acc
            }
        }
        .also { r ->
            println(displayScreen(r.screen))
        }
    //                now we  don't need this as we handle the error at the parse stage of the code
    //                is Left<ParseError> -> {
    //                    println("Error: $eitherOp")
    //                    acc
    //                }

}


sealed interface LogicalError
data class ParseError(val message: String) : LogicalError
data object InvalidInputError : LogicalError
sealed interface TickError : LogicalError
data class TickAddError(val message: String) : TickError
data object ScreenWriteError : LogicalError
data class TickNoopError(val message: String) : TickError
