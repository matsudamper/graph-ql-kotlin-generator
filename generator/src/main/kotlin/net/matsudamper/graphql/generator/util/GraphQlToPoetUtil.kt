package net.matsudamper.graphql.generator.util

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.schema.*

internal object GraphQlToPoetUtil {

    private fun getDataFetcherTypeName(argument: TypeName): TypeName {
        return ClassName("graphql.schema", "DataFetcher")
            .parameterizedBy(argument)
    }

    fun createEnumSpec(name: String, value: GraphQLEnumType, typeDefine: TypeDefinition): TypeSpec {
        val thisType = typeDefine.getClassName(name)
        return TypeSpec.enumBuilder(thisType).also { builder ->
            value.values.associate { it.name to it.value }.map {
                builder.addEnumConstant(name = it.key)
            }
            builder.addType(
                TypeSpec.companionObjectBuilder()
                    .addFunction(
                        FunSpec.builder("fromInput")
                            .addParameter(ParameterSpec("name", ClassNames.String.copy(nullable = true)))
                            .addModifiers(KModifier.INTERNAL)
                            .returns(thisType.copy(nullable = true))
                            .addCode(
                                CodeBlock.builder().apply {
                                    addStatement("name ?: return null")
                                    addStatement("return values().firstOrNull { it.name == name }")
                                }.build()
                            )
                            .build()
                    )
                    .build()
            )
        }.build()
    }

    fun createValueClass(name: String, type: ClassName): TypeSpec {
        return TypeSpec.valueClassBuilder(name)
            .addAnnotation(ClassName("kotlin.jvm", "JvmInline"))
            .addProperty(
                PropertySpec.builder("value", type)
                    .initializer("value")
                    .build()
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("value", type)
                    .build()
            )
            .build()
    }

    fun getTypeName(namedObject: KotlinTypeStruct, typeDefine: TypeDefinition): TypeName {
        return when (namedObject) {
            is KotlinTypeStruct.ListType -> {
                ClassNames.List(getTypeName(namedObject.wrappedType, typeDefine))
                    .copy(nullable = namedObject.isNull)
            }
            is KotlinTypeStruct.CoreType -> {
                typeDefine.getClassName(namedObject.namedType.name)
                    .copy(nullable = namedObject.isNull)
            }
        }
    }


    fun parseList(
        type: GraphQLType,
        option: ParseListOption,
    ): CodeBlock {
        val codeBlock = CodeBlock.builder()
        val kotlinFieldElement = KotlinTypeStruct.fromGraphQlType(type)
        kotlinFieldElement.expand().also {
            val isList = it.any { it is KotlinTypeStruct.ListType }
            it.forEachIndexed { index, value ->
                when (value) {
                    is KotlinTypeStruct.CoreType -> {
                        if (isList) {
                            option.listObject.coreBlock(codeBlock, value)
                        } else {
                            option.singleObject.coreBlock(codeBlock, value)
                        }
                    }
                    is KotlinTypeStruct.ListType -> {
                        codeBlock.apply {
                            if (index == 0) {
                                val thisObject = option.listObject.receiver(it.first())
                                add("($thisObject as %T)", ClassNames.List(ClassNames.Any).copy(nullable = value.isNull))
                            } else {
                                add("(it as %T)", ClassNames.List(ClassNames.Any).copy(nullable = value.isNull))
                            }

                            if (value.isNull) {
                                add("?")
                            }
                            beginControlFlow(".map {")
                        }
                    }
                }
            }
            val listNestedCount = it.filterIsInstance<KotlinTypeStruct.ListType>()
            if (listNestedCount.isNotEmpty()) {
                codeBlock.apply {
                    listNestedCount.drop(1).forEach {
                        endControlFlow()
                    }
                    unindent()
                    add("}")
                }
            }
        }

        return codeBlock.build()
    }

    class ParseListOption(
        val singleObject: SingleObject,
        val listObject: ListObject,
    ) {
        data class SingleObject(
            val coreBlock: CodeBlock.Builder.(KotlinTypeStruct.CoreType) -> Unit
        )

        data class ListObject(
            val receiver: (KotlinTypeStruct) -> String,
            val coreBlock: CodeBlock.Builder.(KotlinTypeStruct.CoreType) -> Unit
        )
    }

}