package cs307.api.handler

import cs307.user.Auth
import cs307.user.UserService
import cs307.user.getUser
import cs307.user.username
import io.vertx.core.Context
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserHandler(private val authService: UserService, context: Context) : Handler<RoutingContext> {

    private val coroutineScope: CoroutineScope = CoroutineScope(context.dispatcher())

    override fun handle(routingContext: RoutingContext) {
        val auth: Auth? = routingContext.user()
        if (auth == null || routingContext.getUser() != null) {
            routingContext.next()
            return
        }
        coroutineScope.launch {
            routingContext.session().put("user", authService.getUserAuth(auth.username, auth))
            routingContext.next()
        }
    }

}