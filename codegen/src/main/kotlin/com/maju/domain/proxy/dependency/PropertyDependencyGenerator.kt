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
class PropertyDependencyGenerator() : AbstractDependencyGenerator() {

    private lateinit var repositoryClassName: ClassName
    private lateinit var converterClassNames: List<ClassName>
    private lateinit var componentModel: ComponentModel
    private var componentModelIsCDI = false

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


    override fun getDependency(
        repositoryClassName: ClassName,
        converterClassNames: List<ClassName>,
        componentModel: ComponentModel
    ): Dependency {
        this.repositoryClassName = repositoryClassName
        this.converterClassNames = converterClassNames
        this.componentModel = componentModel
        this.componentModelIsCDI = componentModel == ComponentModel.CDI
        return Dependency(
            constructor = null,
            getProperties().plus(getRepositoryProperty()),
            getAnnotations()
        )
    }



}
