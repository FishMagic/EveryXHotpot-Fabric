package me.ftmc.hotpot.soup.renderers

import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.forge.net.minecraft.client.renderer.block.renderModel
import me.ftmc.hotpot.forge.net.minecraftforge.client.model.data.ModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3f
import kotlin.math.cos
import kotlin.math.sin


class HotpotSpicySoupFloatingPepperRenderer : IHotpotSoupCustomElementRenderer {
    override fun render(
        context: BlockEntityRendererFactory.Context,
        blockEntity: HotpotBlockEntity,
        partialTick: Float,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int,
        renderedWaterLevel: Float
    ) {
        val f: Float = blockEntity.time / 20f / 5f
        val part1Rotation: Float = (sin(f * Math.PI) * 1.6).toFloat()
        val part2Rotation: Float = (sin((f + 1f) * Math.PI) * 1.6).toFloat()
        val part1Position = cos(f * Math.PI.toFloat()) * 0.02f + renderedWaterLevel * 0.4375f + 0.5625f - 0.01f
        val part2Position =
            Math.cos((f + 1f) * Math.PI) * 0.02f + renderedWaterLevel * 0.4375f + 0.5625f - 0.01f
        poseStack.push()
        poseStack.translate(0.0, part1Position.toDouble(), 0.0)
        poseStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(part1Rotation))
        val part1Model: BakedModel? = context.renderManager.models.modelManager
            .getModel(ModelIdentifier(EveryXHotpot.MOD_ID, "soup/hotpot_spicy_soup_floating_pepper_1"))
        context.renderManager.modelRenderer.renderModel(
            poseStack.peek(),
            bufferSource.getBuffer(RenderLayer.getTranslucent()),
            null,
            part1Model,
            1f,
            1f,
            1f,
            combinedLight,
            combinedOverlay,
            ModelData.EMPTY,
            RenderLayer.getTranslucent()
        )
        poseStack.pop()
        poseStack.push()
        poseStack.translate(0.0, part2Position, 0.0)
        poseStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(part2Rotation))
        val part2Model: BakedModel? = context.renderManager.models.modelManager
            .getModel(ModelIdentifier(EveryXHotpot.MOD_ID, "soup/hotpot_spicy_soup_floating_pepper_2"))
        context.renderManager.modelRenderer.renderModel(
            poseStack.peek(),
            bufferSource.getBuffer(RenderLayer.getTranslucent()),
            null,
            part2Model,
            1f,
            1f,
            1f,
            combinedLight,
            combinedOverlay,
            ModelData.EMPTY,
            RenderLayer.getTranslucent()
        )
        poseStack.pop()
    }
}
