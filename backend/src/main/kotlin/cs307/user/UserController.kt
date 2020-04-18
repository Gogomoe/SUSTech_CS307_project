package cs307.user

import cs307.CoroutineController
import cs307.ServiceRegistry
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf
import moe.gogo.ServiceException

class UserController(registry: ServiceRegistry) : CoroutineController() {

    private val service = registry[UserService::class.java]

    override fun route(router: Router) {
        router.get("/session").coroutineHandler(::handleGetSession)
        router.post("/session").coroutineHandler(::handleLogin)
        router.get("/user/:username").coroutineHandler(::handleGetUser)
    }

    suspend fun handleGetSession(context: RoutingContext) {
        val user = context.getUser()
        val json = if (user == null) {
            jsonObjectOf(
                    "session" to false
            )
        } else {
            jsonObjectOf(
                    "session" to true,
                    "user" to user.toJson()
            )
        }

        context.success(json)
    }

    suspend fun handleLogin(context: RoutingContext) {
        val params = context.request().formAttributes()
        val username = params.get("username")
        val password = params.get("password")

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw ServiceException("Username or password is empty")
        }

        val user = service.getUserAuth(username, password)
        context.setUser(user.auth)
        context.put("user", user)
        context.session().regenerateId()
        context.session().put("user", user)

        context.success()
    }

    suspend fun handleGetUser(context: RoutingContext) {
        val request = context.request()
        val username = request.getParam("username") ?: throw ServiceException("Username not found")

        val user = service.getUser(username)

        context.success(
                jsonObjectOf(
                        "user" to user.toJson()
                )
        )
    }


}