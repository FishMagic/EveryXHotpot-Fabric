package me.ftmc.hotpot.items

import me.ftmc.hotpot.HotpotTagsHelper
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random


class CheesedBakedModel(val originalModel: BakedModel?, val cheeseModel: BakedModel?) : BakedModel {
    override fun getQuads(state: BlockState?, direction: Direction?, randomSource: Random): List<BakedQuad> {
        return originalModel?.getQuads(state, direction, randomSource) ?: listOf()
    }

    override fun useAmbientOcclusion(): Boolean {
        return originalModel?.useAmbientOcclusion() ?: false
    }

    override fun hasDepth(): Boolean {
        return originalModel?.hasDepth() ?: false
    }

    override fun isSideLit(): Boolean {
        return originalModel?.isSideLit ?: false
    }

    override fun isBuiltin(): Boolean {
        return originalModel?.isBuiltin ?: false
    }

    override fun getParticleSprite(): Sprite? {
        return originalModel?.particleSprite
    }

    override fun getOverrides(): ModelOverrideList {

        return object : ModelOverrideList() {
            override fun apply(
                bakedModel: BakedModel,
                itemStack: ItemStack,
                clientLevel: ClientWorld?,
                livingEntity: LivingEntity?,
                seed: Int
            ): BakedModel? {
                return if (isCheesed(itemStack)) cheeseModel?.overrides?.apply(
                    cheeseModel,
                    itemStack,
                    clientLevel,
                    livingEntity,
                    seed
                ) else originalModel?.overrides
                    ?.apply(originalModel, itemStack, clientLevel, livingEntity, seed)
            }
        }
    }

    override fun getTransformation(): ModelTransformation? {
        return originalModel?.transformation
    }

//    fun getRenderPasses(itemStack: ItemStack, fabulous: Boolean): List<BakedModel> {
//        return if (isCheesed(itemStack)) cheeseModel.getRenderPasses(
//            itemStack,
//            fabulous
//        ) else originalModel.getRenderPasses(itemStack, fabulous)
//    }

    private fun isCheesed(itemStack: ItemStack): Boolean {
        return HotpotTagsHelper.hasHotpotTag(itemStack)
                && HotpotTagsHelper.getHotpotTag(itemStack).contains("Cheesed", NbtElement.NUMBER_TYPE.toInt())
                && HotpotTagsHelper.getHotpotTag(itemStack).getBoolean("Cheesed")
    }
}
