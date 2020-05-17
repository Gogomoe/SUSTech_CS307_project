package cs307.api

import cs307.Controller
import cs307.Service
import cs307.ServiceRegistry
import cs307.api.handler.ServiceRegistryHandler
import cs307.api.handler.UserHandler
import cs307.database.DatabaseService
import cs307.memory.MemoryService
import cs307.passenger.PassengerController
import cs307.passenger.PassengerService
import cs307.ticket.TicketController
import cs307.ticket.TicketService
import cs307.train.StationController
import cs307.train.StationService
import cs307.train.TrainController
import cs307.train.TrainService
import cs307.train.trainline.TrainLineController
import cs307.train.trainline.TrainLineService
import cs307.user.UserController
import cs307.user.UserService
import io.vertx.core.http.HttpMethod
import io.vertx.core.impl.logging.Logger
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.coroutineScope

class APIServerVerticle : CoroutineVerticle() {

    lateinit var registry: ServiceRegistry

    lateinit var services: List<Pair<Class<out Service>, Service>>

    lateinit var router: Router

    private val log: Logger = LoggerFactory.getLogger(APIServerVerticle::class.java)

    override suspend fun start() = coroutineScope {

        registry = ServiceRegistry.create(vertx, context)

        services = listOf(
                DatabaseService::class.java to DatabaseService(),
                MemoryService::class.java to MemoryService(),
                UserService::class.java to UserService(),
                StationService::class.java to StationService(),
                TrainLineService::class.java to TrainLineService(),
                TicketService::class.java to TicketService(),
                TrainService::class.java to TrainService(),
                PassengerService::class.java to PassengerService()
        )

        services.forEach { (clazz, service) ->
            registry[clazz] = service
        }
        services.forEach { (_, service) ->
            service.start(registry)
        }

        router = Router.router(vertx)

        config.getJsonObject("webserver_config").getString("cors_origin")?.let {
            router.route().handler(CorsHandler.create(it)
                    .allowCredentials(true)
                    .allowedHeader("Access-Control-Allow-Method")
                    .allowedHeader("Access-Control-Allow-Origin")
                    .allowedHeader("Access-Control-Allow-Credentials")
                    .allowedHeader("Content-Type")
                    .allowedMethod(HttpMethod.GET)
                    .allowedMethod(HttpMethod.POST)
                    .allowedMethod(HttpMethod.HEAD)
                    .allowedMethod(HttpMethod.OPTIONS)
                    .allowedMethod(HttpMethod.DELETE)
            )
        }


        router.route().handler(BodyHandler.create())
        router.route().handler(
                SessionHandler.create(LocalSessionStore.create(vertx))
                        .setAuthProvider(registry[UserService::class.java].auth())
        )
        router.route().handler(UserHandler(registry[UserService::class.java], context))
        router.route().handler(ServiceRegistryHandler(registry))

        router.route().handler { routingContext ->
            routingContext.response().putHeader("Content-Type", "application/json")
            routingContext.next()
        }

        val controllers: List<Controller> = listOf(
                UserController(registry),
                TrainController(registry),
                TrainLineController(registry),
                StationController(registry),
                PassengerController(registry),
                TicketController(registry)
        )

        controllers.forEach {
            it.route(router)
        }

        Unit

    }

    fun router(): Router = router

    override suspend fun stop() {
        super.stop()
        for ((_, service) in services) {
            service.stop()
        }
    }

}