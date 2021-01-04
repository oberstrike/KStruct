package core

import core.util.IConvertable
import core.util.IDTO
import core.util.IModel
import kotlin.reflect.KClass

fun initKStruct(block: KStruct.ConverterStruct.() -> Converter):
        Converter {
    return block(KStruct.ConverterStruct)
}

class Converter {
    fun convert(target: IConvertable): IConvertable? {
        if (target is IModel)
            return KStruct.convertModelToDTO(target)
        if(target is IDTO)
            return KStruct.convertDTOToModel(target)
        return null
    }
}

@Suppress("UNCHECKED_CAST")
object KStruct {

    val converter = Converter()

    data class ConverterWrapper<T : IModel, S : IDTO>(
        val converter: IConverter<T, S>,
        val modelClass: KClass<T>,
        val dtoClass: KClass<S>
    )

    private var pConverters = mutableListOf<ConverterWrapper<*, *>>()

    fun addConverters(converterWrapper: ConverterWrapper<*, *>) {
        pConverters.add(converterWrapper)
    }

    private fun findConverterBy(
        predicate: (ConverterWrapper<*, *>) -> Boolean
    ): ConverterWrapper<*, *>? {
        return pConverters.findLast(predicate = predicate)
    }

    private fun <T : IModel, U : IDTO> findConverterByModel(model: T): ConverterWrapper<T, U>? {
        return findConverterBy { it.modelClass == model::class } as? ConverterWrapper<T, U>
    }

    private fun <T : IModel, U : IDTO> findConverterByDTO(dto: U): ConverterWrapper<T, U>? {
        return findConverterBy { it.dtoClass == dto::class } as? ConverterWrapper<T, U>
    }


    internal fun <T : IModel, S : IDTO> convertModelToDTO(model: T): S {
        val converterWrapper = findConverterByModel<T, S>(model)
            ?: throw ConverterNotFoundException("There was no converter with the model class: ${model::class.simpleName} found")
        return converterWrapper.converter.convertModelToDTO(model)
    }

    internal fun <T : IModel, S : IDTO> convertDTOToModel(dto: S): T {
        val converterMap = findConverterByDTO<T, S>(dto)
            ?: throw ConverterNotFoundException("There was no converter with the dto class: ${dto::class.simpleName} found")

        return converterMap.converter.convertDTOToModel(dto)
    }


    object ConverterStruct {

        inline fun <reified T : IModel, reified S : IDTO> add(pConverter: IConverter<T, S>): Converter {
            val converterWrapper = ConverterWrapper(
                converter = pConverter,
                modelClass = T::class,
                dtoClass = S::class
            )
            addConverters(converterWrapper)
            return converter
        }
    }

}

