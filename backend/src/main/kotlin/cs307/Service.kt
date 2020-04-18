package cs307

interface Service {

    suspend fun start(registry: ServiceRegistry)

    suspend fun stop() {}

}