
import arrow.core.raise.fold
import dayTen.dayTen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun main() {
    fold({ dayTen() }, { println("top level error $it ") }, {  })
}
