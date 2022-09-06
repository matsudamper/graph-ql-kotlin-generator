package net.matsudamper.graphql.generator.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

@Suppress("FunctionName")
internal object ClassNames {
    lateinit var basePackage: String
    lateinit var packageNames: PackageNames

    val Any = ClassName("kotlin", "Any")
    val String = ClassName("kotlin", "String")
    val Boolean = ClassName("kotlin", "Boolean")
    val DataFetchingEnvironment = ClassName("graphql.schema", "DataFetchingEnvironment")
    val RuntimeWiringBuilder = ClassName("graphql.schema.idl", "RuntimeWiring.Builder")
    val DataFetchingFieldSelectionSet = ClassName("graphql.schema", "DataFetchingFieldSelectionSet")
    val SelectedField = ClassName("graphql.schema", "SelectedField")

    fun Map(key: TypeName, value: TypeName): TypeName {
        return ClassName("kotlin.collections", "Map")
            .plusParameter(key)
            .plusParameter(value)
    }

    fun List(value: TypeName): TypeName {
        return ClassName("kotlin.collections", "List")
            .plusParameter(value)
    }

    fun CompletableStage(): ClassName {
        return ClassName("java.util.concurrent", "CompletionStage")
    }

    fun CompletableStage(value: TypeName): TypeName {
        return CompletableStage()
            .plusParameter(value)
    }

    object QlBaseObject {
        fun getClassName(name: String): ClassName {
            return ClassName(packageNames.typePackage, buildString {
                append("Ql")
                append(name.take(1).toUpperCase())
                append(name.drop(1))
            })
        }
    }

    object GraphQlNamedElement {
        fun getClassName(): ClassName {
            return net.matsudamper.graphql.generator.lib.GraphQlNamedElement::class.java.let {
                ClassName(it.packageName, it.simpleName)
            }
        }

        val graphqlName: String = net.matsudamper.graphql.generator.lib.GraphQlNamedElement::graphqlName.name
    }

    object ResponseScope {
        fun getClassName(variable: TypeName): TypeName {
            return net.matsudamper.graphql.generator.lib.ResponseScope::class.java.let {
                ClassName(it.packageName, it.simpleName)
                    .plusParameter(variable)
            }
        }
    }

    object QlQuery {
        fun getClassName(name: String): ClassName {
            val objectClassName = QlBaseObject.getClassName(name)
            return ClassName(
                objectClassName.packageName,
                objectClassName.simpleName,
                buildString {
                    append("Query")
                }
            )
        }
    }

    object RuntimeWiringName {
        object BuilderName {
            fun getClassName(): ClassName {
                return ClassName(
                    RuntimeWiring::class.java.packageName,
                    RuntimeWiring::class.java.simpleName,
                    RuntimeWiring.Builder::class.java.simpleName,
                )
            }
        }
    }

    object NamedObject {
        fun getClassName(name: String): ClassName {
            val objectClassName = QlBaseObject.getClassName(name)
            return ClassName(
                objectClassName.packageName,
                objectClassName.simpleName,
                "BaseNamedObject",
            )
        }
    }

    object BaseTypeInterface {
        fun getClassName(name: String): ClassName {
            return ClassName(
                packageNames.typePackage,
                buildString {
                    append("Ql")
                    append(name.take(1).toUpperCase())
                    append(name.drop(1))
                },
                buildString {
                    append("BaseInterface")
                }
            )
        }
    }

    object RuntimeWiring {
        object Builder {
            fun getClassName(): ClassName {
                return ClassName(
                    "graphql.schema.idl",
                    "RuntimeWiring",
                    "Builder",
                )
            }
        }
    }

    object TypeRuntimeWiring {
        fun getClassName(): ClassName {
            return ClassName(
                "graphql.schema.idl",
                "TypeRuntimeWiring"
            )
        }
    }

    object JavaxInjectProvider {
        fun getClassnames(parameter: ClassName): TypeName {
            return ClassName(
                javax.inject.Provider::class.java.packageName,
                javax.inject.Provider::class.java.simpleName
            ).plusParameter(parameter)
        }
    }

    object QueryDefinitions {
        fun getClassNames(): ClassName {
            return ClassName(
                basePackage,
                "QueryDefinitions"
            )
        }
    }

    private fun KClass<*>.toClassName() = java.let { ClassName(it.packageName, it.simpleName) }
}