package cs307.train

import cs307.format.format
import cs307.format.getLocalDate
import cs307.format.getLocalDateTime
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.LocalDate
import java.time.LocalDateTime

data class Train(
        val id: Int,
        val static: TrainStatic,
        val departDate: LocalDate
)


data class TrainBetween(
        val train: Train,
        val departStation: Station,
        val departTime: LocalDateTime,
        val arriveStation: Station,
        val arriveTime: LocalDateTime,
        val seat: MutableMap<Int, SeatPriceCount> = mutableMapOf()
)

fun JsonObject.toTrain(prefix: String = ""): Train {
    return Train(
            this.getInteger("${prefix}tr_id"),
            this.toTrainStatic("${prefix}tr_"),
            this.getLocalDate("${prefix}tr_depart_date")
    )
}


fun Train.toJson(): JsonObject {
    return jsonObjectOf(
            "id" to id,
            "static" to static.toJson(),
            "depart_date" to departDate.format()
    )
}


fun JsonObject.toTrainBetween(prefix: String = ""): TrainBetween {
    return TrainBetween(
            this.toTrain("${prefix}tb_"),
            this.toStation("${prefix}tb_depart_"),
            this.getLocalDateTime("${prefix}tb_depart_time"),
            this.toStation("${prefix}tb_arrive_"),
            this.getLocalDateTime("${prefix}tb_arrive_time")
    )
}

fun TrainBetween.toJson(): JsonObject {
    return jsonObjectOf(
            "train" to train.toJson(),
            "depart_station" to departStation.toJson(),
            "depart_time" to departTime.format(),
            "arrive_station" to arriveStation.toJson(),
            "arrive_time" to arriveTime.format(),
            "seats" to JsonObject(seat.map { it.key.toString() to it.value.toJson() }.toMap())
    )
}



