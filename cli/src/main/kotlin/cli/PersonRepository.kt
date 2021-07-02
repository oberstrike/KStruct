package cli

import com.maju.annotations.RepositoryProxy
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import org.springframework.data.jpa.repository.JpaRepository

@RepositoryProxy(converters = [PersonMapper::class, CustomMapper::class])
interface PersonRepository: PanacheRepository<Person> {
    fun custom(custom: Custom, person: Person): Person
}
