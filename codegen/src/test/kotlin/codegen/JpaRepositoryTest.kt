package codegen

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository

class JpaRepositoryTest {

    data class Person(val id: Long)

    interface PersonRepository: JpaRepository<Person, Long>

}