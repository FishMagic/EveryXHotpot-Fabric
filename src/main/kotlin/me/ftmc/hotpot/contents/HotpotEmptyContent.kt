package me.ftmc.hotpot.contents

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound


class HotpotEmptyContent : IHotpotContent {
    override fun placed(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {}
    override fun render(
        context: BlockEntityRendererFactory.Context,
        hotpotBlockEntity: HotpotBlockEntity,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int,
        offset: Float,
        waterline: Float
    ) {
    }

    override fun takeOut(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): ItemStack {
        return ItemStack.EMPTY
    }

    override fun onOtherContentUpdate(
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ) {
    }

    override fun tick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Boolean {
        return false
    }

    override fun load(compoundTag: NbtCompound): IHotpotContent {
        return this
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        return compoundTag
    }

    override fun isValid(compoundTag: NbtCompound): Boolean {
        return true
    }

    override val id: String
        get() = "empty_content"

    override fun toString(): String {
        return id
    }
}
