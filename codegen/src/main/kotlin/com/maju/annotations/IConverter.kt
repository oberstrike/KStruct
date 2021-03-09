package com.maju.annotations

import java.util.stream.Stream


interface IConverter<T, S> {
    fun convertModelToDTO(model: T): S

    fun convertDTOToModel(dto: S): T

    fun convertModelsToDTOs(models: List<T>): List<S>

    fun convertDTOsToModels(dtos: List<S>): List<T>

    fun convertStreamModelsToDTOs(models: Stream<T>): Stream<S> = models.map(this::convertModelToDTO)

    fun convertStreamDTOsToModels(dtos: Stream<S>): Stream<T> = dtos.map(this::convertDTOToModel)

}
