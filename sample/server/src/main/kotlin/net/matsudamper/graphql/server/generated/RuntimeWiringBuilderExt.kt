package net.matsudamper.graphql.server.generated

import graphql.schema.DataFetchingEnvironment
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import net.matsudamper.graphql.server.generated.type.QlUser
import net.matsudamper.graphql.server.generated.type.QlUserPostContent

public
    fun RuntimeWiring.Builder.setQuery(definitions: (env: DataFetchingEnvironment) -> QueryDefinitions):
    RuntimeWiring.Builder {
  type(
    TypeRuntimeWiring.newTypeWiring("Query")
      .dataFetcher("user") { env ->
        with(definitions(env)) {
          getQuery(env).getUser()
        }
      }
  )
  type(
    TypeRuntimeWiring.newTypeWiring("User")
      .dataFetcher("postContent") { env ->
        with(definitions(env)) {
          with(env.getSource<QlUser.BaseInterface>()){
            getUser(env).getPostContent()
          }
        }
      }
  )
  type(
    TypeRuntimeWiring.newTypeWiring("UserPostContent")
      .dataFetcher("user") { env ->
        with(definitions(env)) {
          with(env.getSource<QlUserPostContent.BaseInterface>()){
            getUserPostContent(env).getUser()
          }
        }
      }
  )
  return this
}
