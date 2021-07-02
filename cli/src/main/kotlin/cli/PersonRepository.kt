package cli

import com.maju.annotations.RepositoryProxy
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import org.springframework.data.jpa.repository.JpaRepository

@RepositoryProxy(converters = [PersonMapper::class])
interface PersonRepository: PanacheRepository<Person> {

}
