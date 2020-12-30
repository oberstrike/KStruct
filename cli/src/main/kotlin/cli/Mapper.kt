package cli

import codegen.Converter
import core.IConverter
import org.mapstruct.Mapper

@Mapper
@Converter
interface PersonMapper: IConverter<Person, PersonDTO> {

}

