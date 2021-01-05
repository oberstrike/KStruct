package com.maju.utils

import java.util.stream.Stream
import kotlin.streams.toList

interface IRepositoryProxy<T, U> {
    val converter: IConverter<T, U>
}

@Suppress("UNCHECKED_CAST")
class RepositoryProxyHelper<T, U>(
    override val converter: IConverter<T, U>
) : IRepositoryProxy<T, U> {

    fun <S> compute(block: () -> S): S {
        return block()
    }

    fun toDTO(block: () -> T?): U = block()?.let { converter.convertModelToDTO(it) } as U

    fun toDTOs(block: () -> List<T>): List<U> =
        block().let { it.map { t -> converter.convertModelToDTO(t) } }

    fun toModel(block: () -> U?): T = block()?.let { converter.convertDTOToModel(it) } as T

    fun toModels(block: () -> List<U>): List<T> =
        block().let { it.map { u -> converter.convertDTOToModel(u) } }

    fun toStreamDTOs(block: () -> Stream<T>): Stream<U> = toDTOs { block().toList() }.stream()
}
