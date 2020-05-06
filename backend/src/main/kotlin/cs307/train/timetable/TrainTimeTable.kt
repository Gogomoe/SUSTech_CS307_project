package cs307.train.timetable

import cs307.format.plusTime
import cs307.train.Station
import cs307.train.Train
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

data class TrainTimeTableInfo(
        val train: Train,
        val station: List<TrainStationTimeInfo>
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