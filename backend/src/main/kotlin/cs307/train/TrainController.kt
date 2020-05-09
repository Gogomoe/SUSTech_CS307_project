package cs307.train

import cs307.CoroutineController
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.format.toLocalDate
import cs307.train.timetable.toJson
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf

class TrainController(registry: ServiceRegistry) : CoroutineController() {

    val service = registry[TrainService::class.java]

    override fun route(router: Router) {
        router.get("/train/from/:from/to/:to/date/:date").coroutineHandler(::getActiveTrainBetween)
        router.get("/train/:train/timetable").coroutineHandler(::getTrainTimeTable)
        router.get("/train/static/:static/timetable").coroutineHandler(::getTrainStaticTimeTable)
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

    suspend fun getTrainTimeTable(context: RoutingContext) {
        val train = context.request().getParam("train").toInt()
        val timetable = service.getTrainTimetableInfo(train)

        context.success(jsonObject = jsonObjectOf(
                "timetable" to timetable.toJson()
        ))
    }

    suspend fun getTrainStaticTimeTable(context: RoutingContext) {
        val static = context.request().getParam("static").toInt()
        val timetable = service.getTrainStaticTimetableInfo(static)

        context.success(jsonObject = jsonObjectOf(
                "timetable" to timetable.toJson()
        ))
    }

}


