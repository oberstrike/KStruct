package cli

import com.maju.cli.RepositoryProxy
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository

@RepositoryProxy(converters = [PersonMapper::class, CustomMapper::class])
interface PersonRepository: PanacheRepository<Person> {
    fun custom(custom: Custom, person: Person): Person
}
