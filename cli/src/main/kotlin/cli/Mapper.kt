package cli

import com.maju.annotations.IConverter
import org.mapstruct.Mapper

@Mapper
interface PersonMapper : IConverter<Person, PersonDTO> {

}

@Mapper
interface CustomMapper: IConverter<Custom, CustomDTO>{

}