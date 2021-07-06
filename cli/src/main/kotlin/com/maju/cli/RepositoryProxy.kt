package com.maju.cli

import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class RepositoryProxy(
    val converters: Array<KClass<*>>,
    val componentModel: ComponentModel = ComponentModel.DEFAULT,
    val injectionStrategy: InjectionStrategy = InjectionStrategy.DEFAULT
)


