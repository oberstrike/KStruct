package cli

import com.maju.cli.IConverter
import org.mapstruct.Mapper

@Mapper
interface PersonMapper : IConverter<Person, PersonDTO> {

}

@Mapper
interface CustomMapper: IConverter<Custom, CustomDTO>{

}