package cs307.train.trainline

import cs307.CoroutineController
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.format.toDuration
import cs307.user.getUser
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf

class TrainLineController(registry: ServiceRegistry) : CoroutineController() {

    val service: TrainLineService = registry[TrainLineService::class.java]

    override fun route(router: Router) {
        router.get("/train/static/:static/trainline").coroutineHandler(::getTrainLineStatic)
        router.post("/train/static").coroutineHandler(::addTrainLineStatic)
        router.delete("/train/static/:static").coroutineHandler(::deleteTrainLineStatic)
    }

    suspend fun getTrainLineStatic(context: RoutingContext) {
        val static = context.request().getParam("static").toInt()
        val result = service.getTrainLineStatic(static)
        context.success(jsonObject = jsonObjectOf(
                "trainline" to result.toJson()
        ))
    }

    suspend fun addTrainLineStatic(context: RoutingContext) {

        val user = context.getUser()
        val havePermission = user?.isAuthorizedAwait("admin") ?: false
        if (!havePermission) {
            throw ServiceException("permission denied")
        }

        val body = context.bodyAsJson
        val code = body.getString("code")
        val type = body.getString("type")
        val seat = body.getJsonObject("seat")
                ?: throw ServiceException("no seat information")
        val station = body.getJsonArray("stations")
                ?: throw ServiceException("no station information")

        val seatCount: Map<Int, Int> = seat.map
                .map { it.key.toInt() to it.value as Int }.toMap()
                .filterValues { it > 0 }

        val stations: List<TrainLineStaticStation> = station.list.map { item ->
            item as Map<*, *>
            fun Map<*, *>.getInteger(key: String): Int = this[key] as Int
            fun Map<*, *>.getString(key: String): String = this[key] as String
            fun Map<*, *>.getMap(key: String): Map<*, *> = this[key] as Map<*, *>
            TrainLineStaticStation(
                    item.getInteger("station"),
                    item.getString("arriveTime").toDuration(),
                    item.getString("departTime").toDuration(),
                    item.getMap("prices")
                            .map { (it.key as String).toInt() to it.value as Int }.toMap()
            )
        }

        val id = service.addTrainLineStatic(code, type, seatCount, stations)

        context.success(jsonObject = jsonObjectOf(
                "static" to id
        ))
    }

    suspend fun deleteTrainLineStatic(context: RoutingContext) {

        val user = context.getUser()
        val havePermission = user?.isAuthorizedAwait("admin") ?: false
        if (!havePermission) {
            throw ServiceException("permission denied")
        }

        val static = context.request().getParam("static").toInt()
        service.removeTrainLineStatic(static)
        context.success()
    }


}