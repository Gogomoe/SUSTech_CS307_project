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

fun String.toDuration(): Duration {
    val ints = this.split(':').map { it.toInt() }
    val size = ints.size
    if (size < 3 || size > 4) {
        throw IllegalArgumentException("format of duration is invalid")
    }
    var seconds = ints[size - 1] + ints[size - 2] * 60 + ints[size - 3] * 60 * 60
    if (size == 4) {
        seconds += ints[0] * 24 * 60 * 60
    }
    return Duration.ofSeconds(seconds.toLong())
}

fun LocalDate.plusTime(duration: Duration): LocalDateTime {
    return LocalDateTime.of(this, LocalTime.MIN).plus(duration)
}