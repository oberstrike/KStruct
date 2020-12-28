@file:JvmName("Main")

package cli

import core.IConverter
import core.initKStruct
import core.util.IDTO
import core.util.IModel

fun main() {
    val person = PersonService(PersonRepositoryImpl())
    val getPerson = person.getPerson()

    val person2 = converter.convert(getPerson)
    println(person2)
}

data class Person(
    val name: String
) : IModel

data class PersonDTO(
    val name: String
) : IDTO

val converter = initKStruct {
    add(PersonMapperImpl())
}


