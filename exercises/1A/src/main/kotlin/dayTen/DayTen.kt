package dayTen

import arrow.core.Either
import arrow.core.Either.*
import arrow.fx.coroutines.parMap
import arrow.optics.optics
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.fold
import kotlin.io.path.Path

const val debug = false

sealed class Operations
object Noop : Operations()
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

@optics
data class ExecutionState(
    val ticks: Int = 0,
    val value: Int = 1,
    val crtBuffer: List<Char> = emptyList(),
    val screen: List<List<Char>> = emptyList()
) {
    companion object
}

fun tick(executionState: ExecutionState): ExecutionState = run {
    val result = ExecutionState.ticks.modify(executionState) { it + 1 }
    // Determine if we should append signal strength
    result
}

fun <T> List<List<T>>.appendList(l: List<T>): List<List<T>> = this + listOf(l)

fun writeSprite(horizontalPosition: Int, sprite: List<Int>, executionState: ExecutionState): ExecutionState =
    if (horizontalPosition in sprite) {
        ExecutionState.crtBuffer.modify(executionState) { it + listOf('#') }
    } else {
        ExecutionState.crtBuffer.modify(executionState) { it + listOf('.') }
    }

fun writeBufferToScreen(executionState: ExecutionState) =
    if (executionState.crtBuffer.size == 40) {
        val withScreenWrite = ExecutionState.screen.modify(executionState) { it.appendList(executionState.crtBuffer) }
        ExecutionState.crtBuffer.modify(withScreenWrite) { emptyList() }
    } else {
        executionState
    }

fun pixel(executionState: ExecutionState): ExecutionState = run {
    // If our current horizontal position overlaps with the register value:
    // write out '#', else '.'
    val horizontalPosition = (executionState.ticks - 1) % 40
    val sprite = listOf(executionState.value - 1, executionState.value, executionState.value + 1)
    val withBufferUpdate = writeSprite(horizontalPosition, sprite, executionState)
    if (debug) {
        println("before: $executionState after: $withBufferUpdate")
    }
    // If we have a full crtBuffer, write to screen
    val withScreenUpdate = writeBufferToScreen(withBufferUpdate)
    if (debug) {
        println("$sprite $horizontalPosition $withScreenUpdate")
    }
    withScreenUpdate
}

fun noop(executionState: ExecutionState): ExecutionState = run {
    val result = tick(executionState)
    pixel(result)
}


fun addX(executionState: ExecutionState, addX: AddX): ExecutionState = run {
    val firstTick = pixel(tick(executionState))
    val secondTick = pixel(tick(firstTick))
    ExecutionState.value.modify(secondTick) { it + addX.value }
}

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

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayTen() {
    val path = Path("inputFiles/dayTenErrors.txt")
    lines(path)
        .parMap { l ->
            when (val r = operationsParser.parse(l)) {
                is ParserResult.Ok -> Either.Right(r.result)
                is ParserResult.Error -> Either.Left("Unable to parse $l as operation")
//                is ParserResult.Error -> throw Error("Unable to parse $l as operation")
            }
        }
        .fold(ExecutionState()) { acc, eitherOp: Either<String, Operations> ->
            println("op is $eitherOp")
            when (eitherOp) {
                is Right<Operations> -> {
                    when (val op = eitherOp.value) {
                        is Noop -> noop(acc)
                        is AddX -> addX(acc, op)
                    }
                }

                is Left<String> -> {
                    println("Error: $eitherOp")
                    acc
                }
//                is Either.Right -> when(val )
//                Either.
//                else ->
            }
        }

        .also { r ->
            println(displayScreen(r.screen))
        }
}
