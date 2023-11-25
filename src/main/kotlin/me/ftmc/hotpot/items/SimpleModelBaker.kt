package me.ftmc.hotpot.items

import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ItemModelGenerator
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import java.util.function.Function


class SimpleModelBaker(
    val bakedModels: Map<Identifier, BakedModel>,
    val models: Map<Identifier, UnbakedModel>,
    val missingModel: UnbakedModel,
    private val spriteGetter: Function<SpriteIdentifier, Sprite>
) :
    Baker {
    override fun getOrLoadModel(location: Identifier?): UnbakedModel {
        return models[location] ?: missingModel
    }

    override fun bake(location: Identifier?, modelState: ModelBakeSettings?): BakedModel? {
        return bake(location, modelState, spriteGetter)
    }

    fun bake(
        location: Identifier?,
        state: ModelBakeSettings?,
        spriteGetter: Function<SpriteIdentifier, Sprite>
    ): BakedModel? {
        val model: UnbakedModel = getOrLoadModel(location)
        return bake(location, model, state, spriteGetter)
    }

    fun bake(
        location: Identifier?,
        model: UnbakedModel,
        state: ModelBakeSettings?,
        spriteGetter: Function<SpriteIdentifier, Sprite>
    ): BakedModel? {
        return if (model is JsonUnbakedModel) {
            ItemModelGenerator().create(spriteGetter, model).bake(
                this,
                model,
                spriteGetter,
                ModelRotation.X0_Y0,
                getCheesedLocation(location),
                false
            )
        } else {
            model.bake(this, spriteGetter, state, getCheesedLocation(location))
        }
    }

    companion object {
        fun getCheesedLocation(location: Identifier?): Identifier? {
            return if (location is ModelIdentifier) ModelIdentifier(
                location.withSuffixedPath("_cheesed"),
                location.variant
            ) else location?.withSuffixedPath("_cheesed")
        }
    }
}
