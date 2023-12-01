package me.ftmc.hotpot.items

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.HotpotTagsHelper
import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.blocks.HotpotPlaceableBlockEntity
import me.ftmc.hotpot.placeables.HotpotPlacedChopstick
import me.ftmc.hotpot.placeables.IHotpotPlaceable
import me.ftmc.hotpot.placeables.PlaceableRegistrar
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World


class HotpotChopstickItem :
    HotpotPlaceableBlockItem({
                                 PlaceableRegistrar.PLACEABLES.get(Identifier(MOD_ID, "placed_chopstick"))
                                     .createPlaceable()
                             }, Settings().maxCount(1)) {
    override fun shouldPlace(player: PlayerEntity?, hand: Hand, pos: BlockPosWithLevel): Boolean {
        return player?.isInSneakingPose ?: false
    }

    override fun setAdditional(
        hotpotPlaceableBlockEntity: HotpotPlaceableBlockEntity,
        pos: BlockPosWithLevel,
        placeable: IHotpotPlaceable,
        itemStack: ItemStack
    ) {
        if (placeable is HotpotPlacedChopstick) {
            placeable.setChopstickItemStack(itemStack)
        }
    }

    override fun use(level: World, player: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        val itemStack: ItemStack = player.getStackInHand(hand)
        var chopstickFoodItemStack: ItemStack
        return if (!getChopstickFoodItemStack(itemStack).also { chopstickFoodItemStack = it }.isEmpty) {
            if (chopstickFoodItemStack.isFood) {
                if (player.canConsume(true)) {
                    player.setCurrentHand(hand)
                    TypedActionResult.consume(itemStack)
                } else {
                    TypedActionResult.fail(itemStack)
                }
            } else {
                val notEdible: ItemStack = chopstickFoodItemStack.split(1)
                if (!player.inventory.insertStack(notEdible)) {
                    player.dropItem(notEdible, false)
                }
                setChopstickFoodItemStack(itemStack, chopstickFoodItemStack)
                TypedActionResult.pass(player.getStackInHand(hand))
            }
        } else TypedActionResult.pass(player.getStackInHand(hand))
    }

    override fun finishUsing(itemStack: ItemStack, level: World, livingEntity: LivingEntity): ItemStack {
        var chopstickFoodItemStack: ItemStack
        if (!getChopstickFoodItemStack(itemStack).also { chopstickFoodItemStack = it }.isEmpty) {
            if (chopstickFoodItemStack.isFood) {
                setChopstickFoodItemStack(itemStack, chopstickFoodItemStack.finishUsing(level, livingEntity))
            }
        }
        return itemStack
    }

    override fun getUseAction(itemStack: ItemStack): UseAction {
        var chopstickFoodItemStack: ItemStack
        if (!getChopstickFoodItemStack(itemStack).also { chopstickFoodItemStack = it }.isEmpty) {
            if (chopstickFoodItemStack.isFood) {
                return chopstickFoodItemStack.useAction
            }
        }
        return UseAction.NONE
    }

    override fun getMaxUseTime(itemStack: ItemStack): Int {
        var chopstickFoodItemStack: ItemStack
        return if (!getChopstickFoodItemStack(itemStack).also { chopstickFoodItemStack = it }.isEmpty) {
            if (chopstickFoodItemStack.isFood) 8 else 0
        } else 0
    }

//    fun initializeClient(consumer: Consumer<IClientItemExtensions?>) {
//        consumer.accept(object : IClientItemExtensions() {
//            val customRenderer: BlockEntityWithoutLevelRenderer
//                get() = HotpotModEntry.HOTPOT_BEWLR
//        })
//    }

    companion object {
        fun setChopstickFoodItemStack(chopstick: ItemStack, itemStack: ItemStack) {
            HotpotTagsHelper.updateHotpotTag(chopstick) { compoundTag ->
                compoundTag.put(
                    "ChopstickContent",
                    itemStack.writeNbt(NbtCompound())
                )
            }
        }

        fun getChopstickFoodItemStack(itemStack: ItemStack): ItemStack {
            var chopstickFoodItemStack: ItemStack = ItemStack.EMPTY
            if (itemStack.isOf(ItemRegistrar.HOTPOT_CHOPSTICK)
                && HotpotTagsHelper.hasHotpotTag(itemStack)
                && HotpotTagsHelper.getHotpotTag(itemStack)
                    .contains("ChopstickContent", NbtElement.COMPOUND_TYPE.toInt())
            ) {
                chopstickFoodItemStack =
                    ItemStack.fromNbt(HotpotTagsHelper.getHotpotTag(itemStack).getCompound("ChopstickContent"))
            }
            return chopstickFoodItemStack
        }
    }
}
