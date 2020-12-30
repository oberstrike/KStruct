package codegen.generators.repository

import codegen.ProxyMethodEntity
import codegen.ProxyParameterEntity
import codegen.RepositoryProxyEntity
import codegen.utils.IGenerator
import codegen.utils.Type
import codegen.utils.toParameterizedTypeName
import codegen.utils.toType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import core.Converter
import core.util.AbstractRepositoryProxy

class RepositoryProxyGenerator(private val repositoryProxy: RepositoryProxyEntity) : IGenerator<FileSpec> {

    private val modelClass by lazy { repositoryProxy.modelClass }

    private val dtoClass by lazy { repositoryProxy.dtoClass }

    @KotlinPoetMetadataPreview
    override fun generate(): FileSpec {
        val fileSpecBuilder = FileSpec.builder("com.test", "name")


        val proxyClass = AbstractRepositoryProxy::class.toType().className
        val converterClass = Converter::class.toType().className


        val parameterizedBy = proxyClass.parameterizedBy(
            repositoryProxy.modelClass.toParameterizedTypeName(),
            repositoryProxy.dtoClass.toParameterizedTypeName()
        )

        val simpleName = repositoryProxy.type.className.simpleName

        val typeSpecBuilder = TypeSpec.classBuilder("${simpleName}Proxy")
        typeSpecBuilder.superclass(parameterizedBy)

        typeSpecBuilder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(
                    ParameterSpec
                        .builder("converter", converterClass)
                        .addModifiers(KModifier.OVERRIDE)
                        .build()
                ).addParameter(
                    ParameterSpec.builder("repository", repositoryProxy.type.className)
                        .build()
                )
                .build()
        )


        typeSpecBuilder.addProperty(
            PropertySpec.builder("converter", converterClass)
                .initializer("converter").build()
        )

        typeSpecBuilder.addProperty(
            PropertySpec.builder("repository", repositoryProxy.type.className)
                .addModifiers(KModifier.PRIVATE)
                .initializer("repository").build()
        )

        for (method in repositoryProxy.methods) {
            typeSpecBuilder.addFunction(
                generateFunSpec(method)
            )
        }

        fileSpecBuilder.addType(typeSpecBuilder.build())
        return fileSpecBuilder.build()

    }

    private fun generateFunSpec(proxyMethodEntity: ProxyMethodEntity): FunSpec {

        val name = proxyMethodEntity.name
        val returnType = proxyMethodEntity.returnType
        val parameters = proxyMethodEntity.parameters

        return FunSpec.builder(name)
            .returns(returnType.className)
            .apply {
                for (parameter in parameters) {
                    addParameter(generateParamSpec(parameter))
                }

                val statements = generateStatement(parameters, returnType)
                for (statement in statements) {
                    addStatement(statement)
                }


            }
            .build()
    }

    private fun generateStatement(params: List<ProxyParameterEntity>, returnType: Type): List<String> {
        val statements = mutableListOf<String>()
        val dtoParams = mutableListOf<String>()

        val dtoClasses = params.filter { it.type == dtoClass }
        val otherParams = params.filterNot { it.type == dtoClass }.map { it.name }

        for (dtoClass in dtoClasses) {
            val parName = dtoClass.name
            val toModel = "val ${parName}Model = toModel { $parName } )"
            statements.add(toModel)
            dtoParams.add("${parName}Model")
        }

        dtoParams.addAll(otherParams)
        val paramString = dtoParams.joinToString()

        val compute = "compute { $paramString }"
        if (returnType == dtoClass) {
            statements.add("return toDto { $compute } ")
        } else {
            statements.add("return $compute")
        }
        return statements
    }

    private fun generateParamSpec(proxyParameterEntity: ProxyParameterEntity): ParameterSpec {
        val name = proxyParameterEntity.name
        val type = proxyParameterEntity.type
        return ParameterSpec.builder(name, type.toParameterizedTypeName()).build()
    }

}
