package cs307.train

import cs307.Service
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import cs307.format.format
import cs307.ticket.TicketService
import cs307.user.UserService
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
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

}