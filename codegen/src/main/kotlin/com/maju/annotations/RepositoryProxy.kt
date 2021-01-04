package com.maju.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class RepositoryProxy(
    val converter: KClass<*>,
    val componentModel: String = "default",
    val injectionStrategy: InjectionStrategy = InjectionStrategy.CONSTRUCTOR
)

enum class InjectionStrategy {
    CONSTRUCTOR, PROPERTY
}


