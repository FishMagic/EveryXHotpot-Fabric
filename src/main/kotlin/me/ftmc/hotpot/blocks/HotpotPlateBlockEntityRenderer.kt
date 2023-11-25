package me.ftmc.hotpot.blocks

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack


class HotpotPlateBlockEntityRenderer(val context: BlockEntityRendererFactory.Context) : BlockEntityRenderer<HotpotPlaceableBlockEntity> {

    override fun render(
        hotpotPlateBlockEntity: HotpotPlaceableBlockEntity,
        partialTick: Float,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int
    ) {
        hotpotPlateBlockEntity.getPlaceables().forEach { plate ->
            plate.render(
                context,
                hotpotPlateBlockEntity,
                partialTick,
                poseStack,
                bufferSource,
                combinedLight,
                combinedOverlay
            )
        }
    }

    override fun rendersOutsideBoundingBox(blockEntity: HotpotPlaceableBlockEntity): Boolean {
        return false
    }

    override fun getRenderDistance(): Int {
        return 64
    }
}

