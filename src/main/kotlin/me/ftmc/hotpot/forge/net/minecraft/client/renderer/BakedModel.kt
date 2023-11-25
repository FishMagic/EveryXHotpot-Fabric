package me.ftmc.hotpot.forge.net.minecraft.client.renderer

import me.ftmc.hotpot.forge.net.minecraftforge.client.model.data.ModelData
import net.minecraft.block.BlockState
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random

public fun BakedModel.getQuads(
    state: BlockState?,
    side: Direction?,
    rand: Random,
    data: ModelData,
    renderType: RenderLayer
): List<BakedQuad> {
    return getQuads(state, side, rand)
}