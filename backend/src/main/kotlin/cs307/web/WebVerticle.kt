package cs307.web

import cs307.api.APIServerVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class WebServerVerticle : CoroutineVerticle() {

    override suspend fun start() = coroutineScope {

        val server = vertx.createHttpServer()
        val router = Router.router(vertx)

        launch {
            val api = APIServerVerticle()
            vertx.deployVerticleAwait(api, DeploymentOptions().setConfig(config))
            router.mountSubRouter("/api", api.router())
        }

        val defaultAvatar = WebServerVerticle::class.java.classLoader
                .getResourceAsStream("default.jpg")!!.readAllBytes()

        router.get("/avatar/default.jpg").handler { context ->
            context.response().putHeader("Content-Type", "image/jpeg")
            context.end(Buffer.buffer(defaultAvatar))
        }

        val webConfig = config.getJsonObject("webserver_config")

        server.requestHandler(router).listenAwait(
                webConfig.getInteger("port"),
                webConfig.getString("host")
        )

        Unit

    }

}