package me.ftmc.hotpot.mixin;

import me.ftmc.hotpot.EveryXHotpot;
import me.ftmc.hotpot.EveryXHotpotClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemRenderer.class)
public class ItemRenderMixin {

    @Redirect(
            method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/BuiltinModelItemRenderer;render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V"
            )
    )
    public void renderItem(BuiltinModelItemRenderer instance, ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (stack.isOf(EveryXHotpot.INSTANCE.getHOTPOT_CHOPSTICK())
                || stack.isOf(EveryXHotpot.INSTANCE.getHOTPOT_SPICE_PACK())
        ) {
            EveryXHotpotClient.INSTANCE.getHOTPOT_BEWLR().render(stack, mode, matrices, vertexConsumers, light, overlay);
        } else {
            instance.render(stack, mode, matrices, vertexConsumers, light, overlay);
        }
    }
}
