package cs307.api.handler

import cs307.ServiceRegistry
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class ServiceRegistryHandler(private val registry: ServiceRegistry) : Handler<RoutingContext> {

    override fun handle(context: RoutingContext) {
        context.put("ServiceRegistry", registry)
        context.next()
    }

}
