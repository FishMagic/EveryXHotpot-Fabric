package me.ftmc.hotpot.mixin;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.class)
interface SpriteContentAccessorMixin {

    @Accessor()
    NativeImage getImage();
}