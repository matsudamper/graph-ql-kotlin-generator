package net.matsudamper.graphql.generator.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNamedSchemaElement
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLType

/**
 * Kotlinのリスト、型、Nullableの構造を表す
 */
internal sealed class KotlinTypeStruct {
    abstract val isNull: Boolean

    data class ListType(
        override val isNull: Boolean,
        val wrappedType: KotlinTypeStruct,
    ) : KotlinTypeStruct()

    data class CoreType(
        override val isNull: Boolean,
        val namedType: GraphQLNamedSchemaElement,
        val type: GraphQLType,
    ) : KotlinTypeStruct()

    fun changed(isNull: Boolean): KotlinTypeStruct {
        return when (this) {
            is ListType -> this.copy(isNull = isNull)
            is CoreType -> this.copy(isNull = isNull)
        }
    }

    fun getCoreNamedObject(): GraphQLNamedSchemaElement {
        return when (this) {
            is ListType -> this.wrappedType.getCoreNamedObject()
            is CoreType -> this.namedType
        }
    }

    fun expand(): List<KotlinTypeStruct> {
        return mutableListOf<KotlinTypeStruct>().also { expand(it) }.toList()
    }

    private fun expand(result: MutableList<KotlinTypeStruct>) {
        result.add(this)

        when (this) {
            is ListType -> {
                wrappedType.expand(result)
            }
            is CoreType -> return
        }
    }

    fun getTypeName(typeDefinition: TypeDefinition): TypeName {
        val result = mutableListOf<KotlinTypeStruct>().also { expand(it) }.toList()
        var type: TypeName? = null
        result.reversed().forEach { element ->
            type = when (element) {
                is ListType -> {
                    ClassNames.List(type!!).copy(nullable = element.isNull)
                }
                is CoreType -> {
                    typeDefinition.getClassName(element.namedType.name).copy(nullable = element.isNull)
                }
            }
        }

        return type!!
    }

    fun getTypeName(coreTypeBuilder: (CoreType) -> ClassName): TypeName {
        val result = mutableListOf<KotlinTypeStruct>().also { expand(it) }.toList()
        var type: TypeName? = null
        result.reversed().forEach { element ->
            type = when (element) {
                is ListType -> {
                    ClassNames.List(type!!).copy(nullable = element.isNull)
                }
                is CoreType -> {
                    coreTypeBuilder(element).copy(nullable = element.isNull)
                }
            }
        }

        return type!!
    }

    companion object {
        /**
         * GraphQLをKotlinの構造で表現する
         */
        fun fromGraphQlType(type: GraphQLType): KotlinTypeStruct {
            return when (type) {
                is GraphQLNonNull -> {
                    fromGraphQlType(type.wrappedType).changed(isNull = false)
                }
                is GraphQLNamedSchemaElement -> {
                    CoreType(
                        isNull = true,
                        namedType = type,
                        type = type,
                    )
                }
                is GraphQLList -> {
                    ListType(
                        isNull = true,
                        wrappedType = fromGraphQlType(type.wrappedType),
                    )
                }
                else -> TODO("${type::class.java}")
            }
        }
    }
}
