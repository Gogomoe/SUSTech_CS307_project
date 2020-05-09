package cs307.ticket

import cs307.Service
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import cs307.memory.MemoryService
import cs307.passenger.PassengerService
import cs307.train.TrainService
import cs307.train.trainline.TrainLineService
import cs307.user.UserAuth
import io.vertx.core.Vertx
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
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
    private lateinit var trainLineService: TrainLineService

    private lateinit var vertx: Vertx

    private lateinit var scope: CoroutineScope

    override suspend fun start(registry: ServiceRegistry) {
        store = registry[MemoryService::class.java]
        database = registry[DatabaseService::class.java].client()
        trainService = registry[TrainService::class.java]
        passengerService = registry[PassengerService::class.java]
        trainLineService = registry[TrainLineService::class.java]
        vertx = registry.vertx()
        scope = CoroutineScope(Dispatchers.Default)
    }

    suspend fun putOrder(trainID: Int, user: UserAuth, passengerID: Int, seatType: Int, departStationID: Int, arriveStationID: Int): Int {
        val passengerBelongToUser = if (
                user.isAuthorizedAwait("admin") ||
                user.isAuthorizedAwait("buy-ticket-for-other")
        ) {
            true
        } else {
            passengerService.getAllPassengers(user.user.username).any {
                it.id == passengerID
            }
        }

        if (!passengerBelongToUser) {
            throw ServiceException("permission denied, you can not buy ticket for the passenger")
        }

        val existent = database.querySingleWithParamsAwait("""
            SELECT true
            FROM train_active ta
            JOIN train_station sf ON ta.train_static = sf.train_static_id AND sf.station_id = ?
            JOIN train_station st ON ta.train_static = st.train_static_id AND st.station_id = ?
            WHERE train_id = ?
            AND EXISTS(SELECT null
                       FROM ticket_active tk
                       JOIN passenger ps ON tk.passenger_id = ps.passenger_id
                       JOIN train_active ta2 ON tk.train_id = ta2.train_id
                       JOIN train_station sf2
                           ON ta2.train_static = sf2.train_static_id AND tk.depart_station = sf2.station_id
                       JOIN train_station st2
                           ON ta2.train_static = st2.train_static_id AND tk.arrive_station = st2.station_id
                  WHERE ps.people_id = (SELECT people_id FROM passenger WHERE passenger_id = ?)
                  AND tk.valid
                  AND NOT (sf2.depart_time + ta2.depart_date > st.arrive_time + ta.depart_date OR
                        st2.arrive_time + ta2.depart_date < sf.depart_time + ta.depart_date));
        """.trimIndent(), jsonArrayOf(departStationID, arriveStationID, trainID, passengerID))
        if (existent != null) {
            throw ServiceException("Travel plan conflict")
        }

        val trainTicket = getTrainTicketResult(trainID)

        val trainNotDepart = if (
                user.isAuthorizedAwait("admin") ||
                user.isAuthorizedAwait("buy-ticket-late")
        ) {
            true
        } else {
            val departStation = trainTicket.trainLine.stations.find { it.station == departStationID }
                    ?: throw ServiceException("no depart station in the train line")
            departStation.departTime > LocalDateTime.now()
        }

        if (!trainNotDepart) {
            throw ServiceException("permission denied, train have departed")
        }

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

        val ticketCreateByUser = if (
                user.isAuthorizedAwait("admin") ||
                user.isAuthorizedAwait("cancel-ticket-for-other")
        ) {
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

        val trainNotDepart = if (
                user.isAuthorizedAwait("admin") ||
                user.isAuthorizedAwait("cancel-ticket-late")
        ) {
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

    suspend fun getTicket(user: UserAuth, ticketID: Int): TicketInfo {
        val ticket = getTicket(ticketID)

        val canVisit = if (user.isAuthorizedAwait("admin")) {
            true
        } else {
            ticket.username == user.user.username
        }

        if (!canVisit) {
            throw ServiceException("permission denied, can not visit other's ticket")
        }

        val result = database.queryWithParamsAwait("""
            SELECT ticket.ticket_id                             tk_id,
                   train.train_id                               tk_tr_id,
                   train.depart_date                            tk_tr_depart_date,
                   train_static.train_static_id                 tk_tr_ts_id,
                   train_static.code                            tk_tr_ts_code,
                   train_static.type                            tk_tr_ts_type,
                   train_static.depart_station                  tk_tr_ts_depart_station,
                   train_static.arrive_station                  tk_tr_ts_arrive_station,
                   EXTRACT(epoch FROM train_static.depart_time) tk_tr_ts_depart_time,
                   EXTRACT(epoch FROM train_static.arrive_time) tk_tr_ts_arrive_time,
                   depart.station_id                            tk_depart_st_id,
                   depart.name                                  tk_depart_st_name,
                   depart.city                                  tk_depart_st_city,
                   depart.code                                  tk_depart_st_code,
                   arrive.station_id                            tk_arrive_st_id,
                   arrive.name                                  tk_arrive_st_name,
                   arrive.city                                  tk_arrive_st_city,
                   arrive.code                                  tk_arrive_st_code,
                   train.depart_date + depart_s.depart_time     tk_depart_time,
                   train.depart_date + arrive_s.arrive_time     tk_arrive_time,
                   ticket.seat_id                               tk_seat,
                   ticket.seat_num                              tk_seat_num,
                   passenger.passenger_id                       tk_ps_passenger_id,
                   passenger.name                               tk_ps_name,
                   passenger.people_id                          tk_ps_people_id,
                   passenger.phone                              tk_ps_phone,
                   passenger.username                           tk_ps_username,
                   ticket.username                              tk_username,
                   ticket.valid                                 tk_valid,
                   ticket.create_time                           tk_create_time,
                   ticket.update_time                           tk_update_time
            FROM (
                     SELECT *
                     FROM ticket_active
                     WHERE ticket_id = ?
                     UNION ALL
                     SELECT *
                     FROM ticket_history
                     WHERE ticket_id = ?
                 ) ticket
                     JOIN
                 (
                     SELECT *
                     FROM train_active
                     WHERE train_id = ?
                     UNION ALL
                     SELECT *
                     FROM train_history
                     WHERE train_id = ?
                 ) train ON ticket.train_id = train.train_id
                     JOIN train_static ON train.train_static = train_static.train_static_id
                     JOIN station depart ON depart.station_id = ticket.depart_station
                     JOIN train_station depart_s ON train_static.train_static_id = depart_s.train_static_id
                AND depart.station_id = depart_s.station_id
                     JOIN station arrive ON arrive.station_id = ticket.arrive_station
                     JOIN train_station arrive_s ON train_static.train_static_id = arrive_s.train_static_id
                AND arrive.station_id = arrive_s.station_id
                     JOIN passenger ON passenger.passenger_id = ticket.passenger_id;
        """.trimIndent(), jsonArrayOf(ticketID, ticketID, ticket.train, ticket.train))
        return result.rows.firstOrNull()?.toTicketInfo() ?: throw ServiceException("query error")
    }

    suspend fun getTickets(username: String, active: Boolean = true): List<TicketInfo> {
        val fill = if (active) "active" else "history"
        val result = database.queryWithParamsAwait("""
            SELECT ticket.ticket_id                             tk_id,
                   train.train_id                               tk_tr_id,
                   train.depart_date                            tk_tr_depart_date,
                   train_static.train_static_id                 tk_tr_ts_id,
                   train_static.code                            tk_tr_ts_code,
                   train_static.type                            tk_tr_ts_type,
                   train_static.depart_station                  tk_tr_ts_depart_station,
                   train_static.arrive_station                  tk_tr_ts_arrive_station,
                   EXTRACT(epoch FROM train_static.depart_time) tk_tr_ts_depart_time,
                   EXTRACT(epoch FROM train_static.arrive_time) tk_tr_ts_arrive_time,
                   depart.station_id                            tk_depart_st_id,
                   depart.name                                  tk_depart_st_name,
                   depart.city                                  tk_depart_st_city,
                   depart.code                                  tk_depart_st_code,
                   arrive.station_id                            tk_arrive_st_id,
                   arrive.name                                  tk_arrive_st_name,
                   arrive.city                                  tk_arrive_st_city,
                   arrive.code                                  tk_arrive_st_code,
                   train.depart_date + depart_s.depart_time     tk_depart_time,
                   train.depart_date + arrive_s.arrive_time     tk_arrive_time,
                   ticket.seat_id                               tk_seat,
                   ticket.seat_num                              tk_seat_num,
                   passenger.passenger_id                       tk_ps_passenger_id,
                   passenger.name                               tk_ps_name,
                   passenger.people_id                          tk_ps_people_id,
                   passenger.phone                              tk_ps_phone,
                   passenger.username                           tk_ps_username,
                   ticket.username                              tk_username,
                   ticket.valid                                 tk_valid,
                   ticket.create_time                           tk_create_time,
                   ticket.update_time                           tk_update_time
            FROM ticket_${fill} ticket
                     JOIN train_${fill} train ON ticket.train_id = train.train_id
                     JOIN train_static ON train.train_static = train_static.train_static_id
                     JOIN station depart ON depart.station_id = ticket.depart_station
                     JOIN train_station depart_s ON train_static.train_static_id = depart_s.train_static_id
                AND depart.station_id = depart_s.station_id
                     JOIN station arrive ON arrive.station_id = ticket.arrive_station
                     JOIN train_station arrive_s ON train_static.train_static_id = arrive_s.train_static_id
                AND arrive.station_id = arrive_s.station_id
                     JOIN passenger ON passenger.passenger_id = ticket.passenger_id
            WHERE ticket.username = ?
            ORDER BY ticket.create_time;
        """.trimIndent(), jsonArrayOf(username))
        return result.rows.map { it.toTicketInfo() }
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

    suspend fun getTrainTicketResult(trainID: Int): TrainTicketResult {
        val storeKey = "train:${trainID}"
        if (storeKey in store) {
            return store.get(storeKey)
        }
        return withContext(scope.coroutineContext) {

            val train = trainService.getTrain(trainID)
            val trainLine = trainLineService.getTrainLine(train)

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
                """.trimIndent(), jsonArrayOf(train.id)).rows.map { it.toTicket() }

            val trainTicketResult = TrainTicketResult(train, trainLine, tickets)

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