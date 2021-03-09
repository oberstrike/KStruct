package codegen


import com.maju.FileGenerator
import com.maju.entities.PanacheEntity
import com.squareup.kotlinpoet.classinspector.reflective.ReflectiveClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource


class FileGeneratorTest {

    @KotlinPoetMetadataPreview
    @ParameterizedTest
    @ValueSource(strings = ["PROPERTY", "CONSTRUCTOR", "DEFAULT"])
    fun generateRepositoryProxyTest(strategy: String) {
        val clazzName = """SchemaTest2"""
        val converterName = "PersonConverter"
        val packageName = """com.test"""
        val findByNameMethodName = """findByName"""
        val superDeleteMethodName = """delete"""
        val isDeletedMethodName = """isDeleted"""
        val componentModel = """cdi"""
        val injectionStrategy = """InjectionStrategy.$strategy"""


        val saveMethodName = """save"""

        val getAllMethodName = """getAll"""

        val kotlinSource = SourceFile.kotlin(
            "KClass.kt",
            """
            package $packageName
                
            import com.maju.annotations.IConverter
            import com.maju.annotations.RepositoryProxy
            import com.maju.annotations.InjectionStrategy
            import kotlin.collections.List
            import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository

            data class Person(val name: String)
            
            data class PersonDTO(val name: String) 
            
            interface SuperRepository{
                fun $superDeleteMethodName(person: Person)
            }
           
            data class Paged(
                val persons: List<Person>? = null
            )
           
            interface TestRepository: SuperRepository
            
            @RepositoryProxy(converters = [$converterName::class],
              componentModel = "$componentModel",
              injectionStrategy = $injectionStrategy
              )
            interface $clazzName: TestRepository{
                fun $findByNameMethodName(name: String): Person?
                fun $isDeletedMethodName(id: Long): Boolean
                fun $saveMethodName(person: Person): Person
                fun $getAllMethodName(persons: List<Person>): List<Person>
                fun getCustomType(): Paged
            }
            
            @RepositoryProxy(converters = [$converterName::class],
             componentModel = "$componentModel",
             injectionStrategy = $injectionStrategy
            )
            interface PanacheTest: PanacheRepository<Person> {
            
            }
           
            
            class $converterName : IConverter<Person, PersonDTO> {
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
        val generatedClazzName = "${clazzName}Proxy"
        assert(sourcesGeneratedNames.contains("$generatedClazzName.kt"))

        val classLoader = result.classLoader

        val classInspector = ReflectiveClassInspector.create()
        //TODO wait for kotlinpoet update

    }


}
