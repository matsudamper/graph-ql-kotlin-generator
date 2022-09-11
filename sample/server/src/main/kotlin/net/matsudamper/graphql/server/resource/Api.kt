package net.matsudamper.graphql.server.resource

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@Path("/")
class Api {

    @GET
    @Path("healthz")
    fun healthz(): String = "ok"
}