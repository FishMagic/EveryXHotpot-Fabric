package me.ftmc.hotpot.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

interface OnDeathEventCallback {
    companion object {
        val EVENT: Event<OnDeathEventCallback> =
            EventFactory.createArrayBacked(OnDeathEventCallback::class.java) { listeners ->
                object : OnDeathEventCallback {
                    override fun onDeath(entity: ServerPlayerEntity, damageSource: DamageSource) {
                        for (listener in listeners) {
                            listener.onDeath(entity, damageSource)
                        }
                    }
                }
            }
    }

    fun onDeath(entity: ServerPlayerEntity, damageSource: DamageSource)
}