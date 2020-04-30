package cs307.memory

import cs307.Service
import cs307.ServiceRegistry

class MemoryService : Service {

    private val store: MutableMap<String, Any> = mutableMapOf()

    override suspend fun start(registry: ServiceRegistry) {
    }

    override suspend fun stop() {
        store.clear()
    }

    fun <T> get(key: String) = store[key] as T

    operator fun set(key: String, value: Any) {
        store[key] = value
    }

    operator fun contains(key: String) = store.containsKey(key)

}