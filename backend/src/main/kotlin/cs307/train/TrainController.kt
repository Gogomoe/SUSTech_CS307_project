package cs307.train

import cs307.CoroutineController
import cs307.ServiceRegistry
import cs307.format.toLocalDate
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import moe.gogo.ServiceException

class TrainController(registry: ServiceRegistry) : CoroutineController() {

    val service = registry[TrainService::class.java]

    override fun route(router: Router) {
        router.get("/train/from/:from/to/:to/date/:date").coroutineHandler(::getActiveTrainBetween)
    }

    suspend fun getActiveTrainBetween(context: RoutingContext) {
        val from = context.request().getParam("from")
        val to = context.request().getParam("to")
        val datePara = context.request().getParam("date")
        val date = try {
            datePara.toLocalDate()
        } catch (e: Exception) {
            throw ServiceException(e)
        }
        val result = service.searchActiveTrainBetween(from, to, date)

        context.success(
                jsonObjectOf(
                        "trains" to JsonArray(result.map {
                            it.toJson()
                        })
                )
        )
    }

}


