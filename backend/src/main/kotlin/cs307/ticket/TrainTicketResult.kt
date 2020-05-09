package cs307.ticket

import cs307.ServiceException
import cs307.train.SeatPriceCount
import cs307.train.Train
import cs307.train.trainline.TrainLine
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

private typealias StationID = Int
private typealias StationIndex = Int
private typealias SeatType = Int

class TrainTicketResult(val train: Train, trainLine: TrainLine, tickets: List<Ticket>) {


    private val lock = ReentrantLock()

    private val seatCount: Map<SeatType, Int>

    private val ticketRemain: MutableMap<SeatType, MutableMap<Pair<StationIndex, StationIndex>, Int>> = mutableMapOf()

    private val ticketPrice: Map<SeatType, IntArray>

    private val stationToIndex: Map<StationID, Int>

    private val seats: Map<SeatType, Array<BooleanArray>>

    init {

        val stations = trainLine.stations.size

        seatCount = mutableMapOf()
        trainLine.seatCount.forEach { (seatType, count) ->
            seatCount[seatType] = count
        }

        stationToIndex = mutableMapOf()
        trainLine.stations.forEachIndexed { index, trainStation ->
            stationToIndex[trainStation.station] = index
        }

        seats = mutableMapOf()
        ticketPrice = mutableMapOf()
        seatCount.forEach { (seatType, count) ->
            ticketRemain[seatType] = mutableMapOf()

            for (i in 0 until stations) {
                for (j in (i + 1) until stations) {
                    ticketRemain[seatType]!![i to j] = 0
                }
            }

            ticketPrice[seatType] = IntArray(stations)
            for (i in 0 until stations) {
                ticketPrice[seatType]!![i] = trainLine.stations[i].prices[seatType]!!
            }

            seats[seatType] = Array(count) { BooleanArray(stations - 1) }
        }

        tickets.filter { it.valid }.forEach { ticket ->
            val seat = seats[ticket.seatType]!![ticket.seatNum]
            for (i in stationToIndex[ticket.departStation]!! until stationToIndex[ticket.arriveStation]!!) {
                seat[i] = true
            }
        }

        seats.forEach { (seatType, seatsOfTypes) ->
            seatsOfTypes.forEach {
                releaseTickets(seatType, it)
            }
        }
    }

    fun generateTicket(departStationID: StationID, arriveStationID: StationID, seatType: SeatType): Int {
        if (departStationID !in stationToIndex || arriveStationID !in stationToIndex) {
            throw ServiceException("station not in the train line")
        }
        if (seatType !in seatCount.keys) {
            throw ServiceException("seat type not in the train")
        }
        lock.tryLock(3000) {
            val departIndex = stationToIndex[departStationID]!!
            val arriveIndex = stationToIndex[arriveStationID]!!
            val seatsOfTypes = seats[seatType]!!
            if (departIndex >= arriveIndex) {
                throw ServiceException("reverse ticket")
            }
            if (ticketRemain[seatType]!![departIndex to arriveIndex] == 0) {
                throw ServiceException("no tickets remain")
            }
            find_seat@ for (seatNum in seatsOfTypes.indices) {
                //try to find seat
                val seat = seatsOfTypes[seatNum]
                for (i in departIndex until arriveIndex) {
                    if (seat[i]) {
                        continue@find_seat
                    }
                }
                // find a seat
                lockTickets(seatType, seat)
                for (i in departIndex until arriveIndex) {
                    seat[i] = true
                }
                releaseTickets(seatType, seat)

                return seatNum
            }
            throw ServiceException("no tickets remain")
        }
    }

    fun retrieveTicket(departStationID: StationID, arriveStationID: StationID, seatType: SeatType, seatNum: Int) {
        lock.tryLock(5000) {
            val departIndex = stationToIndex[departStationID]!!
            val arriveIndex = stationToIndex[arriveStationID]!!
            val seat = seats[seatType]!![seatNum]
            lockTickets(seatType, seat)
            for (i in departIndex until arriveIndex) {
                seat[i] = false
            }
            releaseTickets(seatType, seat)
        }
    }

    fun ticketInfo(departStationID: StationID, arriveStationID: StationID): Map<SeatType, SeatPriceCount> {
        if (departStationID !in stationToIndex || arriveStationID !in stationToIndex) {
            throw ServiceException("station not in the train line")
        }
        return ticketRemain.mapValues {
            val departStationIndex = stationToIndex[departStationID]!!
            val arriveStationIndex = stationToIndex[arriveStationID]!!
            val count = it.value[departStationIndex to arriveStationIndex]!!
            val price = ticketPrice[it.key]!![departStationIndex] - ticketPrice[it.key]!![arriveStationIndex]
            SeatPriceCount(price, count)
        }
    }

    private fun releaseTickets(seatType: SeatType, seat: BooleanArray) {
        var L = 0
        while (L < seat.size) {
            while (L < seat.size && seat[L]) {
                L++
            }
            if (L == seat.size) {
                break
            }
            var R = L + 1
            while (R < seat.size && !seat[L]) {
                R++
            }
            for (i in L..R) {
                for (j in (i + 1)..R) {
                    ticketRemain[seatType]!![i to j] = ticketRemain[seatType]!![i to j]!! + 1
                }
            }
            L = R
        }
    }

    private fun lockTickets(seatType: SeatType, seat: BooleanArray) {
        var L = 0
        while (L < seat.size) {
            while (L < seat.size && seat[L]) {
                L++
            }
            if (L == seat.size) {
                break
            }
            var R = L + 1
            while (R < seat.size && !seat[L]) {
                R++
            }
            for (i in L..R) {
                for (j in (i + 1)..R) {
                    ticketRemain[seatType]!![i to j] = ticketRemain[seatType]!![i to j]!! - 1
                }
            }
            L = R
        }
    }

    private inline fun <T> Lock.tryLock(mills: Int, action: () -> T): T {
        if (tryLock(mills.toLong(), TimeUnit.MILLISECONDS)) {
            try {
                return action()
            } finally {
                unlock()
            }
        } else {
            throw ServiceException("Resource busy")
        }
    }


}