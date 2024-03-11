
import arrow.core.raise.Raise
import arrow.core.raise.fold
import dayTen.ParseError
import dayTen.dayTen
import kotlinx.coroutines.runBlocking

suspend fun main() {
    dayTen()
//    fold({ dayTen() }, { println(it) }, { println(it) })

}
