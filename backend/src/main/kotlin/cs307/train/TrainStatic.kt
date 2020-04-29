package cs307.train

import cs307.format.format
import cs307.format.getDuration
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Duration

data class TrainStatic(
        val id: Int,
        val code: String,
        val type: String,
        val departStation: Int,
        val arriveStation: Int,
        val departTime: Duration,
        val arriveTime: Duration
)


fun JsonObject.toTrainStatic(prefix: String): TrainStatic {
    return TrainStatic(
            this.getInteger("${prefix}ts_id"),
            this.getString("${prefix}ts_code"),
            this.getString("${prefix}ts_type"),
            this.getInteger("${prefix}ts_depart_station"),
            this.getInteger("${prefix}ts_arrive_station"),
            this.getDuration("${prefix}ts_depart_time"),
            this.getDuration("${prefix}ts_arrive_time")
    )
}

fun TrainStatic.toJson(): Any? {
    return jsonObjectOf(
            "id" to id,
            "code" to code,
            "type" to type,
            "depart_station_id" to departStation,
            "arrive_station_id" to arriveStation,
            "depart_time" to departTime.format(),
            "arrive_time" to arriveTime.format()
    )
}
