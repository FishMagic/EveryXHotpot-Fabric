package me.ftmc.hotpot.mixin;

import me.ftmc.hotpot.DummyCheesedResourceLocation;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Mixin(SpriteAtlasTexture.class)
public abstract class TextureAtlasMixin {
    @Shadow
    @Final
    @Deprecated
    public static Identifier BLOCK_ATLAS_TEXTURE;

    @Shadow
    protected abstract Identifier getTexturePath(Identifier pLocation);

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "loadSprites(Lnet/minecraft/resource/ResourceManager;Ljava/util/Set;)Ljava/util/Collection;", at = @At("RETURN"), cancellable = true)
    public void getBasicSpriteInfos(ResourceManager pResourceManager, Set<Identifier> pSpriteLocations, CallbackInfoReturnable<Collection<Sprite.Info>> cir) {
        ArrayList<Sprite.Info> extendedInfos = new ArrayList<>(cir.getReturnValue());

        cir.getReturnValue().stream()
                .filter(info -> info.getId().getPath().contains("item/"))
                .forEach(info -> extendedInfos.add(new Sprite.Info(new DummyCheesedResourceLocation(info.getId(), info.getId().getNamespace(), info.getId().getPath().concat("_cheesed")), info.getWidth(), info.getHeight(), info.animationData)));

        cir.setReturnValue(extendedInfos);
    }

    @Inject(method = "loadSprite", at = @At("HEAD"), cancellable = true)
    public void load(ResourceManager pResourceManager, Sprite.Info pSpriteInfo, int pWidth, int pHeight, int pMipmapLevel, int pOriginX, int pOriginY, CallbackInfoReturnable<Sprite> cir) {
        if (pSpriteInfo.getId() instanceof DummyCheesedResourceLocation) {
            Identifier resourcelocation = this.getTexturePath(((DummyCheesedResourceLocation) pSpriteInfo.getId()).getOriginalTextureLocation());
            Optional<Resource> optional = pResourceManager.getResource(resourcelocation);

            try {
                if (optional.isEmpty()) {
                    return;
                }

                Resource resource = optional.get();

                NativeImage nativeImage = NativeImage.read(resource.getInputStream());
                remapCheesedImage(nativeImage);

                cir.setReturnValue(new Sprite((SpriteAtlasTexture) (Object) this, pSpriteInfo, pMipmapLevel, pWidth, pHeight, pOriginX, pOriginY, nativeImage));
            } catch (RuntimeException runtimeexception) {
                LOGGER.error("Unable to parse metadata from {}", resourcelocation, runtimeexception);
                cir.setReturnValue(null);
            } catch (IOException ioexception) {
                LOGGER.error("Using missing texture, unable to load {}", resourcelocation, ioexception);
                cir.setReturnValue(null);
            }
        }
    }

    private float getAverageGrayScale(NativeImage image) {
        float totalGray = 0f;
        int validCount = 0;

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int originalColor = image.getColor(x, y);

                int blue = ColorHelper.Argb.getBlue(originalColor);
                int green = ColorHelper.Argb.getGreen(originalColor);
                int red = ColorHelper.Argb.getRed(originalColor);

                if (ColorHelper.Argb.getRed(originalColor) != 0) {
                    totalGray += (red * 0.299f + green * 0.587f + blue * 0.144f) / 255f;
                    validCount++;
                }
            }
        }

        return totalGray / validCount;
    }

    private void remapCheesedImage(NativeImage image) {
        float amplifier = 0.65f / getAverageGrayScale(image);

        Random source = Random.create();
        source.setSeed(42L);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int originalColor = image.getColor(x, y);

                int alpha = ColorHelper.Argb.getAlpha(originalColor);
                int blue = ColorHelper.Argb.getBlue(originalColor);
                int green = ColorHelper.Argb.getGreen(originalColor);
                int red = ColorHelper.Argb.getRed(originalColor);

                float gray = Math.min(1f, (red * 0.299f + green * 0.587f + blue * 0.144f) / 255f * amplifier + (float) source.nextGaussian() * 0.12f);
                int finalAlpha = (int) (alpha * sigmoid(((image.getHeight() - 2f * y) / image.getHeight()) * 10f));

                blendPixel(image, x, y, ColorHelper.Argb.getArgb(
                        finalAlpha,
                        (int) (220 + gray * 35),
                        (int) (170 + gray * 55),
                        0
                ));

                //芝士 0, 170+55, 220+35
                //咖喱 98, 203, 250
            }
        }
    }

    public void blendPixel(NativeImage image, int x, int y, int colorToBlend) {
        int pixelColor = image.getColor(x, y);

        float toBlendA = (float) ColorHelper.Argb.getAlpha(colorToBlend) / 255.0F;
        float toBlendB = (float) ColorHelper.Argb.getBlue(colorToBlend) / 255.0F;
        float toBlendG = (float) ColorHelper.Argb.getGreen(colorToBlend) / 255.0F;
        float toBlendR = (float) ColorHelper.Argb.getRed(colorToBlend) / 255.0F;

        float pixelR = (float) ColorHelper.Argb.getRed(pixelColor) / 255.0F;
        float pixelG = (float) ColorHelper.Argb.getGreen(pixelColor) / 255.0F;
        float pixelB = (float) ColorHelper.Argb.getBlue(pixelColor) / 255.0F;

        float transparency = 1.0F - toBlendA;

        int b = (int) (Math.min(1f, toBlendB * toBlendA + pixelB * transparency) * 255.0F);
        int g = (int) (Math.min(1f, toBlendG * toBlendA + pixelG * transparency) * 255.0F);
        int r = (int) (Math.min(1f, toBlendR * toBlendA + pixelR * transparency) * 255.0F);

        image.setColor(x, y, ColorHelper.Argb.getArgb(ColorHelper.Argb.getAlpha(pixelColor), b, g, r));
    }

    private float sigmoid(float x) {
        return 1f / (1f + (float) Math.exp(-x));
    }
}
