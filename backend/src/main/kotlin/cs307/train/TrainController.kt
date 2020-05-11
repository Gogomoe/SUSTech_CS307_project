package cs307.train

import cs307.CoroutineController
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.format.toLocalDate
import cs307.train.timetable.toJson
import cs307.user.getUser
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

        router.post("/train").coroutineHandler(::addTrain)
        router.delete("/train/:train").coroutineHandler(::deleteTrain)

        router.get("/train/transship/from/:from/to/:to/date/:date").coroutineHandler(::getTransshipTrainBetween)
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

    suspend fun addTrain(context: RoutingContext) {
        val user = context.getUser() ?: throw ServiceException("not login")
        if (!user.isAuthorizedAwait("admin")) {
            throw ServiceException("permission denied")
        }
        val body = context.bodyAsJson
        val static = body.getInteger("static")?.toInt()
                ?: throw ServiceException("static is empty")
        val date = body.getString("date")?.toLocalDate()
                ?: throw ServiceException("date is empty")

        val trainID = service.addTrain(static, date)

        context.success(jsonObject = jsonObjectOf(
                "train" to trainID
        ))
    }

    suspend fun deleteTrain(context: RoutingContext) {
        val user = context.getUser() ?: throw ServiceException("not login")
        if (!user.isAuthorizedAwait("admin")) {
            throw ServiceException("permission denied")
        }

        val trainID = context.request().getParam("train")!!.toInt()

        service.deleteTrain(trainID)

        context.success()
    }

    suspend fun getTransshipTrainBetween(context: RoutingContext){
        val from = context.request().getParam("from")
        val to = context.request().getParam("to")
        val datePara = context.request().getParam("date")
        val date = try {
            datePara.toLocalDate()
        } catch (e: Exception) {
            throw ServiceException(e)
        }
        val result = service.searchTransshipTrainBetween(from,to,date)

        context.success(
                jsonObjectOf(
                        "transships" to JsonArray(result.map {
                            it.toJson()
                        })
                )
        )

    }
}


