package me.ftmc.hotpot.forge.net.minecraft.client.renderer.block

import me.ftmc.hotpot.forge.net.minecraft.client.renderer.getQuads
import me.ftmc.hotpot.forge.net.minecraftforge.client.model.data.ModelData
import me.ftmc.hotpot.mixin.BlockModelRendererMixin
import net.minecraft.block.BlockState
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.block.BlockModelRenderer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.random.Random

fun BlockModelRenderer.renderModel(
    p_111068_: MatrixStack.Entry,
    p_111069_: VertexConsumer,
    p_111070_: BlockState?,
    p_111071_: BakedModel?,
    p_111072_: Float,
    p_111073_: Float,
    p_111074_: Float,
    p_111075_: Int,
    p_111076_: Int,
    modelData: ModelData,
    renderType: RenderLayer
) {
    val randomSource = Random.create()
    val i = 42L

    for (direction in BlockModelRendererMixin.getDirection()) {
        randomSource.setSeed(42L)
        renderQuadList(
            p_111068_,
            p_111069_,
            p_111072_,
            p_111073_,
            p_111074_,
            p_111071_?.getQuads(p_111070_, direction, randomSource, modelData, renderType) ?: listOf(),
            p_111075_,
            p_111076_
        )
    }

    randomSource.setSeed(42L)
    renderQuadList(
        p_111068_,
        p_111069_,
        p_111072_,
        p_111073_,
        p_111074_,
        p_111071_?.getQuads(p_111070_, null, randomSource, modelData, renderType) ?: listOf(),
        p_111075_,
        p_111076_
    )

}

fun BlockModelRenderer.renderQuadList(
    p_111059_: MatrixStack.Entry,
    p_111060_: VertexConsumer,
    p_111061_: Float,
    p_111062_: Float,
    p_111063_: Float,
    p_111064_: List<BakedQuad>,
    p_111065_: Int,
    p_111066_: Int
) {
    (this as BlockModelRendererMixin).invokeRenderQuadList(
        p_111059_,
        p_111060_,
        p_111061_,
        p_111062_,
        p_111063_,
        p_111064_,
        p_111065_,
        p_111066_
    )
}
