package me.ftmc.hotpot.mixin;

import me.ftmc.hotpot.items.CheesedBakedModel;
import me.ftmc.hotpot.items.SimpleModelBaker;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(ModelLoader.class)
public abstract class ModelBakeryMixin {
    @Shadow
    private @Final Map<Identifier, BakedModel> bakedModels;
    @Shadow
    private @Final Map<Identifier, UnbakedModel> modelsToBake;
    @Shadow
    public @Final
    static JsonUnbakedModel GENERATION_MARKER;
    @Shadow
    public @Final
    static ModelIdentifier MISSING_ID;
    @Shadow
    private @Final Map<Identifier, UnbakedModel> unbakedModels;

    @Inject(method = "bake", at = @At("RETURN"))
    public void bakeModels(BiFunction<Identifier, SpriteIdentifier, Sprite> atlasSpriteGetter, CallbackInfo ci) {
        modelsToBake.forEach(((location, unbakedModel) -> {
            if (unbakedModel instanceof JsonUnbakedModel blockModel && blockModel.getRootModel() == GENERATION_MARKER) {
                Function<SpriteIdentifier, Sprite> spriteGetter = material -> {
                    Sprite sprite = atlasSpriteGetter.apply(location, new SpriteIdentifier(
                            material.getAtlasId(),
                            material.getTextureId().withSuffixedPath("_cheesed")
                    ));
                    return sprite.getContents().getId().equals(MissingSprite.getMissingSpriteId()) ?
                            atlasSpriteGetter.apply(location, material) : sprite;
                };


                SimpleModelBaker baker = new SimpleModelBaker(bakedModels, unbakedModels, modelsToBake.get(MISSING_ID), spriteGetter);
                bakedModels.put(location, new CheesedBakedModel(bakedModels.get(location), baker.bake(location, ModelRotation.X0_Y0)));
            }
        }));
    }
}
