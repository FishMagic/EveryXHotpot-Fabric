package me.ftmc.hotpot.mixin;

import me.ftmc.hotpot.soup.effects.EffectRegistrar;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect p_21024_);

    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    public void canFreeze(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!hasStatusEffect(EffectRegistrar.INSTANCE.getHOTPOT_WARM()) && cir.getReturnValue());
    }
}
