package com.maju.domain.entities

import com.maju.domain.generator.ConverterEntity
import com.maju.domain.generator.MethodEntity
import com.maju.domain.generator.ExtensionEntity
import com.maju.domain.generator.RepositoryEntity
import com.maju.utils.IGenerator
import com.maju.utils.CKType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
class RepositoryEntityGenerator(
    private val name: String,
    private val type: CKType,
    private val converters: List<ConverterEntity>,
    private val methods: List<MethodEntity>,
    private val panacheEntity: ExtensionEntity?,
) : IGenerator<RepositoryEntity> {

    override fun generate(): RepositoryEntity {
        return RepositoryEntity(
            name = name,
            type = type,
            converters = converters,
            methods = methods,
            panacheEntity = panacheEntity
        )
    }
}
