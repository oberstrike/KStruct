package cli

import core.util.IConvertable
import core.convert

class PersonService(private val personRepository: PersonRepository) {
    fun getPerson(): PersonDTO = converter.convert(personRepository.getPerson()) as PersonDTO
}

interface PersonRepository {
    fun getPerson(): Person
}

class PersonRepositoryImpl : PersonRepository {
    override fun getPerson() = Person("Markus")
}

class PersonRepositoryProxy : PersonRepository {

    override fun getPerson(): Person {
        TODO("Not yet implemented")
    }

}




