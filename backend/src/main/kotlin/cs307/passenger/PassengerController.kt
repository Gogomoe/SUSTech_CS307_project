package cs307.passenger

import cs307.CoroutineController
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.user.getUser
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf

class PassengerController(registry: ServiceRegistry) : CoroutineController() {

    private val service = registry[PassengerService::class.java]

    override fun route(router: Router) {
        router.post("/passenger").coroutineHandler(::handleAddPassenger)
        router.post("/passenger/:passenger_id").coroutineHandler(::handleModifyPassenger)
        router.delete("/passenger/:passenger_id").coroutineHandler(::handleDeletePassenger)
        router.get("/user/:username/passenger").coroutineHandler(::handleGetAllPassengers)
    }

    suspend fun handleAddPassenger(context: RoutingContext) {
        val params = context.bodyAsJson
        val user = context.getUser() ?: throw ServiceException("not login")

        val name = params.getString("passenger_name")
        val idNumber = params.getString("id_number")
        val phone = params.getString("phone")
        if (name == null || name.isEmpty() || idNumber == null || idNumber.isEmpty()
                || phone == null || phone.isEmpty()) {
            throw ServiceException("empty params")
        }

        val result = service.addPassenger(name, idNumber, phone, user.user.username)

        context.success(
                jsonObjectOf(
                        "passenger" to result.toJSon()
                )
        )
    }

    suspend fun handleModifyPassenger(context: RoutingContext) {
        val params = context.bodyAsJson
        val user = context.getUser() ?: throw ServiceException("not login")

        val id = (context.request().getParam("passenger_id")
                ?: throw ServiceException("passenger id not found")).toInt()

        val name = params.getString("passenger_name")
        val idNumber = params.getString("id_number")
        val phone = params.getString("phone")

        if (name == null || name.isEmpty() || idNumber == null || idNumber.isEmpty()
                || phone == null || phone.isEmpty()) {
            throw ServiceException("empty params")
        }

        val result = service.modifyPassenger(id, name, idNumber, phone, user.user.username, user.isAuthorizedAwait("admin"))

        context.success(
                jsonObjectOf(
                        "passenger" to result.toJSon()
                )
        )
    }

    suspend fun handleDeletePassenger(context: RoutingContext) {
        val user = context.getUser() ?: throw ServiceException("not login")

        val id = (context.request().getParam("passenger_id")
                ?: throw ServiceException("passenger id not found")).toInt()

        val result = service.deletePassenger(id, user.user.username, user.isAuthorizedAwait("admin"))

        context.success(
                jsonObjectOf(
                        "passenger" to result.toJSon()
                )
        )
    }

    suspend fun handleGetAllPassengers(context: RoutingContext) {
        val user = context.getUser() ?: throw ServiceException("not login")

        val username = context.request().getParam("username") ?: throw ServiceException("username not found")

        if (username != user.user.username && !user.isAuthorizedAwait("admin")) {
            println(username+" "+user.user.username+" "+user.isAuthorizedAwait("admin"))
            throw ServiceException("The user is invisible for you")
        }

        val result = service.getAllPassengers(username)

        context.success(
                jsonObjectOf(
                        "passengers" to JsonArray(
                                result.map {
                                    it.toJSon()
                                }
                        )
                )
        )
    }
}