package me.ftmc.hotpot.placeables

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotPlaceableBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Hand
import net.minecraft.util.math.Direction


class HotpotEmptyPlaceable : IHotpotPlaceable {
    override fun load(compoundTag: NbtCompound): IHotpotPlaceable {
        return this
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        return compoundTag
    }

    override fun isValid(compoundTag: NbtCompound): Boolean {
        return true
    }

    override val id: String
        get() = "empty_placeable"

    override fun interact(
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    ) {
    }

    override fun takeOutContent(
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    ): ItemStack {
        return ItemStack.EMPTY
    }

    override fun onRemove(hotpotPlateBlockEntity: HotpotPlaceableBlockEntity, pos: BlockPosWithLevel) {}
    override fun render(
        context: BlockEntityRendererFactory.Context,
        hotpotBlockEntity: HotpotPlaceableBlockEntity,
        partialTick: Float,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int
    ) {
    }

    override fun getCloneItemStack(
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        level: BlockPosWithLevel
    ): ItemStack {
        return ItemStack.EMPTY
    }

    override fun tryPlace(pos: Int, direction: Direction): Boolean {
        return false
    }

    override fun getPos(): List<Int> {
        return listOf()
    }

    override val anchorPos: Int
        get() = 0

    override fun isConflict(pos: Int): Boolean {
        return false
    }
}
