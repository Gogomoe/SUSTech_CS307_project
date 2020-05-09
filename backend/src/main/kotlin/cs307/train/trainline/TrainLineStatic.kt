package cs307.train.trainline

import cs307.format.format
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Duration

data class TrainLineStaticStation(
        val station: Int,
        val arriveTime: Duration,
        val departTime: Duration,
        val prices: Map<Int, Int>
)

fun TrainLineStaticStation.toJson(): JsonObject {
    return jsonObjectOf(
            "station" to station,
            "arriveTime" to arriveTime.format(),
            "departTime" to departTime.format(),
            "prices" to JsonObject(prices.map { it.key.toString() to it.value }.toMap())
    )
}

data class TrainLineStatic(
        val static: Int,
        val seatCount: Map<Int, Int>,
        val stations: List<TrainLineStaticStation>
)

fun TrainLineStatic.toJson(): JsonObject {
    return jsonObjectOf(
            "static" to static,
            "seat" to JsonObject(seatCount.map { it.key.toString() to it.value }.toMap()),
            "stations" to JsonArray(stations.map { it.toJson() })
    )
}
