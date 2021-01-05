package cli

import com.maju.annotations.RepositoryProxy

class PersonService(private val personRepositoryProxy: PersonRepositoryProxy) {
    fun getPerson() = personRepositoryProxy.getPerson()
    fun save(person: PersonDTO) = personRepositoryProxy.save(person)
    fun findAll() = personRepositoryProxy.findAll()
}

interface SuperRepository {
    fun delete(person: Person): Boolean
}

@RepositoryProxy(converter = PersonMapper::class)
interface PersonRepository: SuperRepository {
    fun getPerson(): Person
    fun save(person: Person)
    fun findAll(): List<Person>
}

class PersonRepositoryImpl : PersonRepository {
    override fun getPerson() = Person("Markus")
    override fun save(person: Person) {
        println("Speichere.. $person")
    }

    override fun findAll(): List<Person> {
        return listOf()
    }

    override fun delete(person: Person): Boolean {
        return true
    }
}




