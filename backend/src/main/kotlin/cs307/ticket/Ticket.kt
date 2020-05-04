package cs307.ticket

import cs307.format.format
import cs307.format.getLocalDateTime
import cs307.passenger.Passenger
import cs307.passenger.toJSon
import cs307.passenger.toPassenger
import cs307.train.*
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.LocalDateTime

data class Ticket(
        val id: Int,
        val train: Int,
        val departStation: Int,
        val arriveStation: Int,
        val seatType: Int,
        val seatNum: Int,
        val passenger: Int,
        val username: String,
        val valid: Boolean,
        val createTime: LocalDateTime,
        val updateTime: LocalDateTime
)

fun JsonObject.toTicket(prefix: String = ""): Ticket {
    return Ticket(
            getInteger("${prefix}tk_id"),
            getInteger("${prefix}tk_train"),
            getInteger("${prefix}tk_depart_station"),
            getInteger("${prefix}tk_arrive_station"),
            getInteger("${prefix}tk_seat"),
            getInteger("${prefix}tk_seat_num"),
            getInteger("${prefix}tk_passenger"),
            getString("${prefix}tk_username"),
            getBoolean("${prefix}tk_valid"),
            getLocalDateTime("${prefix}tk_create_time"),
            getLocalDateTime("${prefix}tk_update_time")
    )
}

data class TicketInfo(
        val id: Int,
        val train: Train,
        val departStation: Station,
        val arriveStation: Station,
        val departTime: LocalDateTime,
        val arriveTime: LocalDateTime,
        val seatType: Int,
        val seatNum: Int,
        val passenger: Passenger,
        val username: String,
        val valid: Boolean,
        val createTime: LocalDateTime,
        val updateTime: LocalDateTime
)

fun JsonObject.toTicketInfo(prefix: String = ""): TicketInfo {
    return TicketInfo(
            this.getInteger("${prefix}tk_id"),
            this.toTrain("${prefix}tk_"),
            this.toStation("${prefix}tk_depart_"),
            this.toStation("${prefix}tk_arrive_"),
            this.getLocalDateTime("${prefix}tk_depart_time"),
            this.getLocalDateTime("${prefix}tk_arrive_time"),
            this.getInteger("${prefix}tk_seat"),
            this.getInteger("${prefix}tk_seat_num"),
            this.toPassenger("${prefix}tk_"),
            this.getString("${prefix}tk_username"),
            this.getBoolean("${prefix}tk_valid"),
            this.getLocalDateTime("${prefix}tk_create_time"),
            this.getLocalDateTime("${prefix}tk_update_time")
    )
}

fun TicketInfo.toJson(): JsonObject {
    return jsonObjectOf(
            "id" to id,
            "train" to train.toJson(),
            "departStation" to departStation.toJson(),
            "arriveStation" to arriveStation.toJson(),
            "departTime" to departTime.format(),
            "arriveTime" to arriveTime.format(),
            "seatType" to seatType,
            "seatNum" to seatNum,
            "passenger" to passenger.toJSon(),
            "username" to username,
            "valid" to valid,
            "createTime" to createTime.format(),
            "updateTime" to updateTime.format()
    )
}