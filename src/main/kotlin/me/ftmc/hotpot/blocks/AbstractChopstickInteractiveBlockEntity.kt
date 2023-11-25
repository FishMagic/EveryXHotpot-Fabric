package me.ftmc.hotpot.blocks

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.items.HotpotChopstickItem
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos


abstract class AbstractChopstickInteractiveBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState
) : BlockEntity(type, pos, state) {
    fun interact(
        hitSection: Int,
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        selfPos: BlockPosWithLevel
    ) {
        if (itemStack.isOf(EveryXHotpot.HOTPOT_CHOPSTICK)) {
            var chopstickFoodItemStack: ItemStack = HotpotChopstickItem.getChopstickFoodItemStack(itemStack)
            chopstickFoodItemStack = if (chopstickFoodItemStack.isEmpty) tryTakeOutContentViaChopstick(
                hitSection,
                selfPos
            ) else tryPlaceContentViaChopstick(hitSection, player, hand, chopstickFoodItemStack, selfPos)
            if (chopstickFoodItemStack.item.canBeNested()) {
                HotpotChopstickItem.setChopstickFoodItemStack(itemStack, chopstickFoodItemStack)
            } else {
                selfPos.dropItemStack(chopstickFoodItemStack)
                HotpotChopstickItem.setChopstickFoodItemStack(itemStack, ItemStack.EMPTY)
            }
            return
        }
        tryPlaceContentViaInteraction(hitSection, player, hand, itemStack, selfPos)
    }

    abstract fun tryPlaceContentViaChopstick(
        hitSection: Int,
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        selfPos: BlockPosWithLevel
    ): ItemStack

    abstract fun tryPlaceContentViaInteraction(
        hitSection: Int,
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        selfPos: BlockPosWithLevel
    )

    abstract fun tryTakeOutContentViaChopstick(hitSection: Int, pos: BlockPosWithLevel): ItemStack
}

