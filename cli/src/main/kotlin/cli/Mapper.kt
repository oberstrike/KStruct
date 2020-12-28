package cli

import core.IConverter
import core.codegen.Converter
import org.mapstruct.Mapper

@Mapper
@Converter
interface PersonMapper: IConverter<Person, PersonDTO> {

}

