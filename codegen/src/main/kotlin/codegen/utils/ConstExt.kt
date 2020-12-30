package codegen.utils

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import kotlinx.metadata.KmClassifier
import kotlin.reflect.KClass


@KotlinPoetMetadataPreview
fun ImmutableKmType.className(): ClassName {
    return ClassInspectorUtil.createClassName((classifier as KmClassifier.Class).name)
}


@KotlinPoetMetadataPreview
fun ImmutableKmType.toType(): Type {
    return Type(
        className = className(),
        isNullable = isNullable,
        arguments = arguments.filter { it.type != null }.map { it.type!!.toType() }
    )
}

fun ClassName.toType(): Type {
    return Type(
        className = this,
        isNullable = isNullable
    )
}

@KotlinPoetMetadataPreview
fun <T : Any> KClass<T>.toType(): Type {
    return ClassInspectorUtil.createClassName(toImmutableKmClass().name).toType()
}

fun Type.toParameterizedTypeName(): TypeName {
    val rValue = if (arguments.isEmpty()) className
    else className.parameterizedBy(arguments.map { each -> each.toParameterizedTypeName() })
    return rValue.copy(nullable = isNullable)
}

data class Type(
    val className: ClassName,
    val isNullable: Boolean = false,
    val arguments: List<Type> = listOf(),
)
