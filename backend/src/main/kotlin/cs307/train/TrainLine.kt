package cs307.train

import java.time.Duration

data class TrainStation(
        val station: Int,
        val arriveTime: Duration,
        val departTime: Duration
)

data class TrainLine(
        val train: Int,
        val stations: List<TrainStation>
)

data class TrainStationPrice(
        val station: Int,
        val arriveTime: Duration,
        val departTime: Duration,
        val prices: Map<Int, Int>
)

data class TrainLineSeat(
        val train: Int,
        val seatCount: Map<Int, Int>,
        val stations: List<TrainStationPrice>
)