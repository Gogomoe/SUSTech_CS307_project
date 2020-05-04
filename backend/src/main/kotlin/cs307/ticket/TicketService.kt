package cs307.ticket

import cs307.Service
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import cs307.format.getDuration
import cs307.memory.MemoryService
import cs307.passenger.PassengerService
import cs307.train.TrainLineSeat
import cs307.train.TrainService
import cs307.train.TrainStationPrice
import cs307.train.toTrain
import cs307.user.UserAuth
import io.vertx.core.Vertx
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class TicketService : Service {

    private lateinit var store: MemoryService
    private lateinit var database: JDBCClient
    private lateinit var trainService: TrainService
    private lateinit var passengerService: PassengerService

    private lateinit var vertx: Vertx

    private lateinit var scope: CoroutineScope

    override suspend fun start(registry: ServiceRegistry) {
        store = registry[MemoryService::class.java]
        database = registry[DatabaseService::class.java].client()
        trainService = registry[TrainService::class.java]
        passengerService = registry[PassengerService::class.java]
        vertx = registry.vertx()
        scope = CoroutineScope(Dispatchers.Default)
    }

    suspend fun putOrder(trainID: Int, user: UserAuth, passengerID: Int, seatType: Int, departStationID: Int, arriveStationID: Int): Int {
        val passengerBelongToUser = if (user.isAuthorizedAwait("admin")) {
            true
        } else {
            passengerService.getAllPassengers(user.user.username).any {
                it.id == passengerID
            }
        }
        if (!passengerBelongToUser) {
            throw ServiceException("permission denied, you can not buy ticket for the passenger")
        }

        val trainTicket = getTrainTicketResult(trainID);
        val seatNum = trainTicket.generateTicket(departStationID, arriveStationID, seatType)
        val result = database.updateWithParamsAwait(
                """
                INSERT INTO ticket_active
                (ticket_id, train_id, depart_station, arrive_station, seat_id, seat_num, passenger_id, username)
                VALUES (nextval('ticket_sequence'), ?, ?, ?, ?, ?, ?, ?);
            """.trimIndent(),
                jsonArrayOf(
                        trainID,
                        departStationID,
                        arriveStationID,
                        seatType,
                        seatNum,
                        passengerID,
                        user.user.username
                )
        )
        if (result.updated == 1) {
            return result.keys.getInteger(0)
        } else {
            trainTicket.retrieveTicket(departStationID, arriveStationID, seatType, seatNum)
            throw ServiceException("generate ticket failed")
        }
    }

    suspend fun cancelOrder(user: UserAuth, ticketID: Int) {
        val ticket = getTicket(ticketID)

        val ticketCreateByUser = if (user.isAuthorizedAwait("admin")) {
            true
        } else {
            user.user.username == ticket.username
        }
        if (!ticketCreateByUser) {
            throw ServiceException("permission denied, you cancel others' ticket")
        }

        if (!ticket.valid) {
            throw ServiceException("the ticket has been cancelled")
        }

        val trainNotDepart = if (user.isAuthorizedAwait("admin")) {
            true
        } else {
            val timeTable = trainService.getTrainTimeTable(ticket.train)
            timeTable.station
                    .find { it.station == ticket.departStation }!!
                    .departTime < LocalDateTime.now()
        }

        if (!trainNotDepart) {
            throw ServiceException("the train has departed")
        }

        val result = database.updateWithParamsAwait("""
            UPDATE ticket_active
            SET valid = FALSE
            WHERE ticket_id = ?;
        """.trimIndent(), jsonArrayOf(ticketID))

        if (result.updated != 1) {
            throw ServiceException("cancel ticket failed")
        }

        val trainTicket = getTrainTicketResult(ticket.train)
        trainTicket.retrieveTicket(ticket.departStation, ticket.arriveStation, ticket.seatType, ticket.seatNum)
    }

    suspend fun getTicket(ticketID: Int): Ticket {
        val result = database.queryWithParamsAwait("""
                    SELECT ticket_id      tk_id,
                           train_id       tk_train,
                           depart_station tk_depart_station,
                           arrive_station tk_arrive_station,
                           seat_id        tk_seat,
                           seat_num       tk_seat_num,
                           passenger_id   tk_passenger,
                           username       tk_username,
                           valid          tk_valid,
                           create_time    tk_create_time,
                           update_time    tk_update_time
                    from ticket_active
                    WHERE ticket_id = ?;
                """.trimIndent(), jsonArrayOf(ticketID))
        return result.rows.firstOrNull()?.toTicket()
                ?: throw ServiceException("ticket not exist")
    }

    suspend fun getTrainTicketResult(train: Int): TrainTicketResult {
        val storeKey = "train:${train}"
        if (storeKey in store) {
            return store.get(storeKey)
        }
        return withContext(scope.coroutineContext) {
            val tr = database.queryWithParamsAwait(
                    """
                            SELECT train_id                           tr_id,
                                   depart_date                        tr_depart_date,
                                   ts.train_static_id                 tr_ts_id,
                                   ts.code                            tr_ts_code,
                                   ts.type                            tr_ts_type,
                                   ts.depart_station                  tr_ts_depart_station,
                                   ts.arrive_station                  tr_ts_arrive_station,
                                   EXTRACT(epoch FROM ts.depart_time) tr_ts_depart_time,
                                   EXTRACT(epoch FROM ts.arrive_time) tr_ts_arrive_time
                            FROM train_active
                                     JOIN train_static ts on train_active.train_static = ts.train_static_id
                            WHERE train_id = ?;
                        """.trimIndent(), jsonArrayOf(train)
            ).rows.first().toTrain()

            val seatCount = database.queryWithParamsAwait(
                    """
                            SELECT seat_id, count
                            FROM train_seat
                            WHERE train_static_id = ?;
                        """.trimIndent(), jsonArrayOf(tr.static.id)
            ).rows.map { it.getInteger("seat_id") to it.getInteger("count") }.toMap()

            val stationsRows = database.queryWithParamsAwait(
                    """
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
                        """.trimIndent(), jsonArrayOf(tr.static.id)
            ).rows
            val stations = mutableListOf<TrainStationPrice>()
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
                            TrainStationPrice(
                                    station,
                                    arriveTime,
                                    departTime,
                                    mutableMapOf(
                                            seatType to price
                                    )
                            )
                    )
                }
            }

            val tickets = database.queryWithParamsAwait("""
                    SELECT ticket_id      tk_id,
                           train_id       tk_train,
                           depart_station tk_depart_station,
                           arrive_station tk_arrive_station,
                           seat_id        tk_seat,
                           seat_num       tk_seat_num,
                           passenger_id   tk_passenger,
                           username       tk_username,
                           valid          tk_valid,
                           create_time    tk_create_time,
                           update_time    tk_update_time
                    from ticket_active
                    WHERE train_id = ?
                      AND valid;
                """.trimIndent(), jsonArrayOf(train)).rows.map { it.toTicket() }

            val trainLineSeat = TrainLineSeat(train, seatCount, stations)

            val trainTicketResult = TrainTicketResult(tr, trainLineSeat, tickets)

            synchronized(store) {
                if (storeKey in store) {
                    store.get(storeKey)
                } else {
                    store[storeKey] = trainTicketResult
                    trainTicketResult
                }
            }
        }
    }

}