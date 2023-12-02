package me.ftmc.hotpot.placeables

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.blocks.HotpotPlaceableBlockEntity
import me.ftmc.hotpot.forge.net.minecraft.client.renderer.block.renderModel
import me.ftmc.hotpot.forge.net.minecraftforge.client.model.data.ModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Hand
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f


class HotpotPlacedChopstick : IHotpotPlaceable {
    override val anchorPos: Int
        get() = pos1.toInt()
    private var pos1: Byte = 0
    private var pos2: Byte = 0
    private var chopstickItemStack: ItemStack = ItemStack.EMPTY
    private lateinit var direction: Direction
    override fun load(compoundTag: NbtCompound): IHotpotPlaceable {
        pos1 = compoundTag.getByte("Pos1")
        pos2 = compoundTag.getByte("Pos2")
        direction = HotpotPlaceables.POS_TO_DIRECTION[pos2 - pos1]!!
        chopstickItemStack = ItemStack.fromNbt(compoundTag.getCompound("Chopstick"))
        return this
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        compoundTag.putByte("Pos1", pos1)
        compoundTag.putByte("Pos2", pos2)
        compoundTag.put("Chopstick", chopstickItemStack.writeNbt(NbtCompound()))
        return compoundTag
    }

    override fun isValid(compoundTag: NbtCompound): Boolean {
        return compoundTag.contains("Pos1", NbtElement.BYTE_TYPE.toInt())
                && compoundTag.contains("Pos2", NbtElement.BYTE_TYPE.toInt())
                && compoundTag.contains("Chopstick", NbtElement.COMPOUND_TYPE.toInt())
    }

    override val id: String
        get() = "PlacedChopstick"

    override fun interact(
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    ) {
        hotpotPlateBlockEntity.tryRemove(this, selfPos)
    }

    override fun takeOutContent(
        pos: Int,
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        selfPos: BlockPosWithLevel
    ): ItemStack {
        return ItemStack.EMPTY
    }

    override fun onRemove(hotpotPlateBlockEntity: HotpotPlaceableBlockEntity, pos: BlockPosWithLevel) {
        pos.dropItemStack(chopstickItemStack)
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
        val x1 = IHotpotPlaceable.getSlotX(pos1.toInt()) + 0.25
        val z1 = IHotpotPlaceable.getSlotZ(pos1.toInt()) + 0.25
        val x2 = IHotpotPlaceable.getSlotX(pos2.toInt()) + 0.25
        val z2 = IHotpotPlaceable.getSlotZ(pos2.toInt()) + 0.25
        poseStack.push()
        poseStack.translate((x1 + x2) / 2, 0.07, (z1 + z2) / 2)
        poseStack.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(direction.asRotation()))
        poseStack.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(95f))
        poseStack.scale(0.5f, 0.5f, 0.5f)
        context.itemRenderer.renderItem(
            null,
            chopstickItemStack,
            ModelTransformation.Mode.NONE,
            true,
            poseStack,
            bufferSource,
            null,
            combinedLight,
            combinedOverlay,
            ModelTransformation.Mode.FIXED.ordinal
        )
        poseStack.pop()
        poseStack.push()
        poseStack.translate((x1 + x2) / 2, 0.0, (z1 + z2) / 2)
        poseStack.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(direction.asRotation()))
        poseStack.scale(0.5f, 0.5f, 0.5f)
        val model = context.renderManager.models.modelManager
            .getModel(ModelIdentifier(EveryXHotpot.MOD_ID, "block/hotpot_chopstick_stand"))
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
    }

    override fun getCloneItemStack(
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        level: BlockPosWithLevel
    ): ItemStack {
        return chopstickItemStack
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

    fun setChopstickItemStack(chopstickItemStack: ItemStack) {
        this.chopstickItemStack = chopstickItemStack
    }
}
