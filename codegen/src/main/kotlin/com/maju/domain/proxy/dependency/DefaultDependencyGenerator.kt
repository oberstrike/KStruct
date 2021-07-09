package com.maju.domain.proxy.dependency

import com.maju.cli.ComponentModel
import com.maju.cli.InjectionStrategy
import com.maju.utils.APPLICATION_SCOPED
import com.maju.utils.firstCharToLower
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
class DefaultDependencyGenerator() : AbstractDependencyGenerator() {

    private lateinit var repositoryClassName: ClassName
    private lateinit var converterClassNames: List<ClassName>
    private lateinit var componentModel: ComponentModel

    override val injectionStrategies: List<InjectionStrategy>
        get() = listOf(InjectionStrategy.CONSTRUCTOR, InjectionStrategy.DEFAULT)


    private fun getConstructor(): FunSpec {
        return FunSpec.constructorBuilder()
            .apply {
                for (converterClassName in converterClassNames) {
                    addParameter(
                        ParameterSpec.builder(
                            converterClassName.simpleName.firstCharToLower(),
                            converterClassName
                        )
                            .build()
                    )
                }
            }.addParameter(
                ParameterSpec.builder(repositoryVarName, repositoryClassName)
                    .build()
            )
            .build()
    }

    private fun getProperties(): List<PropertySpec> {
        return converterClassNames.map { converterClassName ->
            val name = converterClassName.simpleName.firstCharToLower()
            property(name, converterClassName) {
                mutable(false)
                initializer(name)
            }
        }

    }

    private fun getRepositoryProperty(): PropertySpec {
        return property(repositoryVarName, repositoryClassName) {
            mutable(false)
            initializer(repositoryVarName)
        }
    }


    private fun getAnnotations(): List<AnnotationSpec> {
        return annotations {
            if (componentModel == ComponentModel.CDI) {
                add(APPLICATION_SCOPED)
            }
        }
    }

    override fun getDependency(
        repositoryClassName: ClassName,
        converterClassNames: List<ClassName>,
        componentModel: ComponentModel
    ): Dependency {
        this.repositoryClassName = repositoryClassName
        this.converterClassNames = converterClassNames
        this.componentModel = componentModel

        return Dependency(
            getConstructor(),
            getProperties().plus(getRepositoryProperty()),
            getAnnotations()
        )
    }

}
