import kotlin.random.Random

sealed interface Reading {
    val id: Int
}

data class ValidReading(override val id: Int, val value: Double = Random.nextDouble()) : Reading {
    override fun toString(): String {
        return "ValidReading($id,$value)"
    }
}

data class InvalidReading(override val id: Int) : Reading
