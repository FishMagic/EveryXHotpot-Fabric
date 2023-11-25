package me.ftmc.hotpot.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

interface FinishUsingItemCallback {
    companion object {
        val EVENT: Event<FinishUsingItemCallback> =
            EventFactory.createArrayBacked(FinishUsingItemCallback::class.java) { listeners ->
                object : FinishUsingItemCallback {
                    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
                        for (listener in listeners) {
                            listener.finishUsing(stack, world, user)
                        }

                        return stack
                    }
                }
            }
    }

    fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack
}