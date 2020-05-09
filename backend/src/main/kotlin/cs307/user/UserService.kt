package cs307.user

import cs307.Service
import cs307.ServiceException
import cs307.ServiceRegistry
import cs307.database.DatabaseService
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.jdbc.JDBCAuthentication
import io.vertx.ext.auth.jdbc.JDBCAuthenticationOptions
import io.vertx.ext.auth.jdbc.JDBCHashStrategy
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.executeBlockingAwait
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.auth.authentication.authenticateAwait
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.from
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.where
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.varchar
import org.postgresql.util.PSQLException


class UserService : Service {

    private lateinit var database: JDBCClient
    private lateinit var auth: JDBCAuthentication
    private lateinit var hashStrategy: JDBCHashStrategy

    private lateinit var orm: Database
    private lateinit var vertx: Vertx

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()

        val options = JDBCAuthenticationOptions()
        options.setAuthenticationQuery("""
                    SELECT password, password_salt FROM "user" WHERE USERNAME = ?
                """.trimIndent())
        hashStrategy = JDBCHashStrategy.createSHA512(registry.vertx())
        auth = JDBCAuthentication.create(database, hashStrategy, options)

        orm = with(registry[DatabaseService::class.java].config()) {
            Database.Companion.connect(
                    getString("url"),
                    getString("driver_class"),
                    getString("user"),
                    getString("password")
            )
        }
        vertx = registry.vertx()
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

    suspend fun getAllUser(): List<UserWithPermissionInfo> {
        val result = database.queryAwait("""
            SELECT "user".username,
                   avatar,
                   user_roles.role,
                   perm
            FROM "user"
                     JOIN user_info ON "user".username = user_info.username
                     LEFT JOIN user_roles ON "user".username = user_roles.username
                     LEFT JOIN roles_perms rp on user_roles.role = rp.role;
        """.trimIndent())
        val map = mutableMapOf<String, UserWithPermissionInfo>()
        result.rows.forEach {
            val username = it.getString("username")
            val avatar = it.getString("avatar")
            val role = it.getString("role")
            val perm = it.getString("perm")

            val user = map.getOrPut(username) {
                UserWithPermissionInfo(User(username, avatar), mutableListOf(), mutableListOf())
            }
            if (role != null && role !in user.roles) {
                (user.roles as MutableList).add(role)
            }
            if (perm != null && perm !in user.permissions) {
                (user.permissions as MutableList).add(perm)
            }
        }
        return map.values.toList()
    }

    suspend fun getUser(username: String): User {
        val userInfo = database.querySingleWithParamsAwait(
                """SELECT * FROM user_info WHERE username = ?""",
                jsonArrayOf(username)
        ) ?: throw ServiceException("User does not exist")
        return User(username, userInfo.getString(1))
    }

    suspend fun signUpUser(username: String, password: String) {

        if (database.querySingleWithParamsAwait("""
                SELECT username FROM "user" WHERE username = ?;
            """.trimIndent(), jsonArrayOf(username)) != null) {
            throw ServiceException("User has been existent")
        }

        val salt = hashStrategy.generateSalt()
        val hashedPassword = hashStrategy.computeHash(password, salt, -1)

        database.updateWithParamsAwait("""
            INSERT INTO "user" (username, password, password_salt) VALUES (?,?,?);
        """.trimIndent(), jsonArrayOf(username, hashedPassword, salt))

        database.updateWithParamsAwait("""
            INSERT INTO user_info (username, avatar) VALUES (?,null);
        """.trimIndent(), jsonArrayOf(username))
    }

    suspend fun endowRoleForUser(username: String, role: String) {
        try {
            database.updateWithParamsAwait("""
                INSERT INTO user_roles (username, role) VALUES (?,?);
            """.trimIndent(), jsonArrayOf(username, role))
        } catch (e: PSQLException) {
            if ((e.message ?: "").contains("重复键违反唯一约束")||
                    (e.message ?: "").contains("unique constraint")) {
                throw ServiceException("this user is already this role")
            } else {
                throw e
            }
        }
    }

    suspend fun cancelRoleForUser(username: String, role: String) {
        val result = database.updateWithParamsAwait("""
                DELETE FROM user_roles WHERE username = ? and role = ?;
            """.trimIndent(), jsonArrayOf(username, role))
        if (result.updated == 0) {
            throw ServiceException("no such (user,role)")
        }
    }

    object UserInfoMapping : Table<Nothing>("user_info".trim()) {
        val username by varchar("username")
        val avatar by varchar("avatar")
    }

    private suspend fun getUserByORM(username: String): User {
        return vertx.executeBlockingAwait { promise ->
            val result = orm.from(UserInfoMapping)
                    .select(UserInfoMapping.columns)
                    .where { (UserInfoMapping.username eq username) }
                    .map {
                        User(it[UserInfoMapping.username]!!, it[UserInfoMapping.avatar])
                    }
            promise.complete(result.firstOrNull())
        } ?: throw ServiceException("User does not exist")
    }

}