package cli

import com.maju.annotations.RepositoryProxy
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository

class PersonService(private val personRepositoryProxy: PersonRepositoryProxy) {
    fun getPerson() = personRepositoryProxy.getPerson()
    fun save(person: PersonDTO) = personRepositoryProxy.save(person)
}

@RepositoryProxy(converter = PersonMapper::class)
interface PersonRepository: PanacheRepository<Person> {
    fun getPerson(): Person
    fun save(person: Person)
}

class PersonRepositoryImpl : PersonRepository {
    override fun getPerson() = Person("Markus")
    override fun save(person: Person) {
        println("Speichere.. $person")
    }
}




