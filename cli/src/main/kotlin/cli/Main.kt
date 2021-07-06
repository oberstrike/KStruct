@file:JvmName("Main")

package cli


fun main() {

}

data class Person(
    val name: String,
    val id: Long
)

data class PersonDTO(
    val name: String
)

data class Custom(
    val name: String,
    val id: Long
)

data class CustomDTO(
    val name: String
)


