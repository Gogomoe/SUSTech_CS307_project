package cs307.passenger

import cs307.Service
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait

class PassengerService : Service {

    private lateinit var database: JDBCClient

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()
    }

    suspend fun addPassenger(name: String, idNumber: String, phone: String, username: String): Passenger {
        if (!isValidPassenger(idNumber, phone)) {
            throw ServiceException("invalid id number or phone")
        }

        val result = database.updateWithParamsAwait("""
            INSERT INTO passenger (name, people_id, phone, username) VALUES (?,?,?,?);
        """.trimIndent(), jsonArrayOf(name, idNumber, phone, username))

        return result.keys.toPassenger()
    }

    suspend fun modifyPassenger(id: Int, name: String, idNumber: String, phone: String, username: String, isAdmin: Boolean): Passenger {
        if (!isValidPassenger(idNumber, phone)) {
            throw ServiceException("invalid id number or phone")
        }

        val result = if (isAdmin) {
            database.updateWithParamsAwait("""
                UPDATE passenger SET name = ?, people_id = ?, phone = ?
                WHERE passenger_id = ?;
            """.trimIndent(), jsonArrayOf(name, idNumber, phone, id))
        } else {
            database.updateWithParamsAwait("""
                UPDATE passenger SET name = ?, people_id = ?, phone = ?
                WHERE passenger_id = ? and username = ?;
            """.trimIndent(), jsonArrayOf(name, idNumber, phone, id, username))
        }

        if (result.keys.size() == 0) {
            throw ServiceException("the passenger is invisible for you")
        }

        return result.keys.toPassenger()
    }

    suspend fun deletePassenger(id: Int, username: String, isAdmin: Boolean): Passenger {
        val result = if (isAdmin) {
            database.updateWithParamsAwait("""
                DELETE FROM passenger where passenger_id = ?;
            """.trimIndent(), jsonArrayOf(id))
        } else {
            database.updateWithParamsAwait("""
                DELETE FROM passenger where passenger_id = ? and username = ?;
            """.trimIndent(), jsonArrayOf(id,username))
        }

        return result.keys.toPassenger()
    }

    suspend fun getAllPassengers(username: String):List<Passenger>{
        val result = database.queryWithParamsAwait("""
            SELECT ps.passenger_id        ps_passenger_id, 
                   ps.name                ps_name,
                   ps.people_id           ps_people_id,
                   ps.phone               ps_phone,
                   ps.username            ps_username
            FROM passenger ps
            WHERE ps.username = ?;
        """.trimIndent(), jsonArrayOf(username))

        return result.rows.map { it.toPassenger() }
    }

    private fun isValidPassenger(idNumber: String, phone: String): Boolean {
        return true//TODO
    }
}