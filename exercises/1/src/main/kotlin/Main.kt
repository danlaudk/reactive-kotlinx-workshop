

fun <T> compress(list: List<T>) =
    list.fold(emptyList<T>()) {
        TODO("Not implemented yet")
    }

fun main() {

    val li = listOf('a','a','a','a','b','c','c','a','a','d','e','e','e','e')
    println(compress(li))
    /*

If a list contains repeated elements they should be replaced with a single copy of the element. The order of the elements should not be changed.
Use fold

Example :
compress "aaaabccaadeeee"
"abcade"
     */

}
