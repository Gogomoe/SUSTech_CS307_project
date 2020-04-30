package cs307.ticket

import cs307.format.getLocalDateTime
import io.vertx.core.json.JsonObject
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