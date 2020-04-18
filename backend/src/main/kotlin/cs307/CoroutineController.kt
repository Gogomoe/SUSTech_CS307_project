package cs307

import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import moe.gogo.ServiceException

abstract class CoroutineController() : Controller {

    companion object {
        private val log = LoggerFactory.getLogger(CoroutineController::class.java)
    }

    val coroutineScope: CoroutineScope = CoroutineScope(CoroutineName("${this.javaClass.name}_scope"))

    open fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            coroutineScope.launch(ctx.vertx().dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: ServiceException) {
                    ctx.fail(e.message ?: "Unknown", 400)
                } catch (e: Exception) {
                    log.error("Error in ${this::class.java.name} coroutineHandler", e)
                    ctx.fail(e.message ?: "Unknown service error", 500)
                }
            }
        }
    }

    open fun RoutingContext.success(jsonObject: JsonObject = JsonObject(), statusCode: Int = 200) {
        response().statusCode = statusCode
        jsonObject.put("success", true)
        end(jsonObject.encode())
    }

    open fun RoutingContext.fail(message: String, statusCode: Int = 400) {
        response().statusCode = statusCode
        val obj = jsonObjectOf(
                "success" to false,
                "error" to jsonObjectOf("message" to message)
        )
        end(obj.encode())
    }

}