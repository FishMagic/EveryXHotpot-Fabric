package me.ftmc.hotpot.mixin;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BlockModelRenderer.class)
public interface BlockModelRendererMixin {
    @Accessor("DIRECTIONS")
    static Direction[] getDirection() {
        throw new AssertionError();
    }

    @Invoker("renderQuads")
    void invokeRenderQuadList(MatrixStack.Entry p_111059_, VertexConsumer p_111060_, float p_111061_, float p_111062_, float p_111063_, List<BakedQuad> p_111064_, int p_111065_, int p_111066_);
}