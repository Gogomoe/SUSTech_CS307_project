package cs307.user

import cs307.CoroutineController
import cs307.ServiceException
import cs307.ServiceRegistry
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf

class UserController(registry: ServiceRegistry) : CoroutineController() {

    private val service = registry[UserService::class.java]

    override fun route(router: Router) {
        router.get("/session").coroutineHandler(::handleGetSession)
        router.post("/session").coroutineHandler(::handleLogin)
        router.get("/user/:username").coroutineHandler(::handleGetUser)
        router.delete("/session").coroutineHandler(::handleLogOut)
        router.get("/users").coroutineHandler(::handleGetAllUser)
        router.post("/user").coroutineHandler(::handleSignUp)
        router.post("/user/:username/role").coroutineHandler(::handleEndowRole)
        router.delete("/user/:username/role/:role").coroutineHandler(::handleCancelRole)
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
        val params = context.bodyAsJson
        val username = params.getString("username")
        val password = params.getString("password")

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

    suspend fun handleLogOut(context: RoutingContext) {

        val user = context.getUser()
        if (user != null) {
            context.setUser(null)
            context.put("user", null)
            context.session().put("user", null)
            context.session().regenerateId()
        }

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

    suspend fun handleGetAllUser(context: RoutingContext) {
        val user = context.getUser()
        val havePermission = user?.isAuthorizedAwait("admin") ?: false
        if (!havePermission) {
            throw ServiceException("permission denied")
        }

        val result = service.getAllUser()

        context.success(jsonObject = jsonObjectOf(
                "users" to JsonArray(result.map { it.toJson() })
        ))
    }

    suspend fun handleSignUp(context: RoutingContext) {
        val params = context.bodyAsJson
        val username = params.getString("username")
        val password = params.getString("password")

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw ServiceException("Username or password is empty")
        }

        service.signUpUser(username, password)
        handleLogin(context)
    }

    suspend fun handleEndowRole(context: RoutingContext) {
        val user = context.getUser() ?: throw ServiceException("not login")
        if (!user.isAuthorizedAwait("admin")) {
            throw ServiceException("No right")
        }

        val username = context.request().getParam("username")
        val role = context.bodyAsJson.getString("role")

        service.endowRoleForUser(username, role)

        context.success()
    }

    suspend fun handleCancelRole(context: RoutingContext) {
        val user = context.getUser() ?: throw ServiceException("not login")
        if (!user.isAuthorizedAwait("admin")) {
            throw ServiceException("No right")
        }

        val params = context.request()
        val username = params.getParam("username")
        val role = params.getParam("role")

        service.cancelRoleForUser(username, role)

        context.success()
    }
}