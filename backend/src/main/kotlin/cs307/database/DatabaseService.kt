package cs307.database

import cs307.Service
import cs307.ServiceRegistry
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.ext.sql.closeAwait

class DatabaseService : Service {

    private lateinit var config: JsonObject

    private lateinit var client: JDBCClient

    override suspend fun start(registry: ServiceRegistry) {
        config = registry.context().config().getJsonObject("database_config")
        client = JDBCClient.createShared(registry.vertx(), config)
    }

    override suspend fun stop() {
        client.closeAwait()
    }

    fun client(): JDBCClient = client

    fun config(): JsonObject = config

}