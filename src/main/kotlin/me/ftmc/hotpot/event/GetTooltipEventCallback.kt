package me.ftmc.hotpot.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

interface GetTooltipEventCallback {
    companion object {
        val EVENT: Event<GetTooltipEventCallback> =
            EventFactory.createArrayBacked(GetTooltipEventCallback::class.java) { listeners ->
                object : GetTooltipEventCallback {
                    override fun getTooltip(
                        stack: ItemStack,
                        list: MutableList<Text>,
                        player: PlayerEntity?,
                        context: TooltipContext
                    ): MutableList<Text> {
                        for (listener in listeners) {
                            listener.getTooltip(stack, list, player, context)
                        }
                        return list
                    }
                }
            }
    }

    //    public List<Text> getTooltip(@Nullable PlayerEntity player, TooltipContext context) {
    fun getTooltip(
        stack: ItemStack,
        list: MutableList<Text>,
        player: PlayerEntity?,
        context: TooltipContext
    ): MutableList<Text>
}