package me.ftmc.hotpot.forge.net.minecraftforge.client.model.data

import com.google.common.base.Preconditions
import org.jetbrains.annotations.Contract
import java.util.*


class ModelData private constructor(private val properties: Map<(Any) -> Boolean, Any>) {

    fun getProperties(): Set<(Any) -> Boolean> {
        return properties.keys
    }

    fun has(property: (Any) -> Boolean): Boolean {
        return properties.containsKey(property)
    }

    operator fun <T : Any> get(property: (Any) -> Boolean): T? {
        return properties[property] as T?
    }

    fun derive(): Builder {
        return Builder(this)
    }

    class Builder constructor(parent: ModelData?) {
        private val properties: MutableMap<(Any) -> Boolean, Any> = IdentityHashMap()

        init {
            if (parent != null) {
                properties.putAll(parent.properties)
            }
        }

        @Contract("_, _ -> this")
        fun <T : Any> with(property: (Any) -> Boolean, value: T): Builder {
            Preconditions.checkState(property(value), "The provided value is invalid for this property.")
            properties[property] = value
            return this
        }

        @Contract("-> new")
        fun build(): ModelData {
            return ModelData(Collections.unmodifiableMap(properties))
        }
    }

    companion object {
        val EMPTY = builder().build()
        fun builder(): Builder {
            return Builder(null)
        }
    }
}
