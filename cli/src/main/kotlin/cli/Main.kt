@file:JvmName("Main")

package cli


fun main() {
    val converter = PersonMapperImpl()
    val repository = PersonRepositoryImpl()
    val personProxy = PersonRepositoryProxy(converter, repository)
    val personService = PersonService(personProxy)
    val person = personService.getPerson()
}

data class Person(
    val name: String
)

data class PersonDTO(
    val name: String
)


