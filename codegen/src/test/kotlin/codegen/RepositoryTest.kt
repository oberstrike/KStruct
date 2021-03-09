package codegen


import com.maju.annotations.IConverter
import com.maju.annotations.RepositoryProxy
import com.maju.entities.*
import com.maju.generators.repository.proxy.RepositoryProxyGenerator
import com.maju.utils.STREAM
import com.maju.utils.parameterizedToType
import com.maju.utils.toType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
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

    override fun convertDTOsToModels(dtos: List<PersonDTO>): List<Person> {
        TODO("Not yet implemented")
    }

    override fun convertModelsToDTOs(models: List<Person>): List<PersonDTO> {
        TODO("Not yet implemented")
    }

}

class CustomConverter: IConverter<Custom, CustomDTO>{
    override fun convertDTOToModel(dto: CustomDTO): Custom {
        TODO("Not yet implemented")
    }

    override fun convertModelToDTO(model: Custom): CustomDTO {
        TODO("Not yet implemented")
    }

    override fun convertDTOsToModels(dtos: List<CustomDTO>): List<Custom> {
        TODO("Not yet implemented")
    }

    override fun convertModelsToDTOs(models: List<Custom>): List<CustomDTO> {
        TODO("Not yet implemented")
    }
}

@KotlinPoetMetadataPreview
class RepositoryTest {


    @Test
    fun generatedTest() {
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
            targetType
        )

        val saveMethod = MethodEntity(
            name = "save",
            listOf(personParam),
            targetType.copy(isNullable = true)
        )

        val deleteMethod = MethodEntity(
            name = "deleteById",
            parameters = listOf(idParam),
            returnType = BOOLEAN.toType()
        )


        val customTypeMethod = MethodEntity(
            name = "customParam",
            parameters = listOf(customParam),
            returnType = LIST.parameterizedToType(customTargetType)
        )

        val personConverter = ConverterEntity(personConverterType, originType, targetType)
        val customConverter = ConverterEntity(customConverterType, Custom::class.toType(), CustomDTO::class.toType())



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
