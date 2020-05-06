package cs307.train.timetable

import cs307.format.format
import cs307.format.plusTime
import cs307.train.Station
import cs307.train.Train
import cs307.train.toJson
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.LocalDateTime

data class TrainStationTime(
        val station: Int,
        val arriveTime: LocalDateTime,
        val departTime: LocalDateTime
)

data class TrainTimeTable(
        val train: Train,
        val station: List<TrainStationTime>
) {
    companion object {
        fun from(train: Train, station: List<TrainStaticStationTime>): TrainTimeTable =
                TrainTimeTable(
                        train,
                        station.map {
                            TrainStationTime(
                                    it.station,
                                    train.departDate.plusTime(it.arriveTime),
                                    train.departDate.plusTime(it.departTime)
                            )
                        }
                )
    }

}

data class TrainStationTimeInfo(
        val station: Station,
        val arriveTime: LocalDateTime,
        val departTime: LocalDateTime
)

fun TrainStationTimeInfo.toJson(): JsonObject {
    return jsonObjectOf(
            "station" to station.toJson(),
            "arriveTime" to arriveTime.format(),
            "departTime" to departTime.format()
    )
}

data class TrainTimeTableInfo(
        val train: Train,
        val stations: List<TrainStationTimeInfo>
) {
    companion object {
        fun from(train: Train, station: List<TrainStaticStationTimeInfo>): TrainTimeTableInfo =
                TrainTimeTableInfo(
                        train,
                        station.map {
                            TrainStationTimeInfo(
                                    it.station,
                                    train.departDate.plusTime(it.arriveTime),
                                    train.departDate.plusTime(it.departTime)
                            )
                        }
                )
    }

}

fun TrainTimeTableInfo.toJson(): JsonObject {
    return jsonObjectOf(
            "train" to train.toJson(),
            "stations" to JsonArray(stations.map { it.toJson() })
    )
}