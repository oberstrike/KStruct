package com.maju.domain.proxy.dependency

import com.maju.cli.ComponentModel
import com.maju.cli.InjectionStrategy
import com.maju.utils.APPLICATION_SCOPED
import com.maju.utils.firstCharToLower
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import org.springframework.beans.factory.annotation.Autowired
import javax.inject.Inject


@KotlinPoetMetadataPreview
class PropertyDependencyGenerator() : AbstractDependencyGenerator() {

    private lateinit var repositoryClassName: ClassName
    private lateinit var converterClassNames: List<ClassName>
    private lateinit var componentModel: ComponentModel

    private var componentModelIsCDI = false
    private var componentModelIsSpring = false

    override val injectionStrategies: List<InjectionStrategy>
        get() = listOf(InjectionStrategy.PROPERTY)

    private fun getProperties(): List<PropertySpec> {
        return converterClassNames.map { converterClassName ->
            val name = converterClassName.simpleName.firstCharToLower()
            property(name, converterClassName) {
                addModifiers(KModifier.LATEINIT)
                if (componentModelIsCDI) addAnnotation(Inject::class)
                if (componentModelIsSpring) addAnnotation(Autowired::class)
                mutable(true)
            }
        }
    }

    private fun getRepositoryProperty(): PropertySpec {
        return property(repositoryVarName, repositoryClassName) {
            addModifiers(KModifier.PRIVATE, KModifier.LATEINIT)
            if (componentModelIsCDI) addAnnotation(Inject::class)
            if (componentModelIsSpring) addAnnotation(Autowired::class)
            mutable(true)
        }
    }


    private fun getAnnotations(): List<AnnotationSpec> {
        return annotations {
            add(APPLICATION_SCOPED)
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
        this.componentModelIsCDI = componentModel == ComponentModel.CDI
        this.componentModelIsSpring = componentModel == ComponentModel.SPRING_CDI

        return Dependency(
            constructor = null,
            getProperties().plus(getRepositoryProperty()),
            getAnnotations()
        )
    }


}
