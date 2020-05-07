package cs307.train.trainline

import cs307.Service
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import cs307.format.getDuration
import cs307.format.plusTime
import cs307.train.Train
import cs307.train.TrainService
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryWithParamsAwait

class TrainLineService : Service {

    private lateinit var database: JDBCClient
    private lateinit var trainService: TrainService

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()
        trainService = registry[TrainService::class.java]
    }

    suspend fun getTrainLineStatic(static: Int): TrainLineStatic {
        val seatCount = database.queryWithParamsAwait("""
             SELECT seat_id, count
             FROM train_seat
             WHERE train_static_id = ?;
        """.trimIndent(), jsonArrayOf(static)
        ).rows.map { it.getInteger("seat_id") to it.getInteger("count") }.toMap()

        val stationsRows = database.queryWithParamsAwait("""
            SELECT tsp.seat_id,
                   tsp.remain_price,
                   ts.station_id,
                   EXTRACT(epoch FROM ts.arrive_time) arrive_time,
                   EXTRACT(epoch FROM ts.depart_time) depart_time
            FROM train_station_price tsp
                     LEFT OUTER JOIN train_station ts ON tsp.station_id = ts.station_id
                AND tsp.train_static_id = ts.train_static_id
            WHERE ts.train_static_id = ?
            ORDER BY ts.arrive_time;
        """.trimIndent(), jsonArrayOf(static)).rows

        val stations = mutableListOf<TrainLineStaticStation>()
        stationsRows.forEach {
            val seatType = it.getInteger("seat_id")
            val price = it.getInteger("remain_price")
            val station = it.getInteger("station_id")
            val arriveTime = it.getDuration("arrive_time")
            val departTime = it.getDuration("depart_time")

            if (stations.isNotEmpty() && stations.last().station == station) {
                (stations.last().prices as MutableMap)[seatType] = price
            } else {
                stations.add(
                        TrainLineStaticStation(
                                station,
                                arriveTime,
                                departTime,
                                mutableMapOf(seatType to price)
                        )
                )
            }
        }

        return TrainLineStatic(static, seatCount, stations)
    }

    suspend fun getTrainLine(train: Train): TrainLine {
        val trainLineStatic = getTrainLineStatic(train.static.id)

        return TrainLine(
                train.id,
                trainLineStatic.seatCount,
                trainLineStatic.stations.map {
                    TrainLineStation(
                            it.station,
                            train.departDate.plusTime(it.arriveTime),
                            train.departDate.plusTime(it.departTime),
                            it.prices)
                }
        )
    }

}