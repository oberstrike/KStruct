package cli

import com.maju.utils.IConverter
import org.mapstruct.Mapper

@Mapper
interface PersonMapper : IConverter<Person, PersonDTO> {

}

