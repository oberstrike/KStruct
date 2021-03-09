@file:JvmName("Main")

package cli


fun main() {
    val repository = PersonRepositoryImpl()
}

data class Person(
    val name: String
)

data class PersonDTO(
    val name: String
)


