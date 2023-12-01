package me.ftmc.hotpot.placeables

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.IHotpotSavableWIthSlot
import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.blocks.HotpotPlaceableBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction
import kotlin.experimental.and


interface IHotpotPlaceable : IHotpotSavableWIthSlot<IHotpotPlaceable> {
    fun interact(
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    )

    fun takeOutContent(
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    ): ItemStack

    fun onRemove(hotpotPlateBlockEntity: HotpotPlaceableBlockEntity, pos: BlockPosWithLevel)
    fun render(
        context: BlockEntityRendererFactory.Context,
        hotpotBlockEntity: HotpotPlaceableBlockEntity,
        partialTick: Float,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int
    )

    fun getCloneItemStack(hotpotPlateBlockEntity: HotpotPlaceableBlockEntity, level: BlockPosWithLevel): ItemStack
    fun tryPlace(pos: Int, direction: Direction): Boolean
    fun getPos(): List<Int>
    val anchorPos: Int

    fun isConflict(pos: Int): Boolean

    companion object {

        val ID_FIXES = mapOf(
            "Empty" to "empty_placeable",
            "LongPlate" to "long_plate",
            "SmallPlate" to "small_plate",
            "PlacedChopstick" to "placed_chopstick"
        )

        fun fixID(id: String): String {
            return ID_FIXES[id] ?: id
        }

        fun getSlotX(slot: Int): Float {
            return if (2 and slot > 0) 0.5f else 0f
        }

        fun getSlotZ(slot: Int): Float {
            return if (1 and slot > 0) 0.5f else 0f
        }

        fun loadAll(listTag: NbtList, list: DefaultedList<IHotpotPlaceable>) {
            IHotpotSavableWIthSlot.loadAll(listTag, list.size) { compoundTag ->
                load(compoundTag) { slot, placeable ->
                    if (placeable is HotpotEmptyPlaceable || slot == placeable.anchorPos
                    ) {
                        list[slot] = placeable
                    }
                }
            }
        }

        fun load(compoundTag: NbtCompound, consumer: (Int, IHotpotPlaceable) -> Unit) {
            val placeable: IHotpotPlaceable =
                PlaceableRegistrar.PLACEABLES.get(Identifier(MOD_ID, fixID(compoundTag.getString("Type"))))
                    .createPlaceable()
            consumer(
                (compoundTag.getByte("Slot") and 255.toByte()).toInt(),
                placeable.loadOrElseGet(compoundTag) { HotpotPlaceables.emptyPlaceable.createPlaceable() }
            )
        }

        fun saveAll(list: DefaultedList<IHotpotPlaceable>): NbtList {
            return IHotpotSavableWIthSlot.saveAll(list)
        }
    }
}
