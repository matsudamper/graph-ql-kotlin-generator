package net.matsudamper.graphql.generator.poetbuilder

import com.squareup.kotlinpoet.*
import net.matsudamper.graphql.generator.util.ClassNames

internal class RuntimeWiringBuilderBuilder(
    private val queries: List<QueryDefinitionBuilder.QueryBuilderInfo>,
) {
    private val fileSpec = FileSpec.builder(ClassNames.basePackage, "RuntimeWiringBuilderExt")
    private val definitionsArgumentName = "definitions"

    init {
        fileSpec
            .addFunction(
                createFunction()
                    .addCode(
                        CodeBlock.builder().apply {
                            queries.forEach { query ->
                                query.fields.forEach { field ->
                                    addStatement("type(")
                                    withIndent {
                                        addStatement(
                                            """%T.newTypeWiring("${query.queryName}")""",
                                            ClassNames.TypeRuntimeWiring.getClassName()
                                        )
                                        withIndent {
                                            addStatement(""".dataFetcher("${field.fieldName}") { env ->""")
                                            withIndent {
                                                addStatement("with(${definitionsArgumentName}(env)) {")
                                                withIndent {
                                                    skippableFlow(
                                                        skip = query.isQuery || query.isMutation,
                                                        flow = CodeBlock.of(
                                                            "with(env.getSource<%T>())", query.rootClass
                                                        ),
                                                    ) {
                                                        addStatement("""${query.queryKotlinName}(env).${field.fieldKotlinName}()""")
                                                    }
                                                }
                                                addStatement("}")
                                            }
                                            addStatement("}")
                                        }
                                    }
                                    addStatement(")")
                                }
                            }

                            addStatement("return this")
                        }.build()
                    )
                    .build()
            )
    }

    /**
     * fun RuntimeWiring.Builder.setQuery(definitions: (env: DataFetchingEnvironment) -> QueryDefinitions): RuntimeWiring.Builder {}
     */
    private fun createFunction(): FunSpec.Builder {
        return FunSpec.builder("setQuery")
            .receiver(ClassNames.RuntimeWiring.Builder.getClassName())
            .returns(ClassNames.RuntimeWiring.Builder.getClassName())
            .addParameter(
                ParameterSpec.builder(
                    definitionsArgumentName,
                    LambdaTypeName.get(
                        null,
                        listOf(
                            ParameterSpec.builder("env", ClassNames.DataFetchingEnvironment)
                                .build()
                        ),
                        ClassNames.QueryDefinitions.getClassNames()
                    )
                )
                    .build()
            )
    }

    private fun CodeBlock.Builder.skippableFlow(
        flow: CodeBlock,
        skip: Boolean,
        body: CodeBlock.Builder.() -> Unit
    ): CodeBlock.Builder {
        if (skip) {
            body()
        } else {
            add(flow)
            addStatement("{")
            withIndent {
                body()
            }
            addStatement("}")
        }
        return this
    }

    fun build() = fileSpec.build()
}