package me.ftmc.hotpot.mixin;

import me.ftmc.hotpot.event.GetTooltipEventCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class GetTooltipEventMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"))
    public void getTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        List<Text> list = GetTooltipEventCallback.Companion.getEVENT().invoker().getTooltip((ItemStack) (Object) this, cir.getReturnValue(), player, context);
    }
}
