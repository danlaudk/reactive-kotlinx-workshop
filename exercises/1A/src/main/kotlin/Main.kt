
import arrow.core.raise.Raise
import arrow.core.raise.fold
import dayTen.ParseError
import dayTen.dayTen
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun main() {
    dayTen()
//    fold({ dayTen() }, { println(it) }, { println(it) })

}
