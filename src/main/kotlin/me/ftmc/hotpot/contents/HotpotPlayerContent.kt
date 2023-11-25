package me.ftmc.hotpot.contents

import com.mojang.authlib.GameProfile
import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.random.Random
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin


class HotpotPlayerContent : IHotpotContent {
    private var profile: GameProfile? = null
    private var modelPartIndex = 0
    private var modelPart: ModelPart? = null
    private var modelSkin: Identifier? = null
    private var slim = false

    constructor(player: PlayerEntity, head: Boolean) {
        profile = player.gameProfile
        modelPartIndex = if (head) 0 else RANDOM_SOURCE.nextBetween(1, VALID_PARTS.size)
    }

    constructor()

    private fun reloadModelPartWithSkin(index: Int) {
        slim = DefaultSkinHelper.getModel(profile!!.id).equals("slim")
        modelSkin = DefaultSkinHelper.getTexture(profile!!.id)
        MinecraftClient.getInstance().skinProvider.loadSkin(profile, { type, location, texture ->
            val modelName: String? = texture.getMetadata("model")
            slim = modelName == "slim"
            modelSkin = location
            updatePlayerModel(index)
        }, true)
        updatePlayerModel(index)
    }

    private fun updatePlayerModel(index: Int) {
        val playerModelPart: ModelPart = MinecraftClient.getInstance().entityModelLoader
            .getModelPart(if (slim) EntityModelLayers.PLAYER_SLIM else EntityModelLayers.PLAYER)
        modelPart = playerModelPart.getChild(VALID_PARTS[index])
        modelPart?.setPivot(0F, 0F, 0F)
        modelPart?.roll = 22.5f
    }

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
        if (modelPart == null) {
            reloadModelPartWithSkin(modelPartIndex)
        }
        poseStack.push()
        val f: Float = hotpotBlockEntity.time / 20f / ITEM_ROUND_TRIP_TIME + offset
        poseStack.translate(
            0.5f + sin(f * 2f * Math.PI) * ITEM_RADIUS,
            (ITEM_START_Y + AbstractHotpotItemStackContent.getFloatingCurve(
                f,
                0f
            ) * ITEM_FLOAT_Y + 0.42f * waterline).toDouble(),
            0.5f + cos(f * 2f * Math.PI) * ITEM_RADIUS
        )
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * 360f))
        poseStack.multiply(
            RotationAxis.POSITIVE_X.rotationDegrees(
                -90f + AbstractHotpotItemStackContent.getFloatingCurve(f, 1f) * ITEM_ROTATION
            )
        )
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE)
        modelPart?.render(
            poseStack,
            bufferSource.getBuffer(RenderLayer.getEntityCutoutNoCull(modelSkin)),
            combinedLight,
            combinedOverlay
        )
        poseStack.pop()
    }

    override fun takeOut(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): ItemStack {
        return if (modelPartIndex == 0) {
            val itemStack = ItemStack(Items.PLAYER_HEAD)
            itemStack.setSubNbt("SkullOwner", NbtHelper.writeGameProfile(NbtCompound(), profile))
            itemStack
        } else {
            ItemStack(Items.BONE, RANDOM_SOURCE.nextBetween(0, 2))
        }
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
        profile = NbtHelper.toGameProfile(compoundTag.getCompound("Profile"))
        modelPartIndex = compoundTag.getInt("ModelPartIndex")
        modelPartIndex = min((VALID_PARTS.size - 1).toDouble(), max(0.0, modelPartIndex.toDouble()))
            .toInt()
        return this
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        compoundTag.put("Profile", NbtHelper.writeGameProfile(NbtCompound(), profile))
        compoundTag.putInt("ModelPartIndex", modelPartIndex)
        return compoundTag
    }

    override fun isValid(tag: NbtCompound): Boolean {
        return tag.contains(
            "Profile",
            NbtElement.COMPOUND_TYPE.toInt()
        ) && NbtHelper.toGameProfile(tag.getCompound("Profile")) != null && tag.contains(
            "ModelPartIndex",
            NbtElement.NUMBER_TYPE.toInt()
        )
    }

    override val id: String
        get() = "Player"

    companion object {
        val VALID_PARTS = arrayOf("head", "body", "right_arm", "left_arm", "right_leg", "left_leg")
        val RANDOM_SOURCE: Random = Random.createLocal()
        const val ITEM_ROUND_TRIP_TIME = 60f
        const val ITEM_RADIUS = 0.325f
        const val ITEM_START_Y = 0.53f
        const val ITEM_FLOAT_Y = 0.06f
        const val ITEM_ROTATION = 25f
        const val ITEM_SCALE = 0.25f
    }
}
