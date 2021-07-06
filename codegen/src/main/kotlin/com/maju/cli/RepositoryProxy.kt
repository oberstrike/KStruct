package com.maju.cli

import com.maju.generators.repository.proxy.dependency.DefaultDependencyGenerator
import com.maju.generators.repository.proxy.dependency.DependencyGenerator
import com.maju.generators.repository.proxy.dependency.PropertyDependencyGenerator
import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class RepositoryProxy(
    val converters: Array<KClass<*>>,
    val componentModel: ComponentModel = ComponentModel.DEFAULT,
    val injectionStrategy: InjectionStrategy = InjectionStrategy.DEFAULT
)

enum class InjectionStrategy(val dependencyGenerator: DependencyGenerator) {
    DEFAULT(DefaultDependencyGenerator()),
    CONSTRUCTOR(DefaultDependencyGenerator()),
    PROPERTY(PropertyDependencyGenerator());
}

enum class ComponentModel{
    DEFAULT, CDI, PROPERTY
}


