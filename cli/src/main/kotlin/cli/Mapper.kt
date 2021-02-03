package cli

import com.maju.annotations.IConverter
import org.mapstruct.Mapper

@Mapper
interface PersonMapper : IConverter<Person, PersonDTO> {

}

