package cs307.train.trainline

import java.time.LocalDateTime

data class TrainLineStation(
        val station: Int,
        val arriveTime: LocalDateTime,
        val departTime: LocalDateTime,
        val prices: Map<Int, Int>
)

data class TrainLine(
        val train: Int,
        val seatCount: Map<Int, Int>,
        val stations: List<TrainLineStation>
)