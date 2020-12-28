package core.codegen

import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class FileGeneratorTest {

    @KotlinPoetMetadataPreview
    @Test
    fun schemaTest() {
        val clazzName = """SchemaTest2"""

        val kotlinSource = SourceFile.kotlin(
            "KClass.kt",
            """
            import core.codegen.Converter

            @Converter
            data class $clazzName(val text: kotlin.collections.List<String>)
             
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
