package me.ftmc.hotpot.mixin;

import net.minecraft.client.texture.Animator;
import net.minecraft.client.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin {

    private boolean isTickerCreated = false;

    @Inject(method = "createAnimation", at = @At("RETURN"), cancellable = true)
    public void createTickerMixin(CallbackInfoReturnable<Animator> returnable) {
        if (isTickerCreated) {
            returnable.setReturnValue(null);
        } else {
            isTickerCreated = true;
        }
    }
}

