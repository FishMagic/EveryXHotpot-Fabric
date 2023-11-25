package me.ftmc.hotpot.soup.renderers

import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier


class HotpotSpicySoupBubbleRenderer : IHotpotSoupCustomElementRenderer {
    private val largeBubbleRenderer: HotpotBubbleRenderer = HotpotBubbleRenderer(
        0.21f,
        0.8f,
        35,
        Identifier(EveryXHotpot.MOD_ID, "soup/hotpot_spicy_soup_bubble_large")
    )
    private val smallBubbleRenderer: HotpotBubbleRenderer = HotpotBubbleRenderer(
        0.35f,
        0.55f,
        45,
        Identifier(EveryXHotpot.MOD_ID, "soup/hotpot_spicy_soup_bubble_small")
    )

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
        largeBubbleRenderer.render(
            context,
            blockEntity,
            partialTick,
            poseStack,
            bufferSource,
            combinedLight,
            combinedLight,
            renderedWaterLevel
        )
        smallBubbleRenderer.render(
            context,
            blockEntity,
            partialTick,
            poseStack,
            bufferSource,
            combinedLight,
            combinedLight,
            renderedWaterLevel
        )
    }
}
