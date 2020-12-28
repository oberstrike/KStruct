package core

import core.util.IDTO
import core.util.IModel

interface IConverter<T : IModel, S : IDTO> {
    fun convertModelToDTO(model: T): S
    fun convertDTOToModel(dto: S): T
}
