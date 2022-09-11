package net.matsudamper.graphql.server

import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.servlet.ServletContainer

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val server = Server().also { server ->
                val httpConnector = ServerConnector(
                    server,
                    HttpConnectionFactory(
                        HttpConfiguration().also { config ->
                            config.sendServerVersion = false
                        }
                    )
                ).also { connector ->
                    connector.port = 80
                }

                server.connectors = listOf<Connector>(
                    httpConnector
                ).toTypedArray()



                server.requestLog = CustomRequestLog(
                    "logs/yyyy_mm_dd.access.log",
                    """"%{client}a %u %{yyyy-MM-dd HH:mm:ss.SSS|UTC}t %r %s"""",
                )

                val resourceConfig = GraphQlAppResourceConfig()

                val container = ServletContainer(resourceConfig)
                val holder = ServletHolder(container)

                ServletContextHandler(server, "/").also { context ->
                    context.addServlet(holder, "/*")
                }
            }

            server.start()
            server.join()
        }
    }
}