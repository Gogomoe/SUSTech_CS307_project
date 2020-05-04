package cs307.train

import cs307.CoroutineController
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.user.getUser
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf

class StationController(registry: ServiceRegistry) : CoroutineController() {

    val service: StationService = registry[StationService::class.java]

    override fun route(router: Router) {
        router.get("/station/search/:search").coroutineHandler(::searchStation)
        router.post("/station").coroutineHandler(::addStation)
        router.delete("/station/:station").coroutineHandler(::deleteStation)
    }

    suspend fun searchStation(context: RoutingContext) {
        val searchKey = context.request().params().get("search").trim()
        val result = service.searchStations(searchKey)
        context.success(jsonObject = jsonObjectOf(
                "stations" to JsonArray(result.map { it.toJson() })
        ))
    }

    suspend fun addStation(context: RoutingContext) {
        val user = context.getUser()
        val havePermission = user?.isAuthorizedAwait("admin") ?: false
        if (!havePermission) {
            throw ServiceException("permission denied")
        }

        val body = context.bodyAsJson
        val name = body.getString("name").trim()
        val city = body.getString("city").trim()
        val code = body.getString("code").trim()

        val stationID = service.addStation(name, city, code)

        context.success(jsonObject = jsonObjectOf(
                "station" to stationID
        ))
    }

    suspend fun deleteStation(context: RoutingContext) {
        val user = context.getUser()
        val havePermission = user?.isAuthorizedAwait("admin") ?: false
        if (!havePermission) {
            throw ServiceException("permission denied")
        }

        val stationID = context.request().getParam("station").toInt()
        service.deleteStation(stationID)

        context.success()
    }


}