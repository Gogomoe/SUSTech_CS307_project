package cs307.passenger

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf

data class Passenger(
        val id: Int,
        val name: String,
        val idNumber: String,
        val phone: String,
        val username: String
)

fun Passenger.toJSon(): JsonObject = jsonObjectOf(
        "passenger_id" to id,
        "passenger_name" to name,
        "id_number" to idNumber,
        "phone" to phone,
        "username" to username
)

fun JsonObject.toPassenger(prefix: String = ""): Passenger {
    return Passenger(
            getInteger("${prefix}ps_passenger_id"),
            getString("${prefix}ps_name"),
            getString("${prefix}ps_people_id"),
            getString("${prefix}ps_phone"),
            getString("${prefix}ps_username")
    )
}

fun JsonArray.toPassenger():Passenger{
    return Passenger(
            getInteger(0),
            getString(1),
            getString(2),
            getString(3),
            getString(4)
    )
}