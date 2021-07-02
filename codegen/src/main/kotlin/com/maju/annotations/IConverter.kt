package com.maju.annotations



interface IConverter<T, S> {
    fun convertModelToDTO(model: T): S

    fun convertDTOToModel(dto: S): T
}
