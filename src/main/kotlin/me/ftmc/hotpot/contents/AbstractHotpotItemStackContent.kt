package me.ftmc.hotpot.contents

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.items.IHotpotSpecialContentItem
import me.ftmc.hotpot.soup.IHotpotSoupWithActiveness
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.item.FoodComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.RotationAxis
import org.joml.Math


abstract class AbstractHotpotItemStackContent : IHotpotContent {
    private lateinit var itemStack: ItemStack
    var cookingTime = 0
        private set
    private var cookingProgress = 0
    private var experience = 0f

    constructor(itemStack: ItemStack) {
        this.itemStack = itemStack
    }

    constructor()

    override fun placed(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        itemStack = itemStack.split(1)
        cookingTime = remapCookingTime(itemStack, hotpotBlockEntity, pos)
        cookingProgress = 0
        experience = 0f
    }

    abstract fun remapCookingTime(
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): Int

    abstract fun remapResult(
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack?

    abstract fun remapExperience(
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): Float?

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
        poseStack.push()
        val f: Float = hotpotBlockEntity.time / 20f / ITEM_ROUND_TRIP_TIME + offset
        poseStack.translate(
            0.5f + Math.sin(f * 2f * Math.PI) * ITEM_RADIUS,
            (ITEM_START_Y + getFloatingCurve(f, 0f) * ITEM_FLOAT_Y + 0.42f * waterline).toDouble(),
            0.5f + Math.cos(f * 2f * Math.PI) * ITEM_RADIUS
        )
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * 360f))
        poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90f + getFloatingCurve(f, 1f) * ITEM_ROTATION))
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE)
        context.itemRenderer.renderItem(
            null,
            itemStack,
            ModelTransformationMode.FIXED,
            true,
            poseStack,
            bufferSource,
            hotpotBlockEntity.world,
            combinedLight,
            combinedOverlay,
            ModelTransformationMode.FIXED.ordinal
        )
        poseStack.pop()
    }

    override fun takeOut(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): ItemStack {
        val serverLevel = pos.level
        if (serverLevel is ServerWorld) {
            val withActiveness = hotpotBlockEntity.getSoup()
            if (withActiveness is IHotpotSoupWithActiveness) {
                experience *= 1f + withActiveness.getActiveness(hotpotBlockEntity, pos)
            }
            ExperienceOrbEntity.spawn(serverLevel, pos.toVec3(), Math.round(experience * 1.5f))
        }
        return itemStack
    }

    override fun onOtherContentUpdate(
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ) {
        val iHotpotSpecialContentItem = itemStack.item
        if (iHotpotSpecialContentItem is IHotpotSpecialContentItem && content is AbstractHotpotItemStackContent) {
            content.itemStack = iHotpotSpecialContentItem.onOtherContentUpdate(
                itemStack,
                content.itemStack,
                content,
                hotpotBlockEntity,
                pos
            )
            itemStack = iHotpotSpecialContentItem.getSelfItemStack(itemStack, this, hotpotBlockEntity, pos)
        }
    }

    override fun tick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Boolean {
        if (cookingTime < 0) return false
        if (cookingProgress >= cookingTime) {
            val result = remapResult(itemStack, hotpotBlockEntity, pos)
            if (result != null) {
                experience = remapExperience(itemStack, hotpotBlockEntity, pos) ?: 0f
                itemStack = result
                cookingTime = -1
                return true
            }
        } else {
            cookingProgress++
        }
        return false
    }

    val foodProperties: FoodComponent?
        get() = itemStack.item.foodComponent

    override fun load(compoundTag: NbtCompound): IHotpotContent {
        itemStack = ItemStack.fromNbt(compoundTag)
        cookingTime = compoundTag.getInt("CookingTime")
        cookingProgress = compoundTag.getInt("CookingProgress")
        experience = compoundTag.getFloat("Experience")
        return this
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        itemStack.writeNbt(compoundTag)
        compoundTag.putInt("CookingTime", cookingTime)
        compoundTag.putInt("CookingProgress", cookingProgress)
        compoundTag.putFloat("Experience", experience)
        return compoundTag
    }

    override fun isValid(compoundTag: NbtCompound): Boolean {
        return ItemStack.fromNbt(compoundTag) !== ItemStack.EMPTY && compoundTag.contains(
            "CookingTime",
            NbtElement.NUMBER_TYPE.toInt()
        ) && compoundTag.contains(
            "CookingProgress",
            NbtElement.NUMBER_TYPE.toInt()
        ) && compoundTag.contains("Experience", NbtElement.FLOAT_TYPE.toInt())
    }

    override fun toString(): String {
        return itemStack.toString()
    }

    fun getItemStack(): ItemStack? {
        return itemStack
    }

    companion object {
        const val ITEM_ROUND_TRIP_TIME = 60f
        const val ITEM_RADIUS = 0.315f
        const val ITEM_START_Y = 0.53f
        const val ITEM_FLOAT_Y = 0.06f
        const val ITEM_ROTATION = 25f
        const val ITEM_SCALE = 0.25f
        fun getFloatingCurve(f: Float, offset: Float): Float {
            return Math.sin((f + offset) / 0.25f * 2f * Math.PI).toFloat()
        }
    }
}

