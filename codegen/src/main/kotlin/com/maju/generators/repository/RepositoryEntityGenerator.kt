package com.maju.generators.repository

import com.maju.entities.ConverterEntity
import com.maju.entities.MethodEntity
import com.maju.entities.RepositoryEntity
import com.maju.utils.IGenerator
import com.maju.utils.CKType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
class RepositoryEntityGenerator(
    private val name: String,
    private val modelClass: CKType,
    private val dtoClass: CKType,
    private val type: CKType,
    private val converter: ConverterEntity,
    private val methods: List<MethodEntity>
) : IGenerator<RepositoryEntity> {

    override fun generate(): RepositoryEntity {
        return RepositoryEntity(
            name = name,
            modelClass = modelClass,
            dtoClass = dtoClass,
            type = type,
            converter = converter,
            methods = methods
        )
    }
}
