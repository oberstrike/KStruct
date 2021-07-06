package com.maju.domain.proxy.dependency

import com.maju.cli.ComponentModel
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


@KotlinPoetMetadataPreview
class PropertyDependencyGenerator(
    repositoryClassName: ClassName,
    converterClassNames: List<ClassName>,
    componentModel: ComponentModel
) : AbstractDependencyGenerator(
    repositoryClassName,
    converterClassNames,
    componentModel
) {

    private val componentModelIsCDI = componentModel == ComponentModel.CDI

    private fun getProperties(): List<PropertySpec> {
        return converterClassNames.map { converterClassName ->
            val name = converterClassName.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }
            property(name, converterClassName){
                addModifiers(KModifier.LATEINIT)
                if (componentModelIsCDI) addAnnotation(Inject::class)
                mutable(true)
            }
        }
    }

    private fun getRepositoryProperty(): PropertySpec {
        return property("repository", repositoryClassName) {
            addModifiers(KModifier.PRIVATE, KModifier.LATEINIT)
            if (componentModelIsCDI) addAnnotation(Inject::class)
            mutable(true)
        }
    }


    private fun getAnnotations(): List<AnnotationSpec>{
        return annotations {
            add(ClassInspectorUtil.createClassName("javax/enterprise/context/ApplicationScoped"))
        }
    }


    override fun getDependency(): Dependency {
        return Dependency(
            constructor = null,
            getProperties().plus(getRepositoryProperty()),
            getAnnotations()
        )
    }



}
