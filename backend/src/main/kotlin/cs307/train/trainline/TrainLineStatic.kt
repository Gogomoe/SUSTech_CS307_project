package cs307.train.trainline

import java.time.Duration

data class TrainLineStaticStation(
        val station: Int,
        val arriveTime: Duration,
        val departTime: Duration,
        val prices: Map<Int, Int>
)

data class TrainLineStatic(
        val static: Int,
        val seatCount: Map<Int, Int>,
        val stations: List<TrainLineStaticStation>
)