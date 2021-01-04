package cli

import core.IConverter
import org.mapstruct.Mapper

@Mapper
interface PersonMapper : IConverter<Person, PersonDTO> {

}

