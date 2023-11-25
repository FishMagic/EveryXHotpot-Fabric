package me.ftmc.hotpot.blocks

import me.ftmc.hotpot.forge.net.minecraft.client.renderer.block.renderModel
import me.ftmc.hotpot.forge.net.minecraftforge.client.model.data.ModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.max


class HotpotBlockEntityRenderer(private val context: BlockEntityRendererFactory.Context) :
    BlockEntityRenderer<HotpotBlockEntity> {

    override fun render(
        entity: HotpotBlockEntity,
        partialTick: Float,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int
    ) {
        val waterLevel: Float = entity.waterLevel
        val isVanillaBufferSource = bufferSource is VertexConsumerProvider.Immediate //Fix crashes when using Rubidium
        val renderedWaterLevel = entity.renderedWaterLevel
        val difference = waterLevel - renderedWaterLevel
        entity.renderedWaterLevel =
            if (renderedWaterLevel < 0) waterLevel else if (difference < 0.02f) waterLevel else renderedWaterLevel + difference * partialTick / 8f
        entity.renderedWaterLevel = max(0.35f, entity.renderedWaterLevel)
        for (i in 0 until entity.getContents().size) {
            entity.getContents()[i].render(
                context,
                entity,
                poseStack,
                bufferSource,
                combinedLight,
                combinedOverlay,
                0.125f * i,
                renderedWaterLevel
            )
        }

        //Fix crashes when using Rubidium
        if (isVanillaBufferSource) {
            val source: VertexConsumerProvider.Immediate = bufferSource as VertexConsumerProvider.Immediate

            //FIXME: Probably UNSAFE FOR RENDERING!
            source.draw(RenderLayer.getEntitySolid(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE))
            source.draw(RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE))
            source.draw(RenderLayer.getEntityCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE))
            source.draw(RenderLayer.getEntitySmoothCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE))
        }
        entity.getSoup().customElementRenderers.forEach { iHotpotSoupCustomElementRenderer ->
            iHotpotSoupCustomElementRenderer.render(
                context,
                entity,
                partialTick,
                poseStack,
                bufferSource,
                combinedLight,
                combinedOverlay,
                renderedWaterLevel
            )
        }
        entity.getSoup().soupResourceLocation?.let { soupLocation ->
            poseStack.push()
            poseStack.translate(0.0, max(0.563, (renderedWaterLevel * 0.4375f + 0.5625f).toDouble()), 0.0)
            val model: BakedModel? =
                context.renderManager.models.modelManager.getModel(soupLocation)
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
        }

        //Fix crashes when using Rubidium
        if (isVanillaBufferSource) {
            val source: VertexConsumerProvider.Immediate = bufferSource as VertexConsumerProvider.Immediate

            //FIXME: Probably UNSAFE FOR RENDERING!
            source.draw(TexturedRenderLayers.getEntityTranslucentCull())
            source.draw(RenderLayer.getGlintTranslucent())
        }
    }

    fun shouldRenderOffScreen(hotpotBlockEntity: HotpotBlockEntity?): Boolean {
        return false
    }

    val viewDistance: Int
        get() = 24

    data class Bubble(val x: Float, val z: Float, val offset: Int, val startTime: Int)
}
