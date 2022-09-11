package net.matsudamper.graphql.server

import org.glassfish.jersey.server.ResourceConfig

class GraphQlAppResourceConfig : ResourceConfig() {
    init {
        packages("net.matsudamper.graphql")
    }
}