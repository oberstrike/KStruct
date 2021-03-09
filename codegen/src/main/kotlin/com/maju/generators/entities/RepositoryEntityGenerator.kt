package com.maju.generators.entities

import com.maju.entities.ConverterEntity
import com.maju.entities.MethodEntity
import com.maju.entities.RepositoryEntity
import com.maju.generators.repository.IGenerator
import com.maju.utils.CKType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
class RepositoryEntityGenerator(
    private val name: String,
    private val type: CKType,
    private val converters: List<ConverterEntity>,
    private val methods: List<MethodEntity>,
) : IGenerator<RepositoryEntity> {

    override fun generate(): RepositoryEntity {
        return RepositoryEntity(
            name = name,
            type = type,
            converters = converters,
            methods = methods
        )
    }
}
