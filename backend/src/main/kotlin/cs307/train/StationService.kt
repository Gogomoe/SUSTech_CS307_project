package cs307.train

import cs307.Service
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait

class StationService : Service {

    lateinit var database: JDBCClient

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()
    }

    suspend fun searchStations(name: String): List<Station> {
        if (name.length < 2) {
            throw ServiceException("the name of station or city is too short, it must greater than 2")
        }
        return database.queryWithParamsAwait("""
            SELECT station_id st_id,
                   name       st_name,
                   city       st_city,
                   code       st_code
            FROM station
            WHERE name LIKE ?
               OR city LIKE ?;
        """.trimIndent(), jsonArrayOf("%${name}%", "%${name}%")).rows.map { it.toStation() }
    }

    suspend fun addStation(name: String, city: String, code: String): Int {
        if (name.length < 2 || name.length > 20) {
            throw ServiceException("the name of station is too short or too long")
        }
        if (city.length < 2 || city.length > 20) {
            throw ServiceException("the name of city is too short or too long")
        }
        if (code.length < 2 || code.length > 5) {
            throw ServiceException("the station code is too short or too long")
        }
        val result = database.updateWithParamsAwait("""
            INSERT INTO station(station_id, name, city, code)
            VALUES ((SELECT MAX(station_id) FROM station) + 1, ?, ?, ?);
        """.trimIndent(), jsonArrayOf(name, city, code))

        if (result.updated == 1) {
            return result.keys.getInteger(0)
        } else {
            throw ServiceException("insert station fail")
        }
    }

    suspend fun deleteStation(station: Int) {
        val result = database.updateWithParamsAwait("""
            DELETE FROM station WHERE station_id = ?;
        """.trimIndent(), jsonArrayOf(station))
        if (result.updated != 1) {
            throw ServiceException("delate station fail")
        }
    }

}