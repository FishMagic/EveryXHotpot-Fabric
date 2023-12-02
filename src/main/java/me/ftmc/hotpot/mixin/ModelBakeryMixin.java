package me.ftmc.hotpot.mixin;

import me.ftmc.hotpot.items.CheesedBakedModel;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
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
    @Nullable
    private SpriteAtlasManager spriteAtlasManager;

    @Shadow
    public abstract UnbakedModel getOrLoadModel(Identifier p_209597_1_);

    @Shadow
    @Final
    private static ItemModelGenerator ITEM_MODEL_GENERATOR;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "upload", at = @At("RETURN"))
    public void uploadTextures(TextureManager textureManager, Profiler profiler, CallbackInfoReturnable<SpriteAtlasManager> cir) {
        modelsToBake.forEach(((location, unbakedModel) -> {
            if (unbakedModel instanceof JsonUnbakedModel && ((JsonUnbakedModel) unbakedModel).getRootModel() == GENERATION_MARKER) {
                Function<SpriteIdentifier, Sprite> spriteGetter = material -> spriteAtlasManager.getSprite(new SpriteIdentifier(
                        material.getAtlasId(),
                        new Identifier(material.getTextureId().getNamespace(), material.getTextureId().getPath().concat("_cheesed"))
                ));

                BakedModel bakedModel = null;

                try {
                    UnbakedModel iunbakedmodel = getOrLoadModel(location);
                    if (iunbakedmodel instanceof JsonUnbakedModel) {
                        JsonUnbakedModel blockmodel = (JsonUnbakedModel) iunbakedmodel;
                        if (blockmodel.getRootModel() == GENERATION_MARKER) {
                            bakedModel = ITEM_MODEL_GENERATOR.create(spriteGetter, blockmodel).bake((ModelLoader) (Object) this, blockmodel, spriteGetter, ModelRotation.X0_Y0, location, false);
                        }
                    } else {
                        bakedModel = iunbakedmodel.bake((ModelLoader) (Object) this, spriteGetter, ModelRotation.X0_Y0, location);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    LOGGER.warn("Unable to bake model: '{}': {}", location, exception);
                }

                if (bakedModel != null) {
                    bakedModels.put(location, new CheesedBakedModel(bakedModels.get(location), bakedModel));
                }
            }
        }));
    }
}
