package me.ftmc.hotpot.forge.net.minecraft.client.renderer.block

import me.ftmc.hotpot.forge.net.minecraft.client.renderer.getQuads
import me.ftmc.hotpot.forge.net.minecraftforge.client.model.data.ModelData
import net.minecraft.block.BlockState
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.block.BlockModelRenderer
import net.minecraft.client.render.model.BakedModel
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

    for (direction in BlockModelRenderer.DIRECTIONS) {
        randomSource.setSeed(42L)
        BlockModelRenderer.renderQuads(
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
    BlockModelRenderer.renderQuads(
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
