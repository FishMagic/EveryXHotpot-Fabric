package me.ftmc.hotpot.mixin;

import me.ftmc.hotpot.items.HotpotChopstickItem;
import me.ftmc.hotpot.items.ItemRegistrar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.CrackParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrackParticle.class)
public abstract class BreakingItemParticleMixin extends SpriteBillboardParticle {
    protected BreakingItemParticleMixin(ClientWorld clientWorld, double d, double e, double f) {
        super(clientWorld, d, e, f);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    public void constructor(ClientWorld level, double p_105666_, double p_105667_, double p_105668_, ItemStack itemStack, CallbackInfo ci) {
        if (itemStack.isOf(ItemRegistrar.INSTANCE.getHOTPOT_CHOPSTICK())) {
            ItemStack chopstickFoodItemStack;

            if (!(chopstickFoodItemStack = HotpotChopstickItem.Companion.getChopstickFoodItemStack(itemStack)).isEmpty()) {
                BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModel(chopstickFoodItemStack, level, null, 0);
                setSprite(model.getParticleSprite());
            }
        }
    }
}
