package me.ftmc.hotpot.mixin;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Math;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin {
    @Shadow
    private @Final Identifier id;

    @SuppressWarnings("deprecation")
    @ModifyVariable(method = "stitch", argsOnly = true, index = 1, at = @At("HEAD"))
    private List<SpriteContents> stitch(List<SpriteContents> contents) {
        if (id.equals(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)) {
            ArrayList<SpriteContents> replacedContents = new ArrayList<>(contents);

            for (SpriteContents content : contents) {
                if (!content.getId().getPath().contains("item/")) continue;

                NativeImage originalImage = ((SpriteContentAccessorMixin) content).getImage();

                NativeImage image = new NativeImage(
                        originalImage.getFormat(),
                        originalImage.getWidth(),
                        originalImage.getHeight(),
                        true);
                image.copyFrom(originalImage);
                remapCheesedImage(image);

                SpriteContents cheesedContent = new SpriteContents(
                        content.getId().withSuffixedPath("_cheesed"),
                        new SpriteDimensions(content.getWidth(), content.getHeight()),
                        image,
                        AnimationResourceMetadata.EMPTY
                );
                cheesedContent.animation = content.animation;

                replacedContents.add(cheesedContent);
            }

            return replacedContents;
        }

        return contents;
    }

    private float getAverageGrayScale(NativeImage image) {
        float totalGray = 0f;
        int validCount = 0;

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int originalColor = image.getColor(x, y);

                int blue = ColorHelper.Abgr.getBlue(originalColor);
                int green = ColorHelper.Abgr.getGreen(originalColor);
                int red = ColorHelper.Abgr.getRed(originalColor);

                if (ColorHelper.Argb.getAlpha(originalColor) != 0) {
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

                int alpha = ColorHelper.Abgr.getAlpha(originalColor);
                int blue = ColorHelper.Abgr.getBlue(originalColor);
                int green = ColorHelper.Abgr.getGreen(originalColor);
                int red = ColorHelper.Abgr.getRed(originalColor);

                float gray = Math.min(1f, (red * 0.299f + green * 0.587f + blue * 0.144f) / 255f * amplifier + (float) source.nextGaussian() * 0.12f);
                int finalAlpha = (int) (alpha * sigmoid(((image.getHeight() - 2f * y) / image.getHeight()) * 10f));

                blendPixel(image, x, y, ColorHelper.Abgr.getAbgr(
                        finalAlpha,
                        0,
                        (int) (170 + gray * 55),
                        (int) (220 + gray * 35)
                ));

                //芝士 0, 170+55, 220+35
                //咖喱 98, 203, 250
            }
        }
    }

    public void blendPixel(NativeImage image, int x, int y, int colorABGR) {
        int pixelColor = image.getColor(x, y);

        float toBlendA = (float) ColorHelper.Abgr.getAlpha(colorABGR) / 255.0F;
        float toBlendB = (float) ColorHelper.Abgr.getBlue(colorABGR) / 255.0F;
        float toBlendG = (float) ColorHelper.Abgr.getGreen(colorABGR) / 255.0F;
        float toBlendR = (float) ColorHelper.Abgr.getRed(colorABGR) / 255.0F;

        float pixelB = (float) ColorHelper.Abgr.getBlue(pixelColor) / 255.0F;
        float pixelG = (float) ColorHelper.Abgr.getGreen(pixelColor) / 255.0F;
        float pixelR = (float) ColorHelper.Abgr.getRed(pixelColor) / 255.0F;

        float transparency = 1.0F - toBlendA;

        int b = (int) (Math.min(1f, toBlendB * toBlendA + pixelB * transparency) * 255.0F);
        int g = (int) (Math.min(1f, toBlendG * toBlendA + pixelG * transparency) * 255.0F);
        int r = (int) (Math.min(1f, toBlendR * toBlendA + pixelR * transparency) * 255.0F);

        image.setColor(x, y, ColorHelper.Abgr.getAbgr(ColorHelper.Abgr.getAlpha(pixelColor), b, g, r));
    }

    private float sigmoid(float x) {
        return 1f / (1f + (float) Math.exp(-x));
    }
}
