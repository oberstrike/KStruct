package core.util

import core.Converter

interface RepositoryProxy<T : Any, U> {
    val converter: Converter
}

@Suppress("UNCHECKED_CAST")
abstract class AbstractRepositoryProxy<T : IModel, U : IDTO> : RepositoryProxy<T, U> {

    fun compute(block: (T) -> T?, subject: U): U? {
        val model = converter.convert(subject) ?: return null
        return block(model as T)?.let { converter.convert(it) } as? U
    }

    fun <S> compute(block: () -> S?): S? {
        return block()
    }

    fun toDTO(block: () -> T?): U = block()?.let { converter.convert(it) } as U

    fun toModel(block: () -> U?): T = block()?.let { converter.convert(it) } as T

}
