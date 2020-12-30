package codegen

import codegen.utils.Type


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Converter

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class RepositoryProxy

data class RepositoryProxyEntity(
    val modelClass: Type,
    val dtoClass: Type,
    val type: Type,
    val methods: List<ProxyMethodEntity>
)


data class ProxyMethodEntity(
    val name: String,
    val parameters: List<ProxyParameterEntity>,
    val returnType: Type
)

data class ProxyParameterEntity(
    val name: String,
    val type: Type
)
