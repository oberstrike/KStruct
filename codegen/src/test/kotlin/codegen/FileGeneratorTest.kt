package codegen


import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.maju.FileGenerator
import com.maju.utils.IConverter
import org.junit.Test


class FileGeneratorTest {

    @KotlinPoetMetadataPreview
    @Test
    fun schemaTest() {
        val clazzName = """SchemaTest2"""

        val kotlinSource = SourceFile.kotlin(
            "KClass.kt",
            """
            import com.maju.utils.IConverter
            import com.maju.annotations.RepositoryProxy
            import com.maju.annotations.InjectionStrategy
            import kotlin.collections.List

            data class Person(val name: String)
            
            data class PersonDTO(val name: String) 
            
            interface TestRepository{
                fun delete(person: Person)
            }

            
            @RepositoryProxy(converter = PersonConverter::class,
              componentModel = "cdi",
              injectionStrategy = InjectionStrategy.PROPERTY
              )
            interface PersonRepository: TestRepository{
                fun findByName(name: String): Person?
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
                
                override fun delete(person: Person){
                    println("delete")
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
