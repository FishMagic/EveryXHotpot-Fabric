package me.ftmc.hotpot.soup.renderers

import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.forge.net.minecraft.client.renderer.block.renderModel
import me.ftmc.hotpot.forge.net.minecraftforge.client.model.data.ModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random


class HotpotBubbleRenderer(
    private val spread: Float,
    private val maxScale: Float,
    amount: Int,
    private val bubbleLocation: Identifier
) :
    IHotpotSoupCustomElementRenderer {
    private val bubbles: Array<Bubble?> = arrayOfNulls(amount)

    private fun renderBubble(
        context: BlockEntityRendererFactory.Context,
        blockEntity: HotpotBlockEntity,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int,
        bubble: Bubble,
        model: BakedModel?
    ) {
        poseStack.push()
        val progress: Float = (blockEntity.time + bubble.offset) % BUBBLE_GROWTH_TIME / BUBBLE_GROWTH_TIME
        val scale = progress * maxScale
        val y: Float = BUBBLE_START_Y + blockEntity.renderedWaterLevel * progress * BUBBLE_GROWTH_Y
        poseStack.translate(bubble.x, y, bubble.z)
        poseStack.scale(scale, scale, scale)
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
        val model: BakedModel? =
            context.renderManager.models.modelManager.getModel(bubbleLocation)
        for (i in bubbles.indices) {
            val bubble = bubbles[i]
            if (bubble == null || blockEntity.time >= bubble.startTime + bubble.offset + BUBBLE_GROWTH_TIME) {
                bubbles[i] = Bubble(
                    0.5f + (RANDOM_SOURCE.nextFloat() * 2f - 1f) * spread,
                    0.5f + (RANDOM_SOURCE.nextFloat() * 2f - 1f) * spread,
                    RANDOM_SOURCE.nextBetween(-BUBBLE_EMERGE_OFFSET_RANGE, BUBBLE_EMERGE_OFFSET_RANGE + 1),
                    blockEntity.time
                )
                continue
            }
            renderBubble(context, blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay, bubble, model)
        }
    }

    @JvmRecord
    data class Bubble(val x: Float, val z: Float, val offset: Int, val startTime: Int)
    companion object {
        val RANDOM_SOURCE: Random = Random.createLocal()
        const val BUBBLE_EMERGE_OFFSET_RANGE = 5
        const val BUBBLE_GROWTH_TIME = 10f
        const val BUBBLE_START_Y = 0.5f
        const val BUBBLE_GROWTH_Y = 0.525f
    }
}
