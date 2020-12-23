package core

import kotlin.reflect.KClass

interface IModel

interface IDTO

interface IConverter<T : IModel, S : IDTO> {
    fun convertModelToDTO(model: T): S
}

fun converters(function: KStruct.ConverterStruct.() -> KStruct.ConverterStruct): KStruct.ConverterStruct {
    return function(KStruct.ConverterStruct)
}

object KStruct {

    data class ConverterWrapper<T : IModel, S : IDTO>(
        val converter: IConverter<T, S>,
        val modelClass: KClass<T>,
        val dtoClass: KClass<S>
    )


    private var pConverters = mutableListOf<ConverterWrapper<*, *>>()

    fun init(converterStruct: ConverterStruct) {
        pConverters.addAll(converterStruct.converters)
    }

    private fun <T : IModel, U : IDTO> findConverterByModel(model: IModel): ConverterWrapper<T, U>? {
        val last = pConverters.findLast {
            it.modelClass == model::class
        }
        @Suppress("UNCHECKED_CAST")
        return last as? ConverterWrapper<T, U>
    }

    fun <T : IModel, S : IDTO> convertModelToDTO(model: T): S {
        val converterWrapper = findConverterByModel<T, S>(model)
            ?: throw ConverterNotFoundException("There was no converter with the model class: ${model::class.simpleName} found")

        return converterWrapper.converter.convertModelToDTO(model)
    }

    object ConverterStruct {

        val converters = mutableListOf<ConverterWrapper<*, *>>()

        inline fun <reified T : IModel, reified S : IDTO> add(converter: IConverter<T, S>): ConverterStruct {
            val converterWrapper = ConverterWrapper(
                converter = converter,
                modelClass = T::class,
                dtoClass = S::class
            )

            return apply {
                converters.add(converterWrapper)
            }
        }
    }

}

