@file:JvmName("Main")

package cli

import core.*

fun main(vararg args: String) {
    val person = Person("Markus")
    val dto = KStruct.convertModelToDTO<Person, PersonDTO>(person)
    println(dto)

}

data class Person(
    val name: String
) : IModel

data class PersonDTO(
    val name: String
) : IDTO

class PersonConverter : IConverter<Person, PersonDTO> {
    override fun convertModelToDTO(model: Person): PersonDTO {
        return PersonDTO(name = model.name)
    }
}

val converters = converters {
    add(PersonConverter())
}


