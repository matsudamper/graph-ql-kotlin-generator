package net.matsudamper.graphql.generator.util

import com.squareup.kotlinpoet.ClassName
import graphql.schema.*
import kotlin.reflect.KClass

internal class PackageNames(
    val basePackage: String,
    val libPackage: String,
    val bridgePackage: String,
    val runtimeWiringPackage: String,
    val typePackage: String,
    val unionPackage: String,
    val queryPackage: String,
    val inputPackage: String,
    val enumPackage: String,
)

/**
 * 生成する型情報はここを元とする
 */
internal class TypeDefinition(
    val packageNames: PackageNames,
    map: Map<String, GraphQLNamedType>,
    private val scalarMap: Map<String, String>,
) {
    /**
     * GraphQLとJVMで1:1になる型
     */
    private val javaType: Map<String, JavaPoetMap> = mapOf(
        "Boolean" to JavaPoetMap(Boolean::class, ClassNames.Boolean),
        "String" to JavaPoetMap(String::class, ClassNames.String),
    )

    private val scalarType: MutableMap<String, GraphQLScalarType> = mutableMapOf()
    val objectType: MutableMap<String, GraphQLObjectType> = mutableMapOf()
    val enumType: MutableMap<String, GraphQLEnumType> = mutableMapOf()
    val inputType: MutableMap<String, GraphQLInputObjectType> = mutableMapOf()
    private val unionType: MutableMap<String, GraphQLUnionType> = mutableMapOf()

    val unionElements: MutableMap<String, Union> = mutableMapOf()

    init {
        println("=============scalarMap==============")
        scalarMap.onEach {
            println(it)
        }
        println()
        println()
        map.filterNot { it.key.startsWith("__") }.forEach { (key, value) ->
            println(value)
            when (value) {
                is GraphQLScalarType -> {
                    scalarType[key] = value
                }
                is GraphQLObjectType -> {
                    objectType[key] = value
                }
                is GraphQLEnumType -> {
                    enumType[key] = value
                }
                is GraphQLInputObjectType -> {
                    inputType[key] = value
                }
                is GraphQLUnionType -> {
                    unionType[value.name] = value
                }
                is GraphQLInterfaceType -> {

                }
                else -> throw NotImplementedError("this type not supported yet. [${value::class.java}]")
            }
        }

        unionElements.putAll(
            unionType.map { (name, value) ->
                val union = getClassName(name)
                val bridgeImplName = ClassName(packageNames.bridgePackage, "${union.simpleName}BridgeImpl")
                name to Union(
                    graphQlUnionName = name,
                    bridgeImplName = bridgeImplName,
                    unionName = union,
                    child = value.types.associate {
                        it.name to Union.Child(
                            graphQlName = it.name,
                            type = ClassName(packageNames.queryPackage, union.simpleName, it.name),
                            bridgeImplChildName = ClassName(
                                bridgeImplName.packageName,
                                bridgeImplName.simpleName,
                                it.name
                            ),
                            rawType = getClassName(it.name),
                        )
                    }
                )
            }
        )
    }

    fun getUnionClassNames(graphQlName: String): List<ClassName> {
        return unionType[graphQlName]!!.types.map { getClassName(it.name) }
    }

    /**
     * ラップされていない素のTypeを返す
     */
    fun getClassName(graphQlQueryName: String): ClassName {
        return sequence {
            yield(javaType[graphQlQueryName]?.className)
            yield(
                scalarType[graphQlQueryName]?.let {
                    val targetType = scalarMap[graphQlQueryName]
                        ?: throw IllegalArgumentException("cannot mapping scalar type. [$graphQlQueryName]")

                    ClassName(
                        packageName = targetType.split(".").dropLast(1).joinToString("."),
                        targetType.split(".").last(),
                    )
                }
            )
            yieldAll(
                sequence {
                    yield(unionType[graphQlQueryName]?.let {
                        ClassName(
                            packageNames.unionPackage,
                            Prefix.plus(it.name.plus("Union"))
                        )
                    })
                    yield(objectType[graphQlQueryName]?.let { ClassNames.BaseTypeInterface.getClassName(it.name) })
                    yield(enumType[graphQlQueryName]?.let { ClassName(packageNames.enumPackage, Prefix.plus(it.name)) })
                    yield(inputType[graphQlQueryName]?.let { ClassName(packageNames.inputPackage, Prefix.plus(it.name)) })
                }.filterNotNull()
            )
        }.filterNotNull()
            .firstOrNull() ?: throw IllegalArgumentException("Not Found. [${graphQlQueryName}]")
    }

    /**
     * QueryでreturnするNamedObjectかScalarTypeを返す
     */
    fun getQueryReturnType(type: GraphQLNamedSchemaElement): ClassName {
        return when (type) {
            is GraphQLScalarType,
            is GraphQLEnumType -> {
                getClassName(type.name)
            }
            else -> {
                ClassNames.NamedObject.getClassName(type.name)
            }
        }
    }

    data class Union(
        val graphQlUnionName: String,
        val bridgeImplName: ClassName,
        val unionName: ClassName,
        val child: Map<String, Child>
    ) {
        data class Child(
            val graphQlName: String,
            val bridgeImplChildName: ClassName,
            val type: ClassName,
            val rawType: ClassName
        )
    }

    data class JavaPoetMap(
        val kClass: KClass<*>,
        val className: ClassName,
    )

    companion object {
        private const val Prefix = "Ql"
    }
}