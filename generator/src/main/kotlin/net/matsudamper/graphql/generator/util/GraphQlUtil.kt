package net.matsudamper.graphql.generator.util

import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import net.matsudamper.graphql.generator.Constant

internal object GraphQlUtil {
    fun isFieldQuery(type: GraphQLFieldDefinition): Boolean {
        return type.hasDirective(Constant.Directive.lazy) || type.arguments.isNotEmpty()
    }

    fun getFieldQueryTypes(type: GraphQLObjectType): List<GraphQLFieldDefinition> {
        val result = type.fields.asSequence()
            .filter { isFieldQuery(it) }
            .map { it to KotlinTypeStruct.fromGraphQlType(it.type) }
            .sortedBy { (_, it) -> it.getCoreNamedObject().name }
            .map { it.first }
            .toList()

        return result
    }
}