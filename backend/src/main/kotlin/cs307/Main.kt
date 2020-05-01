package cs307

import cs307.web.WebServerVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val dbConfig = JsonObject(this::class.java.classLoader.getResource("database_config.json")!!.readText())
        val webConfig = JsonObject(this::class.java.classLoader.getResource("webserver_config.json")!!.readText())

        val config = jsonObjectOf(
                "database_config" to dbConfig,
                "webserver_config" to webConfig
        )
        val option = DeploymentOptions().setConfig(config)

        val vertx = Vertx.vertx()
        vertx.deployVerticle(WebServerVerticle(), option).setHandler {
            if (it.failed()) {
                it.cause().printStackTrace()
                vertx.close()
            }
        }
    }
}
