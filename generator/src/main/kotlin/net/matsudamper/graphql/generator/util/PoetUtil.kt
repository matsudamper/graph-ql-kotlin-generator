package net.matsudamper.graphql.generator.util

import com.squareup.kotlinpoet.*

internal fun TypeSpec.Builder.addPrimaryConstructor(
    items: List<PoetUtil.PrimaryConstructorElement>,
    primaryConstructorBuilder: (FunSpec.Builder.() -> Unit)? = null
): TypeSpec.Builder {
    val builder: TypeSpec.Builder = this
    return builder
        .addProperties(
            items.filter { it.isProperty }.map {
                PropertySpec.builder(it.name, it.typeName)
                    .initializer(it.name)
                    .also { builder ->
                        if (it.override) {
                            builder.addModifiers(KModifier.OVERRIDE)
                        }
                        if (it.isPrivate) {
                            builder.addModifiers(KModifier.PRIVATE)
                        }
                    }
                    .build()
            }
        )
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .also {
                    primaryConstructorBuilder?.invoke(it)
                }
                .addParameters(
                    items.map {
                        ParameterSpec.builder(it.name, it.typeName)
                            .build()
                    }
                )
                .build()
        )
}


internal object PoetUtil {
    data class PrimaryConstructorElement(
        val name: String,
        val typeName: TypeName,
        val override: Boolean,
        val isProperty: Boolean,
        val isPrivate: Boolean,
    )

    fun typeSpecToClassName(typeSpec: TypeSpec): ClassName {
        return ClassName(
            typeSpec.name!!.split(".").dropLast(1).joinToString("."),
            typeSpec.name!!.split(".").takeLast(1),
        )
    }

    fun getClassName(type: TypeName): ClassName {
        return when (type) {
            is ClassName -> type
            is Dynamic -> TODO()
            is LambdaTypeName -> TODO()
            is ParameterizedTypeName -> type.rawType
            is TypeVariableName -> TODO()
            is WildcardTypeName -> TODO()
        }
    }
}