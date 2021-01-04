@file:JvmName("Main")

package cli

import core.util.IDTO
import core.util.IModel

fun main() {
    val converter = PersonMapperImpl()
    val repository = PersonRepositoryImpl()
    val personProxy = PersonRepositoryProxy(converter, repository)
    val personService = PersonService(personProxy)
    val person = personService.getPerson()
    println(person)
}

data class Person(
    val name: String
) : IModel

data class PersonDTO(
    val name: String
) : IDTO


