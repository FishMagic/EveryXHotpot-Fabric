package me.ftmc.hotpot.placeables

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.blocks.HotpotPlaceableBlockEntity
import me.ftmc.hotpot.forge.net.minecraft.client.renderer.block.renderModel
import me.ftmc.hotpot.forge.net.minecraftforge.client.model.data.ModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis


class HotpotLongPlate : IHotpotPlaceable {
    override val anchorPos: Int
        get() = pos1.toInt()
    private var pos1: Byte = 0
    private var pos2: Byte = 0
    private val itemSlot1 = SimpleItemSlot()
    private val itemSlot2 = SimpleItemSlot()
    private lateinit var direction: Direction
    override fun load(compoundTag: NbtCompound): IHotpotPlaceable {
        pos1 = compoundTag.getByte("Pos1")
        pos2 = compoundTag.getByte("Pos2")
        direction = HotpotPlaceables.POS_TO_DIRECTION[pos2 - anchorPos]!!
        itemSlot1.load(compoundTag.getCompound("ItemSlot1"))
        itemSlot2.load(compoundTag.getCompound("ItemSlot2"))
        return this
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        compoundTag.putByte("Pos1", pos1)
        compoundTag.putByte("Pos2", pos2)
        compoundTag.put("ItemSlot1", itemSlot1.save(NbtCompound()))
        compoundTag.put("ItemSlot2", itemSlot2.save(NbtCompound()))
        return compoundTag
    }

    override fun isValid(compoundTag: NbtCompound): Boolean {
        return compoundTag.contains("Pos1", NbtElement.BYTE_TYPE.toInt())
                && compoundTag.contains("Pos2", NbtElement.BYTE_TYPE.toInt())
                && compoundTag.contains("ItemSlot1", NbtElement.COMPOUND_TYPE.toInt())
                && compoundTag.contains("ItemSlot2", NbtElement.COMPOUND_TYPE.toInt())
    }

    override val id: String
        get() = "LongPlate"

    override fun interact(
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    ) {
        if (itemStack.isEmpty()) {
            if (player.isInSneakingPose) {
                hotpotPlateBlockEntity.tryRemove(this, selfPos)
            } else {
                hotpotPlateBlockEntity.tryTakeOutContentViaHand(pos, selfPos)
            }
        } else {
            val preferred: SimpleItemSlot = if (pos == anchorPos) itemSlot1 else itemSlot2
            val fallback: SimpleItemSlot = if (pos == anchorPos) itemSlot2 else itemSlot1
            if (!preferred.addItem(itemStack)) {
                fallback.addItem(itemStack)
            }
        }
    }

    override fun takeOutContent(
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    ): ItemStack {
        val consume: Boolean = !hotpotPlateBlockEntity.isInfiniteContent
        return if (pos == anchorPos) (
                if (itemSlot1.isEmpty) itemSlot2.takeItem(consume)
                else itemSlot1.takeItem(consume))
        else itemSlot2.takeItem(consume)
    }

    override fun onRemove(hotpotPlateBlockEntity: HotpotPlaceableBlockEntity, pos: BlockPosWithLevel) {
        itemSlot1.dropItem(pos)
        itemSlot2.dropItem(pos)
    }

    override fun render(
        context: BlockEntityRendererFactory.Context,
        hotpotBlockEntity: HotpotPlaceableBlockEntity,
        partialTick: Float,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int
    ) {
        val x1 = IHotpotPlaceable.getSlotX(pos1.toInt()) + 0.25f
        val z1 = IHotpotPlaceable.getSlotZ(pos1.toInt()) + 0.25f
        val x2 = IHotpotPlaceable.getSlotX(pos2.toInt()) + 0.25f
        val z2 = IHotpotPlaceable.getSlotZ(pos2.toInt()) + 0.25f
        poseStack.push()
        poseStack.translate((x1 + x2) / 2, 0f, (z1 + z2) / 2)
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction.asRotation()))
        poseStack.scale(0.8f, 0.8f, 0.8f)
        val model: BakedModel? = context.renderManager.models.modelManager
            .getModel(Identifier(EveryXHotpot.MOD_ID, "block/hotpot_plate_long"))
        context.renderManager.modelRenderer.renderModel(
            poseStack.peek(),
            bufferSource.getBuffer(RenderLayer.getSolid()),
            null,
            model,
            1f,
            1f,
            1f,
            combinedLight,
            combinedOverlay,
            ModelData.EMPTY,
            RenderLayer.getSolid()
        )
        poseStack.pop()
        var i = 0
        run {
            var k = 0
            while (k < itemSlot1.renderCount) {
                renderLargePlateItem(
                    context,
                    poseStack,
                    bufferSource,
                    combinedLight,
                    combinedOverlay,
                    itemSlot1,
                    x1,
                    z1,
                    i
                )
                k++
                i++
            }
        }
        var k = 0
        while (k < itemSlot2.renderCount) {
            renderLargePlateItem(context, poseStack, bufferSource, combinedLight, combinedOverlay, itemSlot2, x1, z1, i)
            k++
            i++
        }
    }

    override fun getCloneItemStack(
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        level: BlockPosWithLevel
    ): ItemStack {
        return ItemStack(EveryXHotpot.HOTPOT_LONG_PLATE_BLOCK_ITEM)
    }

    fun renderLargePlateItem(
        context: BlockEntityRendererFactory.Context,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int,
        slot: SimpleItemSlot,
        x: Float,
        z: Float,
        index: Int
    ) {
        poseStack.push()
        poseStack.translate(x, 0.12f, z)
        poseStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(direction.asRotation()))
        poseStack.translate(0f, 0f, -0.14f + index * 0.09f)
        poseStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(75f))
        poseStack.scale(0.35f, 0.35f, 0.35f)
        slot.renderSlot(context, poseStack, bufferSource, combinedLight, combinedOverlay)
        poseStack.pop()
    }

    override fun tryPlace(pos: Int, direction: Direction): Boolean {
        val pos2 = pos + HotpotPlaceables.DIRECTION_TO_POS[direction]!!
        if (isValidPos(pos, pos2)) {
            this.pos1 = pos.toByte()
            this.pos2 = pos2.toByte()
            this.direction = direction
            return true
        }
        return false
    }

    fun isValidPos(pos1: Int, pos2: Int): Boolean {
        return pos1 in 0..3 && pos2 in 0..3 && pos1 + pos2 != 3
    }

    override fun getPos(): List<Int> {
        return listOf(pos1.toInt(), pos2.toInt())
    }

    override fun isConflict(pos: Int): Boolean {
        return pos1.toInt() == pos || pos2.toInt() == pos
    }
}
