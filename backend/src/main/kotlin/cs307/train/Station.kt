package cs307.train

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf

data class Station(
        val id: Int,
        val name: String,
        val city: String,
        val code: String
)

fun JsonObject.toStation(prefix: String = ""): Station {
    return Station(
            getInteger("${prefix}st_id"),
            getString("${prefix}st_name"),
            getString("${prefix}st_city"),
            getString("${prefix}st_code")
    )
}

fun Station.toJson(): Any? {
    return jsonObjectOf(
            "id" to id,
            "name" to name,
            "city" to city,
            "code" to code
    )
}
