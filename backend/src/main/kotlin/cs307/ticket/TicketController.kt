package cs307.ticket

import cs307.CoroutineController
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.user.getUser
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf

class TicketController(registry: ServiceRegistry) : CoroutineController() {

    val service = registry[TicketService::class.java]

    override fun route(router: Router) {
        router.post("/ticket").coroutineHandler(::putOrder)
        router.delete("/ticket/:ticket").coroutineHandler(::cancelOrder)
    }

    suspend fun putOrder(context: RoutingContext) {
        val user = context.getUser() ?: throw ServiceException("please login")

        val params = context.bodyAsJson
        val trainID = params.getInteger("train")
                ?: throw ServiceException("train id is null")
        val seatType = params.getInteger("seat")
                ?: throw ServiceException("train type is null")
        val departStationID = params.getInteger("from")
                ?: throw ServiceException("depart station is null")
        val arriveStationID = params.getInteger("to")
                ?: throw ServiceException("arrive station is null")
        val passengerID = params.getInteger("passenger")
                ?: throw ServiceException("passenger is null")

        val ticketID = service.putOrder(trainID, user, passengerID, seatType, departStationID, arriveStationID)

        context.success(jsonObject = jsonObjectOf(
                "ticket" to ticketID
        ))
    }

    suspend fun cancelOrder(context: RoutingContext) {
        val user = context.getUser() ?: throw ServiceException("please login")
        val ticketID = context.request().getParam("ticket").toInt()

        service.cancelOrder(user, ticketID)

        context.success()
    }

}