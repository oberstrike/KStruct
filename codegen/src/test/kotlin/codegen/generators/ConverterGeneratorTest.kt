package codegen.generators

import com.maju.annotations.IConverter

@ExperimentalStdlibApi
class ConverterGeneratorTest {

    /**
     * Example
     */

    class Word

    class WordDTO


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
