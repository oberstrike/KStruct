package cli

import codegen.RepositoryProxy

class PersonService(private val personRepository: PersonRepository) {
    fun getPerson(): PersonDTO = converter.convert(personRepository.getPerson()) as PersonDTO
}


@RepositoryProxy
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




