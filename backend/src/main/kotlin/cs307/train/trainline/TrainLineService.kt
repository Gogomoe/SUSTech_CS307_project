package cs307.train.trainline

import cs307.Service
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import cs307.format.getDuration
import cs307.format.plusTime
import cs307.train.Train
import cs307.train.TrainService
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.*
import java.time.Duration

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

    suspend fun addTrainLineStatic(code: String, type: String, seatCount: Map<Int, Int>, stations: List<TrainLineStaticStation>): Int {
        if (code.isBlank() || type.isBlank()) {
            throw ServiceException("code or type cannot be blank")
        }
        if (stations.map { it.station }.distinct().size != stations.size) {
            throw ServiceException("contains same stations")
        }
        if (stations.size < 2) {
            throw ServiceException("must contain at least 2 stations")
        }
        if (!seatCount.keys.all { it in setOf(1, 2, 3, 4) }) {
            throw ServiceException("seat type invalid")
        }
        var lastPrice = seatCount.keys.associateWith { Int.MAX_VALUE }
        var lastDepartDuration = Duration.ofSeconds(-1)
        for (station in stations) {
            if (station.prices.keys != seatCount.keys) {
                throw ServiceException("price of seat not associate with seat types")
            }
            for (seatType in seatCount.keys) {
                if (station.prices[seatType]!! >= lastPrice[seatType]!!) {
                    throw ServiceException("price of successor station cannot higher than previous station")
                }
            }
            if (station.arriveTime <= lastDepartDuration) {
                throw ServiceException("arrive time must later than depart time of previous station")
            }
            if (station.departTime < station.arriveTime) {
                throw ServiceException("depart time must later than arrive time")
            }
            lastDepartDuration = station.departTime
            lastPrice = station.prices
        }
        if (stations.last().prices.values.any { it != 0 }) {
            throw ServiceException("price of last station must be zero")
        }

        val staticID = database.transaction { connection ->
            val result = connection.updateWithParamsAwait("""
                INSERT INTO train_static (train_static_id,
                                          code,
                                          type,
                                          depart_station,
                                          arrive_station,
                                          depart_time,
                                          arrive_time)
                VALUES ((SELECT MAX(train_static_id) FROM train_static) + 1,
                        ?, ?,
                        ?, ?,
                        ?::INTERVAL(0), ?::INTERVAL(0));
            """.trimIndent(), jsonArrayOf(
                    code, type,
                    stations.first().station, stations.last().station,
                    "${stations.first().departTime.toSeconds()}s",
                    "${stations.last().arriveTime.toSeconds()}s"
            ))
            val staticID = result.keys.getInteger(0)

            seatCount.forEach { (seatType, count) ->
                connection.updateWithParamsAwait("""
                    INSERT INTO train_seat (train_static_id, seat_id, count)
                    VALUES (?, ?, ?);
                """.trimIndent(), jsonArrayOf(staticID, seatType, count))
            }

            stations.forEach { station ->
                connection.updateWithParamsAwait("""
                    INSERT INTO train_station(train_static_id, station_id, arrive_time, depart_time)
                    VALUES (?, ?, ?::INTERVAL(0), ?::INTERVAL(0));
                """.trimIndent(), jsonArrayOf(
                        staticID, station.station,
                        "${station.arriveTime.toSeconds()}s", "${station.departTime.toSeconds()}s"
                ))
                station.prices.forEach { (seatType, price) ->
                    connection.updateWithParamsAwait("""
                    INSERT INTO train_station_price (train_static_id, station_id, seat_id, remain_price)
                    values (?, ?, ?, ?);
                """.trimIndent(), jsonArrayOf(
                            staticID, station.station,
                            seatType, price
                    ))
                }
            }
            staticID
        }

        return staticID
    }

    suspend fun removeTrainLineStatic(staticID: Int) {
        val count = database.querySingleWithParamsAwait("""
            SELECT (SELECT COUNT(*) FROM train_active WHERE train_static = ?) +
                   (SELECT COUNT(*) FROM train_history WHERE train_static = ?);
        """.trimIndent(), jsonArrayOf(staticID, staticID))!!.getInteger(0)

        if (count != 0) {
            throw ServiceException("have train depend on the static, cannot delete")
        }

        database.transaction { connection ->
            connection.updateWithParamsAwait("""
                DELETE
                FROM train_station
                WHERE train_static_id = ?;
            """.trimIndent(), jsonArrayOf(staticID))
            connection.updateWithParamsAwait("""
                DELETE
                FROM train_station_price
                WHERE train_static_id = ?;
            """.trimIndent(), jsonArrayOf(staticID))
            connection.updateWithParamsAwait("""
                DELETE
                FROM train_seat
                WHERE train_static_id = ?;
            """.trimIndent(), jsonArrayOf(staticID))
            connection.updateWithParamsAwait("""
                DELETE
                FROM train_static
                WHERE train_static_id = ?;
            """.trimIndent(), jsonArrayOf(staticID))
            Unit
        }
    }

    suspend inline fun <T> JDBCClient.transaction(block: (SQLConnection) -> T): T {
        val connection = this.getConnectionAwait()
        try {
            connection.setAutoCommitAwait(false)
            val result = block(connection)
            connection.commitAwait()
            return result
        } catch (e: Exception) {
            connection.rollbackAwait()
            throw e
        } finally {
            connection.closeAwait()
        }
    }

}