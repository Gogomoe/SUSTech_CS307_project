package cs307.user

import cs307.Service
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.jdbc.JDBCAuthentication
import io.vertx.ext.auth.jdbc.JDBCAuthenticationOptions
import io.vertx.ext.auth.jdbc.JDBCHashStrategy
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.auth.authentication.authenticateAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import moe.gogo.ServiceException


class UserService : Service {

    private lateinit var database: JDBCClient
    private lateinit var auth: JDBCAuthentication
    private lateinit var hashStrategy: JDBCHashStrategy

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()

        val options = JDBCAuthenticationOptions()
        options.setAuthenticationQuery("""
                    SELECT password, password_salt FROM "user" WHERE USERNAME = ?
                """.trimIndent())
        hashStrategy = JDBCHashStrategy.createSHA512(registry.vertx())
        auth = JDBCAuthentication.create(database, hashStrategy, options)
    }

    fun auth(): AuthProvider = auth

    suspend fun getUserAuth(username: String, password: String): UserAuth {
        val authInfo = JsonObject().put("username", username).put("password", password)

        try {
            val userAuth = auth.authenticateAwait(authInfo)
            return getUserAuth(username, userAuth)
        } catch (e: Throwable) {
            if (e.message == "Invalid username/password") {
                throw ServiceException("Invalid username/password", e)
            }
            throw e
        }
    }

    suspend fun getUserAuth(username: String, auth: Auth): UserAuth {
        val user = getUser(username)

        val rolePerms = database.queryWithParamsAwait(
                """SELECT UR.role, RP.perm FROM roles_perms RP RIGHT JOIN user_roles UR ON UR.role = RP.role WHERE UR.username = ?""",
                jsonArrayOf(username)
        )
        val roles = rolePerms.results.map { it.getString(0) }.distinct().filterNotNull()
        val perms = rolePerms.results.map { it.getString(1) }.distinct().filterNotNull()

        return UserAuth(user, auth, roles, perms)
    }

    suspend fun getUser(username: String): User {
        val userInfo = database.querySingleWithParamsAwait(
                """SELECT * FROM user_info WHERE username = ?""",
                jsonArrayOf(username)
        ) ?: throw ServiceException("User does not exist")
        return User(username, userInfo.getString(1))
    }

}