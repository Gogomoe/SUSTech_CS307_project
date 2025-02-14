package cs307.train

import cs307.Service
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import cs307.format.format
import cs307.ticket.TicketService
import cs307.train.timetable.*
import cs307.user.UserService
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import java.time.LocalDate

class TrainService : Service {

    private lateinit var database: JDBCClient
    private lateinit var auth: AuthProvider
    private lateinit var tickets: TicketService

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()
        auth = registry[UserService::class.java].auth()
        tickets = registry[TicketService::class.java]
    }

    suspend fun getTrain(train: Int): Train {
        val result = database.queryWithParamsAwait("""
            SELECT tr.tr_id,
                   tr.tr_depart_date,
                   ts.train_static_id                     tr_ts_id,
                   ts.code                                tr_ts_code,
                   ts.type                                tr_ts_type,
                   ts.depart_station                      tr_ts_depart_station,
                   ts.arrive_station                      tr_ts_arrive_station,
                   EXTRACT(epoch FROM ts.depart_time)     tr_ts_depart_time,
                   EXTRACT(epoch FROM ts.arrive_time)     tr_ts_arrive_time
            FROM (SELECT train_id     tr_id,
                         train_static tr_ts,
                         depart_date  tr_depart_date
                  FROM train_active
                  WHERE train_active.train_id = ?
                  UNION ALL
                  SELECT train_id     tr_id,
                         train_static tr_ts,
                         depart_date  tr_depart_date
                  FROM train_history
                  WHERE train_history.train_id = ?) tr
                     JOIN train_static ts ON tr.tr_ts = ts.train_static_id;
        """.trimIndent(), jsonArrayOf(train, train))
        if (result.rows.isEmpty()) {
            throw ServiceException("train not exist")
        }
        return result.rows.first().toTrain()
    }

    suspend fun getActiveTrain(train: Int): Train {
        return database.queryWithParamsAwait("""
            SELECT tr.train_id                        tr_id,
                   tr.depart_date                     tr_depart_date,
                   ts.train_static_id                 tr_ts_id,
                   ts.code                            tr_ts_code,
                   ts.type                            tr_ts_type,
                   ts.depart_station                  tr_ts_depart_station,
                   ts.arrive_station                  tr_ts_arrive_station,
                   EXTRACT(epoch FROM ts.depart_time) tr_ts_depart_time,
                   EXTRACT(epoch FROM ts.arrive_time) tr_ts_arrive_time
            FROM train_active tr
                     JOIN train_static ts ON tr.train_static = ts.train_static_id
            WHERE tr.train_id = ?;
        """.trimIndent(), jsonArrayOf(train)).rows.firstOrNull()?.toTrain()
                ?: throw ServiceException("train not exist")
    }

    suspend fun getTrainStatic(static: Int): TrainStatic {
        val result = database.queryWithParamsAwait("""
            SELECT train_static_id                 ts_id,
                   code                            ts_code,
                   type                            ts_type,
                   depart_station                  ts_depart_station,
                   arrive_station                  ts_arrive_station,
                   EXTRACT(epoch FROM depart_time) ts_depart_time,
                   EXTRACT(epoch FROM arrive_time) ts_arrive_time
            FROM train_static
            WHERE train_static_id = ?;
        """.trimIndent(), jsonArrayOf(static))
        if (result.rows.isEmpty()) {
            throw ServiceException("train static not exist")
        }
        return result.rows.first().toTrainStatic()
    }

    suspend fun getTrainStaticStationTime(trainStatic: Int): List<TrainStaticStationTime> {
        return database.queryWithParamsAwait("""
            SELECT station_id                      tss_station,
                   EXTRACT(epoch FROM arrive_time) tss_arrive_time,
                   EXTRACT(epoch FROM depart_time) tss_depart_time
            FROM train_station
            WHERE train_static_id = ?
            ORDER BY arrive_time;
        """.trimIndent(), jsonArrayOf(trainStatic)).rows
                .map { it.toTrainStaticStationTime() }
    }

    suspend fun getTrainTimeTable(train: Int): TrainTimeTable {
        val t = getTrain(train)
        val stations = getTrainStaticStationTime(t.static.id)
        return TrainTimeTable.from(t, stations)
    }

    suspend fun getTrainStaticStationTimeInfo(trainStatic: Int): List<TrainStaticStationTimeInfo> {
        return database.queryWithParamsAwait("""
            SELECT s.station_id                    tss_st_id,
                   s.name                          tss_st_name,
                   s.city                          tss_st_city,
                   s.code                          tss_st_code,
                   EXTRACT(epoch FROM arrive_time) tss_arrive_time,
                   EXTRACT(epoch FROM depart_time) tss_depart_time
            FROM train_station
                     JOIN station s on train_station.station_id = s.station_id
            WHERE train_static_id = ?
            ORDER BY arrive_time;
        """.trimIndent(), jsonArrayOf(trainStatic)).rows
                .map { it.toTrainStaticStationTimeInfo() }
    }

    suspend fun getTrainTimetableInfo(train: Int): TrainTimeTableInfo {
        val t = getTrain(train)
        val stations = getTrainStaticStationTimeInfo(t.static.id)
        return TrainTimeTableInfo.from(t, stations)
    }

    suspend fun getTrainStaticTimetableInfo(static: Int): TrainStaticTimetableInfo {
        val train = getTrainStatic(static)
        val stations = getTrainStaticStationTimeInfo(static)
        return TrainStaticTimetableInfo(train, stations)
    }

    suspend fun searchActiveTrainBetween(from: String, to: String, date: LocalDate): List<TrainBetween> {
        if (from.trim().length < 2 || to.trim().length < 2) {
            throw ServiceException("the name of station or city is too short, it must greater than 2")
        }
        val result = database.queryWithParamsAwait("""
            SELECT f.ts_id                                  tb_tr_ts_id,
                   f.ts_code                                tb_tr_ts_code,
                   f.ts_type                                tb_tr_ts_type,
                   f.ts_depart_station                      tb_tr_ts_depart_station,
                   f.ts_arrive_station                      tb_tr_ts_arrive_station,
                   EXTRACT(epoch FROM f.ts_depart_time)     tb_tr_ts_depart_time,
                   EXTRACT(epoch FROM f.ts_arrive_time)     tb_tr_ts_arrive_time,
                   train_active.train_id                    tb_tr_id,
                   train_active.depart_date                 tb_tr_depart_date,
                   f.station_id                             tb_depart_st_id,
                   f.name                                   tb_depart_st_name,
                   f.city                                   tb_depart_st_city,
                   f.st_code                                tb_depart_st_code,
                   t.station_id                             tb_arrive_st_id,
                   t.name                                   tb_arrive_st_name,
                   t.city                                   tb_arrive_st_city,
                   t.st_code                                tb_arrive_st_code,
                   train_active.depart_date + f.depart_time tb_depart_time,
                   train_active.depart_date + t.arrive_time tb_arrive_time
            FROM (SELECT train_static.code ts_code, train_static.type ts_type, 
                         train_static.depart_station ts_depart_station, train_static.arrive_station ts_arrive_station,
                         train_static.depart_time ts_depart_time, train_static.arrive_time ts_arrive_time,
                         train_static.train_static_id ts_id, train_station.depart_time, station.station_id, 
                         station.name, station.city, station.code st_code
                  FROM train_static
                           JOIN train_station ON train_static.train_static_id = train_station.train_static_id
                           JOIN station ON train_station.station_id = station.station_id
                  WHERE station.name LIKE ?
                     OR station.city LIKE ?
                 ) f
                     JOIN
                 (SELECT train_static.train_static_id ts_id, train_station.arrive_time, station.station_id, 
                         station.name, station.city, station.code st_code
                  FROM train_static
                           JOIN train_station ON train_static.train_static_id = train_station.train_static_id
                           JOIN station ON train_station.station_id = station.station_id
                  WHERE station.name LIKE ?
                     OR station.city LIKE ?
                 ) t
                 ON f.ts_id = t.ts_id AND f.depart_time < t.arrive_time
                     JOIN train_active ON f.ts_id = train_active.train_static
            WHERE train_active.depart_date + f.depart_time BETWEEN ?::DATE AND (?::DATE + '1d'::INTERVAL(0));
        """.trimIndent(), jsonArrayOf("%${from}%", "%${from}%", "%${to}%", "%${to}%", date.format(), date.format()))

        return result.rows.map {
            it.toTrainBetween()
        }.onEach {
            val trainTicketResult = tickets.getTrainTicketResult(it.train.id)
            val ticketInfo = trainTicketResult.ticketInfo(it.departStation.id, it.arriveStation.id)
            it.seat.putAll(ticketInfo)
        }
    }

    suspend fun searchTransshipTrainBetween(from: String, to: String, date: LocalDate): List<TransshipTrainBetween> {
        if (from.trim().length < 2 || to.trim().length < 2) {
            throw ServiceException("the name of station or city is too short, it must greater than 2")
        }
        
        val result = database.queryWithParamsAwait("""
                SELECT train_1.ts_id                              tr1_tb_tr_ts_id,
                train_1.ts_code                            tr1_tb_tr_ts_code,
                train_1.ts_type                            tr1_tb_tr_ts_type,
                train_1.ts_depart_station                  tr1_tb_tr_ts_depart_station,
                train_1.ts_arrive_station                  tr1_tb_tr_ts_arrive_station,
                EXTRACT(epoch FROM train_1.ts_depart_time) tr1_tb_tr_ts_depart_time,
                EXTRACT(epoch FROM train_1.ts_arrive_time) tr1_tb_tr_ts_arrive_time,
                train_1.tr_id                              tr1_tb_tr_id,
                train_1.depart_date                        tr1_tb_tr_depart_date,
                train_1.station_id                         tr1_tb_depart_st_id,
                train_1.s_name                             tr1_tb_depart_st_name,
                train_1.city                               tr1_tb_depart_st_city,
                train_1.s_code                             tr1_tb_depart_st_code,
                stations.station_id                        tr1_tb_arrive_st_id,
                stations.s_name                            tr1_tb_arrive_st_name,
                stations.city                              tr1_tb_arrive_st_city,
                stations.s_code                            tr1_tb_arrive_st_code,
                train_1.depart_time                        tr1_tb_depart_time,
                stations.arrive_time                       tr1_tb_arrive_time,
                
                train_2.ts_id                              tr2_tb_tr_ts_id,
                train_2.ts_code                            tr2_tb_tr_ts_code,
                train_2.ts_type                            tr2_tb_tr_ts_type,
                train_2.ts_depart_station                  tr2_tb_tr_ts_depart_station,
                train_2.ts_arrive_station                  tr2_tb_tr_ts_arrive_station,
                EXTRACT(epoch FROM train_2.ts_depart_time) tr2_tb_tr_ts_depart_time,
                EXTRACT(epoch FROM train_2.ts_arrive_time) tr2_tb_tr_ts_arrive_time,
                train_2.tr_id                              tr2_tb_tr_id,
                train_2.depart_date                        tr2_tb_tr_depart_date,
                train_2.station_id                         tr2_tb_depart_st_id,
                stations.s_name                            tr2_tb_depart_st_name,
                stations.city                              tr2_tb_depart_st_city,
                stations.s_code                            tr2_tb_depart_st_code,
                t.station_id                               tr2_tb_arrive_st_id,
                t.s_name                                   tr2_tb_arrive_st_name,
                t.city                                     tr2_tb_arrive_st_city,
                t.s_code                                   tr2_tb_arrive_st_code,
                train_2.depart_time                        tr2_tb_depart_time,
                t.arrive_time                              tr2_tb_arrive_time
            FROM (SELECT ts.train_static_id               ts_id,
                 ts.code                          ts_code,
                 ts.type                          ts_type,
                 ts.depart_station                ts_depart_station,
                 ts.arrive_station                ts_arrive_station,
                 ts.depart_time                   ts_depart_time,
                 ts.arrive_time                   ts_arrive_time,
                 trs.depart_time + ta.depart_date depart_time,
                 ta.depart_date                   depart_date,
                 s.station_id                     station_id,
                 s.name                           s_name,
                 s.city                           city,
                 s.code                           s_code,
                 ta.train_id                      tr_id
                FROM train_station trs
                   JOIN station s ON s.station_id = trs.station_id
                   JOIN train_static ts ON ts.train_static_id = trs.train_static_id
                   JOIN train_active ta ON ts.train_static_id = ta.train_static
              WHERE (s.name like ?
                  OR s.city like ?)
                AND trs.depart_time + ta.depart_date BETWEEN ?::DATE AND (?::DATE + '1d'::INTERVAL)
             ) train_1
         JOIN (SELECT trs.arrive_time + ta.depart_date arrive_time,
                      trs.depart_time + ta.depart_date depart_time,
                      s.station_id                     station_id,
                      s.name                           s_name,
                      s.city                           city,
                      s.code                           s_code,
                      ta.train_id                      tr_id
                FROM train_station trs
                        JOIN train_static ts ON ts.train_static_id = trs.train_static_id
                        JOIN train_active ta ON ta.train_static = ts.train_static_id
                        JOIN station s ON trs.station_id = s.station_id
            ) stations ON train_1.tr_id = stations.tr_id AND stations.depart_time > train_1.depart_time
         JOIN (SELECT ts.train_static_id               ts_id,
                      ts.code                          ts_code,
                      ts.type                          ts_type,
                      ts.depart_station                ts_depart_station,
                      ts.arrive_station                ts_arrive_station,
                      ts.depart_time                   ts_depart_time,
                      ts.arrive_time                   ts_arrive_time,
                      trs.depart_time + ta.depart_date depart_time,
                      ta.depart_date                   depart_date,
                      trs.station_id                   station_id,
                      ta.train_id                      tr_id
               FROM train_station trs
                        JOIN train_static ts ON ts.train_static_id = trs.train_static_id
                        JOIN train_active ta ON ts.train_static_id = ta.train_static AND 
                                    ta.depart_date BETWEEN ?::DATE AND (?::DATE + '3d'::INTERVAL)
            ) train_2 ON stations.station_id = train_2.station_id
                        AND stations.tr_id <> train_2.tr_id
                     AND train_2.depart_time > (stations.arrive_time + '15m'::interval)
         JOIN (SELECT trs.arrive_time + ta.depart_date arrive_time,
                      trs.depart_time + ta.depart_date depart_time,
                      s.station_id                     station_id,
                      s.name                           s_name,
                      s.city                           city,
                      s.code                           s_code,
                      ta.train_id                      tr_id
               FROM train_station trs
                    JOIN station s ON trs.station_id = s.station_id
                    JOIN train_static ts ON trs.train_static_id = ts.train_static_id
                    JOIN train_active ta ON ts.train_static_id = ta.train_static
               WHERE s.name like ?
                  OR s.city like ?
            ) t ON t.tr_id = train_2.tr_id
            AND t.depart_time > train_2.depart_time
            ORDER BY t.arrive_time - train_1.depart_time
            LIMIT 10;
        """.trimIndent(), jsonArrayOf("%${from}%", "%${from}%", date.format(), date.format(), date.format(), date.format(), "%${to}%", "%${to}%"))

        return result.rows.map {
            it.toTransshipTrainBetween()
        }.onEach {
            var trainTicketResult = tickets.getTrainTicketResult(it.firstTrainBetween.train.id)
            var ticketInfo = trainTicketResult.ticketInfo(it.firstTrainBetween.departStation.id, it.firstTrainBetween.arriveStation.id)
            it.firstTrainBetween.seat.putAll(ticketInfo)

            trainTicketResult = tickets.getTrainTicketResult(it.secondTrainBetween.train.id)
            ticketInfo = trainTicketResult.ticketInfo(it.secondTrainBetween.departStation.id, it.secondTrainBetween.arriveStation.id)
            it.secondTrainBetween.seat.putAll(ticketInfo)
        }
    }

    suspend fun addTrain(static: Int, date: LocalDate): Int {
        val count = database.querySingleWithParamsAwait("""
            SELECT (SELECT COUNT(*) FROM train_active WHERE train_static = ? AND depart_date = ?) +
                   (SELECT COUNT(*) FROM train_history WHERE train_static = ? AND depart_date = ?) count;
        """.trimIndent(), jsonArrayOf(static, date, static, date)
        )!!.getInteger(0)

        if (count != 0) {
            throw ServiceException("train already exist")
        }

        val result = database.updateWithParamsAwait("""
            INSERT INTO train_active (train_id, train_static, depart_date)
            VALUES (nextval('train_sequence'), ?, ?);
        """.trimIndent(), jsonArrayOf(static, date))

        if (result.updated != 1) {
            throw ServiceException("insert train error")
        }

        return result.keys.getInteger(0)

    }

    suspend fun deleteTrain(trainID: Int) {
        val result1 = database.updateWithParamsAwait("""
            DELETE FROM train_active WHERE train_id = ?;
        """.trimIndent(), jsonArrayOf(trainID))

        val result2 = database.updateWithParamsAwait("""
            DELETE FROM train_history WHERE train_id = ?;
        """.trimIndent(), jsonArrayOf(trainID))

        val updated = result1.updated + result2.updated

        if (updated != 1) {
            throw ServiceException("delete train error")
        }
    }

}