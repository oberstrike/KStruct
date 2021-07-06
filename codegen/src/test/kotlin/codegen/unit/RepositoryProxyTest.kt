package codegen.unit


import com.maju.cli.IConverter
import com.maju.cli.RepositoryProxy
import com.maju.domain.generator.ConverterEntity
import com.maju.domain.generator.MethodEntity
import com.maju.domain.generator.ParameterEntity
import com.maju.domain.generator.RepositoryEntity
import com.maju.domain.repository.proxy.RepositoryProxyGenerator
import com.maju.utils.parameterizedToType
import com.maju.utils.toType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import org.junit.jupiter.api.Test

data class Person(val name: String)

data class PersonDTO(val name: String)

data class Custom(val custom: String)

data class CustomDTO(val custom: String)

@RepositoryProxy(converters = [PersonConverter::class])
interface PersonRepository {
    fun findByName(name: String): Person
    fun isDeleted(id: Long): Boolean
    fun save(person: Person): Person
    fun getAll(persons: List<Person>): List<Person>
}

class PersonConverter : IConverter<Person, PersonDTO> {
    override fun convertDTOToModel(dto: PersonDTO): Person {
        return Person(dto.name)
    }

    override fun convertModelToDTO(model: Person): PersonDTO {
        return PersonDTO(model.name)
    }

}

class CustomConverter: IConverter<Custom, CustomDTO>{
    override fun convertDTOToModel(dto: CustomDTO): Custom {
        TODO("Not yet implemented")
    }

    override fun convertModelToDTO(model: Custom): CustomDTO {
        TODO("Not yet implemented")
    }

}

@KotlinPoetMetadataPreview
class RepositoryProxyTest {


    @Test
    fun `Panache query`(){
        val originType = Person::class.toType()
        val targetType = PersonDTO::class.toType()
        val panacheType = PanacheQuery::class.toType(targetType)

        val personConverterType = PersonConverter::class.toType()

        val personConverter = ConverterEntity(
            personConverterType,
            originType,
            targetType,
            originToTargetFunctionName = "convertModelToDTO",
            targetToOriginFunctionName = "convertDTOToModel"
        )
        val personRepositoryType = PersonRepository::class.toType()


        val testParameter = ParameterEntity(
            name = "test",
            type = STRING.toType()
        )
        val findByNameMethod = MethodEntity(
            "findByName",
            listOf(testParameter),
            panacheType,
            false
        )

        val repositoryProxyEntity = RepositoryEntity(
            type = personRepositoryType,
            methods = listOf(findByNameMethod),
            converters = listOf(personConverter),
            name = "PersonRepositoryProxy"
        )

        val repositoryProxyGenerator = RepositoryProxyGenerator("com.test", repositoryProxyEntity)


        val fileSpec = repositoryProxyGenerator.generate()
        println(fileSpec)

    }


    @Test
    fun `test whether it works`() {
        val originType = Person::class.toType()
        val targetType = PersonDTO::class.toType()

        val customOriginType = Custom::class.toType()
        val customTargetType = CustomDTO::class.toType()


        val personRepositoryType = PersonRepository::class.toType()
        val personConverterType = PersonConverter::class.toType()
        val customConverterType = CustomConverter::class.toType()

        val idParam = ParameterEntity(
            name = "id",
            type = INT.toType()
        )

        val nameParameter = ParameterEntity(
            name = "name",
            type = STRING.toType()
        )

        val testParameter = ParameterEntity(
            name = "test",
            type = STRING.toType()
        )

        val personParam = ParameterEntity(
            name = "person",
            type = targetType.copy(isNullable = true)
        )

        val customParam = ParameterEntity(
            name = "custom",
            type = customTargetType
        )

        val findByNameMethod = MethodEntity(
            "findByName",
            listOf(nameParameter, testParameter),
            targetType,
            false
        )

        val saveMethod = MethodEntity(
            name = "save",
            listOf(personParam),
            targetType.copy(isNullable = true),
            true
        )

        val deleteMethod = MethodEntity(
            name = "deleteById",
            parameters = listOf(idParam),
            returnType = BOOLEAN.toType(),
            isSuspend = false
        )


        val customTypeMethod = MethodEntity(
            name = "customParam",
            parameters = listOf(customParam),
            returnType = LIST.parameterizedToType(customTargetType),
            isSuspend = false
        )

        val personConverter = ConverterEntity(
            personConverterType,
            originType,
            targetType,
            originToTargetFunctionName = "convertModelToDTO",
            targetToOriginFunctionName = "convertDTOToModel"
        )
        val customConverter = ConverterEntity(
            customConverterType,
            Custom::class.toType(),
            CustomDTO::class.toType(),
            originToTargetFunctionName = "convertModelToDTO",
            targetToOriginFunctionName = "convertDTOToModel"
        )



        val repositoryProxyEntity = RepositoryEntity(
            type = personRepositoryType,
            methods = listOf(findByNameMethod, saveMethod, deleteMethod, customTypeMethod),
            converters = listOf(personConverter, customConverter),
            name = "PersonRepositoryProxy"
        )

        val repositoryProxyGenerator = RepositoryProxyGenerator("com.test", repositoryProxyEntity)


        val fileSpec = repositoryProxyGenerator.generate()
        println(fileSpec)

    }


}
