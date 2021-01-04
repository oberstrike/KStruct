package com.maju.utils


interface IConverter<T, S > {
    fun convertModelToDTO(model: T): S
    fun convertDTOToModel(dto: S): T
}
