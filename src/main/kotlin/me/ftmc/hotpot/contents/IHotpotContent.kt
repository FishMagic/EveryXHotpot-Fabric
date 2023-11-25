package me.ftmc.hotpot.contents

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.IHotpotSavableWIthSlot
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import kotlin.experimental.and


interface IHotpotContent : IHotpotSavableWIthSlot<IHotpotContent> {
    fun placed(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel)
    fun render(
        context: BlockEntityRendererFactory.Context,
        hotpotBlockEntity: HotpotBlockEntity,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int,
        offset: Float,
        waterline: Float
    )

    fun tick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Boolean
    fun takeOut(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): ItemStack
    fun onOtherContentUpdate(content: IHotpotContent, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel)

    companion object {
        fun loadAll(listTag: NbtList, list: MutableList<IHotpotContent>) {
            IHotpotSavableWIthSlot.loadAll(listTag, list.size) { compoundTag ->
                load(compoundTag, list::set)
            }
        }

        fun load(compoundTag: NbtCompound, consumer: (Int, IHotpotContent) -> Unit) {
            val content: IHotpotContent = HotpotContents.getContentOrElseEmpty(compoundTag.getString("Type"))()
            consumer(
                (compoundTag.getByte("Slot") and 255f.toInt().toByte()).toInt(),
                content.loadOrElseGet(compoundTag, HotpotContents.emptyContent)
            )
        }

        fun saveAll(list: List<IHotpotContent>): NbtList {
            return IHotpotSavableWIthSlot.saveAll(list)
        }
    }
}
