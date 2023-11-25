package me.ftmc.hotpot.placeables

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.blocks.HotpotPlaceableBlockEntity
import me.ftmc.hotpot.forge.net.minecraft.client.renderer.block.renderModel
import me.ftmc.hotpot.forge.net.minecraftforge.client.model.data.ModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis


class HotpotSmallPlate : IHotpotPlaceable {
    override val anchorPos: Int
        get() = pos.toInt()
    private var pos: Byte = 0
    private var directionSlot: Byte = 0
    private lateinit var direction: Direction
    private val itemSlot = SimpleItemSlot()
    override fun load(compoundTag: NbtCompound): IHotpotPlaceable {
        pos = compoundTag.getByte("Pos")
        directionSlot = compoundTag.getByte("DirectionPos")
        direction = HotpotPlaceables.POS_TO_DIRECTION[directionSlot - pos]!!
        itemSlot.load(compoundTag.getCompound("ItemSlot"))
        return this
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        compoundTag.putByte("Pos", pos)
        compoundTag.putByte("DirectionPos", directionSlot.toByte())
        compoundTag.put("ItemSlot", itemSlot.save(NbtCompound()))
        return compoundTag
    }

    override fun isValid(compoundTag: NbtCompound): Boolean {
        return compoundTag.contains("Pos", NbtElement.BYTE_TYPE.toInt())
                && compoundTag.contains("DirectionPos", NbtElement.BYTE_TYPE.toInt())
                && compoundTag.contains("ItemSlot", NbtElement.COMPOUND_TYPE.toInt())
    }

    override val id: String
        get() = "SmallPlate"

    override fun interact(
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    ) {
        if (itemStack.isEmpty) {
            if (player.isInSneakingPose) {
                hotpotPlateBlockEntity.tryRemove(this, selfPos)
            } else {
                hotpotPlateBlockEntity.tryTakeOutContentViaHand(pos, selfPos)
            }
        } else {
            itemSlot.addItem(itemStack)
        }
    }

    override fun takeOutContent(
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    ): ItemStack {
        return itemSlot.takeItem(!hotpotPlateBlockEntity.isInfiniteContent)
    }

    override fun onRemove(hotpotPlateBlockEntity: HotpotPlaceableBlockEntity, pos: BlockPosWithLevel) {
        itemSlot.dropItem(pos)
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
        val x = IHotpotPlaceable.getSlotX(pos.toInt()) + 0.25f
        val z = IHotpotPlaceable.getSlotZ(pos.toInt()) + 0.25f
        poseStack.push()
        poseStack.translate(x, 0f, z)
        poseStack.scale(0.8f, 0.8f, 0.8f)
        val model = context.renderManager.models.modelManager
            .getModel(Identifier(EveryXHotpot.MOD_ID, "block/hotpot_plate_small"))
        context.renderManager.modelRenderer.renderModel(
            poseStack.peek(),
            bufferSource.getBuffer(RenderLayer.getTranslucent()),
            null,
            model,
            1f,
            1f,
            1f,
            combinedLight,
            combinedOverlay,
            ModelData.EMPTY,
            RenderLayer.getTranslucent()
        )
        poseStack.pop()
        for (i in 0 until itemSlot.renderCount) {
            poseStack.push()
            poseStack.translate(x.toDouble(), 0.065f + 0.02 * i, z.toDouble())
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction.asRotation() + i % 2 * 20))
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f))
            poseStack.scale(0.35f, 0.35f, 0.35f)
            itemSlot.renderSlot(context, poseStack, bufferSource, combinedLight, combinedOverlay)
            poseStack.pop()
        }
    }

    override fun getCloneItemStack(
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        level: BlockPosWithLevel
    ): ItemStack {
        return ItemStack(EveryXHotpot.HOTPOT_SMALL_PLATE_BLOCK_ITEM)
    }

    override fun tryPlace(pos: Int, direction: Direction): Boolean {
        this.pos = pos.toByte()
        directionSlot = (pos + HotpotPlaceables.DIRECTION_TO_POS[direction]!!).toByte()
        this.direction = direction
        return true
    }

    override fun getPos(): List<Int> {
        return listOf(pos.toInt())
    }

    override fun isConflict(pos: Int): Boolean {
        return this.pos.toInt() == pos
    }
}
