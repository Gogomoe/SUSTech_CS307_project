package cs307.train.timetable

import cs307.format.getDuration
import io.vertx.core.json.JsonObject
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