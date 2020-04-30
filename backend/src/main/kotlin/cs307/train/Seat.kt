package cs307.train

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf

data class Seat(
        val id: Int,
        val name: String
)

data class SeatPriceCount(
        val price: Int,
        val count: Int?
)

fun SeatPriceCount.toJson(): JsonObject {
    return jsonObjectOf(
            "price" to price,
            "count" to count
    )
}