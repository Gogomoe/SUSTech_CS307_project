package cs307.train.timetable

import cs307.format.format
import cs307.format.getDuration
import cs307.train.Station
import cs307.train.TrainStatic
import cs307.train.toJson
import cs307.train.toStation
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Duration

data class TrainStaticStationTime(
        val station: Int,
        val arriveTime: Duration,
        val departTime: Duration
)

fun JsonObject.toTrainStaticStationTime(prefix: String = ""): TrainStaticStationTime {
    return TrainStaticStationTime(
            getInteger("${prefix}tss_station"),
            getDuration("${prefix}tss_arrive_time"),
            getDuration("${prefix}tss_depart_time")
    )
}

data class TrainStaticStationTimeInfo(
        val station: Station,
        val arriveTime: Duration,
        val departTime: Duration
)

fun TrainStaticStationTimeInfo.toJson(): JsonObject {
    return jsonObjectOf(
            "station" to station.toJson(),
            "arriveTime" to arriveTime.format(),
            "departTime" to departTime.format()
    )
}

fun JsonObject.toTrainStaticStationTimeInfo(prefix: String = ""): TrainStaticStationTimeInfo {
    return TrainStaticStationTimeInfo(
            toStation("${prefix}tss_"),
            getDuration("${prefix}tss_arrive_time"),
            getDuration("${prefix}tss_depart_time")
    )
}

data class TrainStaticTimetableInfo(
        val static: TrainStatic,
        val stations: List<TrainStaticStationTimeInfo>
)

fun TrainStaticTimetableInfo.toJson(): JsonObject {
    return jsonObjectOf(
            "static" to static.toJson(),
            "stations" to JsonArray(stations.map { it.toJson() })
    )
}