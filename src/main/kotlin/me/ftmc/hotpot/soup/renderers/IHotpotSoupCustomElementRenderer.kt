package me.ftmc.hotpot.soup.renderers

import me.ftmc.hotpot.blocks.HotpotBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack


interface IHotpotSoupCustomElementRenderer {
    fun render(
        context: BlockEntityRendererFactory.Context,
        blockEntity: HotpotBlockEntity,
        partialTick: Float,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int,
        renderedWaterLevel: Float
    )
}
