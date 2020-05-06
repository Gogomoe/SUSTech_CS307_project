package cs307.train.trainline

import java.time.Duration

data class TrainLineStaticStation(
        val station: Int,
        val arriveTime: Duration,
        val departTime: Duration,
        val prices: Map<Int, Int>
)

