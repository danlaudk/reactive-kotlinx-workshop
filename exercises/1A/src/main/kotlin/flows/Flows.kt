package flows

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield
import java.nio.file.Path
import kotlin.io.path.useLines

fun lines(path: Path, skipWhitespace: Boolean = false) = flow {
    path.useLines { lines ->
        lines.forEach { line ->
            if (skipWhitespace && line.isEmpty()) {
                yield()
            } else {
                emit(line)
            }
        }
    }
}.flowOn(Dispatchers.IO)

fun linesSeq(path: Path, skipWhitespace: Boolean = false) = sequence<String> {
    path.useLines { lines ->
        lines.forEach { line ->
            if (!(skipWhitespace && line.isEmpty())) {
                yield(line)
            }
        }
    }
}
