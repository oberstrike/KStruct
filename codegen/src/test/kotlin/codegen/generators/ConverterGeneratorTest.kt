package codegen.generators

import com.maju.annotations.Converter
import com.maju.generators.converter.ConverterGenerator
import core.IConverter
import core.util.IDTO
import core.util.IModel
import org.junit.Test

@ExperimentalStdlibApi
class ConverterGeneratorTest {

    /**
     * Example
     */

    class Word : IModel

    class WordDTO : IDTO

    @Converter
    interface ITestConverter : IConverter<Word, WordDTO>

    class ConverterImpl : ITestConverter{
        override fun convertDTOToModel(dto: WordDTO): Word {
            TODO("Not yet implemented")
        }

        override fun convertModelToDTO(model: Word): WordDTO {
            TODO("Not yet implemented")
        }
    }


    @Test
    fun generateTest() {
        val generator = ConverterGenerator()

        val fileSpec = generator.generate()
        println(fileSpec)

    }
}
