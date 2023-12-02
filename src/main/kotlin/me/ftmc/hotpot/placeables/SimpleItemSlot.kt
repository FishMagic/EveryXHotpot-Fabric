package me.ftmc.hotpot.placeables

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.forge.net.minecraft.item.copyWithCount
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import kotlin.math.max
import kotlin.math.min


class SimpleItemSlot {
    private var itemSlot: ItemStack = ItemStack.EMPTY
    fun renderSlot(
        context: BlockEntityRendererFactory.Context,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int
    ) {
        context.itemRenderer.renderItem(
            itemSlot,
            ModelTransformation.Mode.FIXED,
            combinedLight,
            combinedOverlay,
            poseStack,
            bufferSource,
            ModelTransformation.Mode.GROUND.ordinal
        )
    }

    val renderCount: Int
        get() = if (itemSlot.isEmpty) 0 else max(1, itemSlot.count / 16)

    fun addItem(itemStack: ItemStack): Boolean {
        if (itemSlot.isEmpty) {
            itemSlot = itemStack.copy()
            itemStack.count = 0
            return true
        } else if (ItemStack.canCombine(itemStack, itemSlot)) {
            moveItemWithCount(itemStack)
            return itemStack.isEmpty
        }
        return false
    }

    private fun moveItemWithCount(itemStack: ItemStack) {
        val j: Int = min(itemStack.count, itemSlot.maxCount - itemSlot.count)
        if (j > 0) {
            itemSlot.increment(j)
            itemStack.decrement(j)
        }
    }

    fun takeItem(consume: Boolean): ItemStack {
        return if (consume) itemSlot.split(1) else itemSlot.copyWithCount(1)
    }

    val isEmpty: Boolean
        get() = itemSlot.isEmpty

    fun dropItem(pos: BlockPosWithLevel) {
        val copied = itemSlot.copy()
        itemSlot.count = 0
        pos.dropItemStack(copied)
    }

    fun save(compoundTag: NbtCompound): NbtCompound {
        itemSlot.writeNbt(compoundTag)
        return compoundTag
    }

    fun load(compoundTag: NbtCompound) {
        itemSlot = ItemStack.fromNbt(compoundTag)
    }
}
