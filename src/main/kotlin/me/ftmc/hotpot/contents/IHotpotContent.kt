package me.ftmc.hotpot.contents

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.IHotpotSavableWIthSlot
import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.HotpotContents.emptyContent
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier
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
        val ID_FIXES = mapOf(
            "ItemStack" to "campfire_recipe_content",
            "BlastingItemStack" to "blasting_recipe_content",
            "Player" to "player_content",
            "Empty" to "empty_content"
        )

        fun fixID(id: String): String {
            return ID_FIXES[id] ?: id
        }

        fun loadAll(listTag: NbtList, list: MutableList<IHotpotContent>) {
            IHotpotSavableWIthSlot.loadAll(listTag, list.size) { compoundTag ->
                load(compoundTag, list::set)
            }
        }

        fun load(compoundTag: NbtCompound, consumer: (Int, IHotpotContent) -> Unit) {
            val content: IHotpotContent = ContentRegistrar.CONTENTS.get(
                Identifier(MOD_ID, fixID(compoundTag.getString("Type")))
            ).createContent()
            consumer(
                (compoundTag.getByte("Slot") and 255f.toInt().toByte()).toInt(),
                content.loadOrElseGet(compoundTag) { emptyContent.createContent() }
            )
        }

        fun saveAll(list: List<IHotpotContent>): NbtList {
            return IHotpotSavableWIthSlot.saveAll(list)
        }
    }
}
