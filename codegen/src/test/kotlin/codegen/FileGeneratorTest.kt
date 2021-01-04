package codegen


import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.maju.FileGenerator
import org.junit.Test

class FileGeneratorTest {

    @KotlinPoetMetadataPreview
    @Test
    fun schemaTest() {
        val clazzName = """SchemaTest2"""


        val kotlinSource = SourceFile.kotlin(
            "KClass.kt",
            """
            import core.IConverter
            import core.util.IDTO
            import core.util.IModel
            import codegen.annotations.RepositoryProxy
            import core.IRepository
            import codegen.annotations.InjectionStrategy
            import kotlin.collections.List

            data class Person(val name: String) : IModel
            
            data class PersonDTO(val name: String) : IDTO
            
            @RepositoryProxy(converter = PersonConverter::class,
              modelClass = Person::class,
              dtoClass = PersonDTO::class,
              componentModel = "cdi",
              injectionStrategy = InjectionStrategy.PROPERTY
              )
            interface PersonRepository: IRepository<Person, PersonDTO> {
                fun findByName(name: String): Person
                fun isDeleted(id: Long): Boolean
                fun save(person: Person): Person
                fun getAll(persons: List<Person>): List<Person>

            }
            
            class PersonRepositoryImpl : PersonRepository {
                override fun findByName(name: String): Person {
                    return Person(name)
                }
            
                override fun isDeleted(id: Long): Boolean {
                    return true
                }
            
                override fun save(person: Person): Person {
                    return person
                }
                
                override fun getAll(persons: List<Person>): List<Person> {
                      return persons
                }
            }
            
            class PersonConverter : IConverter<Person, PersonDTO> {
                override fun convertDTOToModel(dto: PersonDTO): Person {
                    return Person(dto.name)
                }
            
                override fun convertModelToDTO(model: Person): PersonDTO {
                    return PersonDTO(model.name)
                }
            }
        """.trimIndent()
        )

        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            annotationProcessors = listOf(FileGenerator())

            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        val sourcesGeneratedByAnnotationProcessor = result.sourcesGeneratedByAnnotationProcessor
        val sourcesGeneratedNames = sourcesGeneratedByAnnotationProcessor.map { it.name }

        sourcesGeneratedByAnnotationProcessor.forEach {
            val code = it.readText()
            println(code)
        }
        val generatedClazzName = "I$clazzName"
    }
}
