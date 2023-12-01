package me.ftmc.hotpot.items

import me.ftmc.hotpot.HotpotTagsHelper
import me.ftmc.hotpot.MOD_ID
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.item.BuiltinModelItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import kotlin.math.max


class HotpotBlockEntityWithoutLevelRenderer : BuiltinModelItemRenderer(null, null) {
    override fun render(
        itemStack: ItemStack,
        displayContext: ModelTransformationMode,
        poseStack: MatrixStack,
        bufferSource: VertexConsumerProvider,
        combinedLight: Int,
        combinedOverlay: Int
    ) {
        if (itemStack.isOf(ItemRegistrar.HOTPOT_CHOPSTICK)) {
            poseStack.push()
            poseStack.translate(0.5f, 0.5f, 0.5f)
            val chopstickModel = MinecraftClient.getInstance()
                .bakedModelManager
                .getModel(Identifier(MOD_ID, "item/hotpot_chopstick_model"))
            MinecraftClient.getInstance()
                .itemRenderer
                .renderItem(
                    itemStack,
                    displayContext,
                    true,
                    poseStack,
                    bufferSource,
                    combinedLight,
                    combinedOverlay,
                    chopstickModel
                )
            poseStack.pop()
            var chopstickFoodItemStack: ItemStack?
            if (!HotpotChopstickItem.getChopstickFoodItemStack(itemStack)
                    .also { chopstickFoodItemStack = it }.isEmpty
            ) {
                poseStack.push()
                poseStack.translate(0.5f, 0.1f, 0.5f)
                poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f))
                MinecraftClient.getInstance()
                    .itemRenderer
                    .renderItem(
                        null,
                        chopstickFoodItemStack,
                        ModelTransformationMode.FIXED,
                        true,
                        poseStack,
                        bufferSource,
                        null,
                        combinedLight,
                        combinedOverlay,
                        ModelTransformationMode.FIXED.ordinal
                    )
                poseStack.pop()
            }
        } else if (itemStack.isOf(ItemRegistrar.HOTPOT_SPICE_PACK)) {
            poseStack.push()
            poseStack.translate(0.5f, 0.5f, 0.5f)
            val spicePackModel = MinecraftClient.getInstance()
                .bakedModelManager
                .getModel(Identifier(MOD_ID, "item/hotpot_spice_pack_model"))
            MinecraftClient.getInstance()
                .itemRenderer
                .renderItem(
                    itemStack,
                    displayContext,
                    true,
                    poseStack,
                    bufferSource,
                    combinedLight,
                    combinedOverlay,
                    spicePackModel
                )
            poseStack.pop()
            poseStack.push()
            val itemStacks: List<ItemStack> =
                if (HotpotTagsHelper.hasHotpotTag(itemStack)) HotpotTagsHelper.getHotpotTag(itemStack)
                    .getList("Spices", NbtElement.COMPOUND_TYPE.toInt())
                    .map { ItemStack.fromNbt(it as NbtCompound) }
                else listOf()
            val startX = (0.3f - 0.3f / (itemStacks.size * 3f) * max(0.0, (itemStacks.size - 1).toDouble())).toFloat()
            poseStack.translate(startX + 0.2f, 0.25f, 0.5f)
            for (spiceItemStack in itemStacks) {
                poseStack.push()
                poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30f))
                poseStack.scale(0.78f, 0.78f, 0.78f)
                MinecraftClient.getInstance()
                    .itemRenderer
                    .renderItem(
                        null,
                        spiceItemStack,
                        ModelTransformationMode.GROUND,
                        true,
                        poseStack,
                        bufferSource,
                        null,
                        combinedLight,
                        combinedOverlay,
                        ModelTransformationMode.FIXED.ordinal
                    )
                poseStack.pop()
                poseStack.translate((0.3f / (itemStacks.size * 1.5f)).toDouble(), 0.0, 0.0)
            }
            poseStack.pop()
        }
    }
}
