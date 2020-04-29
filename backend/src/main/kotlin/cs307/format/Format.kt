package cs307.format

import io.vertx.core.json.JsonObject
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private val datetimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

fun LocalDate.format(): String = this.format(dateFormatter)

fun LocalDateTime.format(): String = this.format(datetimeFormatter)

fun Duration.format(): String {
    val seconds: Long = this.seconds
    val absSeconds = abs(seconds)
    val positive = String.format(
            "%d:%02d:%02d",
            absSeconds / 3600,
            absSeconds % 3600 / 60,
            absSeconds % 60)
    return if (seconds < 0) "-$positive" else positive
}

fun JsonObject.getLocalDate(name: String): LocalDate {
    return LocalDate.from(dateFormatter.parse(this.getString(name)))
}

fun JsonObject.getLocalDateTime(name: String): LocalDateTime {
    return LocalDateTime.ofInstant(this.getInstant(name), ZoneId.of("+8"))
}

fun JsonObject.getDuration(name: String): Duration {
    return Duration.ofSeconds(this.getLong(name))
}

fun String.toLocalDate(): LocalDate {
    return LocalDate.from(dateFormatter.parse(this))
}