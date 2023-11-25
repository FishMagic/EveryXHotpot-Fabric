package me.ftmc.hotpot.mixin;

import me.ftmc.hotpot.event.OnDeathEventCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class OnDeathEventMixin {

    @Inject(method = "onDeath", at = @At("RETURN"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        OnDeathEventCallback.Companion.getEVENT().invoker().onDeath((ServerPlayerEntity) (Object) this, damageSource);
    }
}
