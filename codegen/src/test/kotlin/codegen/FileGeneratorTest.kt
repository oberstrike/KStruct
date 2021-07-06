package codegen


import com.maju.FileGenerator
import com.maju.cli.ComponentModel
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.classinspector.reflective.ReflectiveClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URLClassLoader



class FileGeneratorTest {

    @KotlinPoetMetadataPreview
    @ParameterizedTest
    @ValueSource(strings = ["PROPERTY", "CONSTRUCTOR", "DEFAULT"])
    fun generateRepositoryProxyTest(strategy: String) {
        ComponentModel.CDI
        val clazzName = """SchemaTest2"""
        val converterName = "PersonConverter"
        val packageName = """com.test"""
        val findByNameMethodName = """findByName"""
        val superDeleteMethodName = """delete"""
        val isDeletedMethodName = """isDeleted"""
        val componentModel = """ComponentModel.CDI"""
        val injectionStrategy = """InjectionStrategy.$strategy"""
        val saveMethodName = """save"""
        val getAllMethodName = """getAll"""
        val panacheTestRepositoryName = "PanacheTest"

        val kotlinSource = SourceFile.kotlin(
            "KClass.kt",
            """
            package $packageName
                
            import com.maju.cli.IConverter
            import com.maju.cli.RepositoryProxy
            import com.maju.cli.InjectionStrategy
            import kotlin.collections.List
            import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
            import com.maju.cli.ComponentModel
            import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity

            data class Person(val name: String, override var id: Long? = null): PanacheEntity()
            
            data class PersonDTO(val name: String) 
            
            interface SuperRepository{
                fun $superDeleteMethodName(person: Person)
            }
           
            data class Paged(
                val persons: List<Person>? = null
            )
           
            interface TestRepository: SuperRepository
            
            @RepositoryProxy(converters = [$converterName::class],
              componentModel = $componentModel,
              injectionStrategy = $injectionStrategy
              )
            interface $clazzName: TestRepository{
                fun $findByNameMethodName(name: String): Person?
                fun $isDeletedMethodName(id: Long): Boolean
                fun $saveMethodName(person: Person): Person
                fun $getAllMethodName(persons: List<Person>): List<Person>
                fun getCustomType(): Paged
                private fun getName(person: Person): String = "Hallo"
            }
            
            @RepositoryProxy(converters = [$converterName::class],
             componentModel = $componentModel,
             injectionStrategy = $injectionStrategy
            )
            interface $panacheTestRepositoryName: PanacheRepository<Person> {
            
            }
           
            
            class $converterName : IConverter<Person, PersonDTO> {
                override fun convertDTOToModel(dto: PersonDTO): Person {
                    return Person(dto.name)
                }
            
                override fun convertModelToDTO(model: Person): PersonDTO {
                    return PersonDTO(model.name)
                }
                
                
            }
        """.trimIndent()
        )

        val compilationResult = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            annotationProcessors = listOf(FileGenerator())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assert(compilationResult.exitCode == KotlinCompilation.ExitCode.OK)

        val sourcesGeneratedByAnnotationProcessor = compilationResult.sourcesGeneratedByAnnotationProcessor
        val sourcesGeneratedNames = sourcesGeneratedByAnnotationProcessor.map { it.name }

        //Check first proxy
        val generatedProxyClazzName = "${clazzName}Proxy"
        assert(sourcesGeneratedNames.contains("$generatedProxyClazzName.kt"))

        val classLoader = compilationResult.classLoader
        val classInspector = ReflectiveClassInspector.create(classLoader)

        val generatedProxyClass = classLoader.loadClass("com.test.$generatedProxyClazzName")
        val generatedProxyContainerData = classInspector.containerData(
            generatedProxyClass.toImmutableKmClass(),
            generatedProxyClass.asClassName(),
            null
        )
        val container = generatedProxyContainerData.declarationContainer
        val functions = container.functions
        Assertions.assertEquals(6, functions.size)
        val functionNames = functions.map { it.name }
        Assertions.assertTrue(functionNames.contains(superDeleteMethodName))
        Assertions.assertTrue(functionNames.contains(findByNameMethodName))
        Assertions.assertTrue(functionNames.contains(getAllMethodName))
        Assertions.assertTrue(functionNames.contains(saveMethodName))


        testPanacheTestRepository(
            panacheTestRepositoryName,
            sourcesGeneratedNames,
            classLoader,
            classInspector
        )


    }

    @KotlinPoetMetadataPreview
    private fun testPanacheTestRepository(
        panacheTestRepositoryName: String,
        sourcesGeneratedNames: List<String>,
        classLoader: URLClassLoader,
        classInspector: ClassInspector
    ) {
        //Check panache proxy test
        val generatedPanacheProxyClazzName = "${panacheTestRepositoryName}Proxy"
        assert(sourcesGeneratedNames.contains("$generatedPanacheProxyClazzName.kt"))

        val generatedPanacheProxyClass = classLoader.loadClass("com.test.$generatedPanacheProxyClazzName")
        val generatedPanacheProxyContainerData = classInspector.containerData(
            generatedPanacheProxyClass.toImmutableKmClass(),
            generatedPanacheProxyClass.asClassName(),
            null
        )
        val container = generatedPanacheProxyContainerData.declarationContainer
        Assertions.assertEquals(22, container.functions.size)
    }


}
