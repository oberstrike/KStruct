package codegen.generators

import com.maju.annotations.Converter
import com.maju.utils.IConverter

@ExperimentalStdlibApi
class ConverterGeneratorTest {

    /**
     * Example
     */

    class Word

    class WordDTO

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


}
