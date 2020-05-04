package cs307.user

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf

data class User(
        val username: String,
        val avatar: String?
)

typealias Auth = io.vertx.ext.auth.User

val Auth.username: String
    get() = this.principal().getString("username")!!

data class UserAuth(
        val user: User,
        val auth: Auth,
        val roles: List<String>,
        val permissions: List<String>
) {
    suspend fun isAuthorizedAwait(authority: String): Boolean {
        return if (authority.startsWith("role:")) {
            authority.substring(5) in roles
        } else {
            authority in permissions
        }
    }
}

fun RoutingContext.getUser(): UserAuth? = this.session().get("user")

fun User.toJson(): JsonObject = jsonObjectOf(
        "username" to username,
        "avatar" to (avatar ?: defaultAvatar())
)

fun UserAuth.toJson(): JsonObject = jsonObjectOf(
        "username" to user.username,
        "avatar" to (user.avatar ?: defaultAvatar()),
        "roles" to json { array(roles) },
        "perms" to json { array(permissions) }
)

fun defaultAvatar(): String = "/avatar/default.jpg"
