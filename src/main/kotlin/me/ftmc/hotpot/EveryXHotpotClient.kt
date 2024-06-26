package me.ftmc.hotpot

import me.ftmc.hotpot.blocks.BlockEntityRegistrar
import me.ftmc.hotpot.blocks.BlockRegistrar
import me.ftmc.hotpot.blocks.HotpotBlockEntityRenderer
import me.ftmc.hotpot.blocks.HotpotPlateBlockEntityRenderer
import me.ftmc.hotpot.event.GetTooltipEventCallback
import me.ftmc.hotpot.items.HotpotBlockEntityWithoutLevelRenderer
import me.ftmc.hotpot.items.HotpotChopstickItem
import me.ftmc.hotpot.items.ItemRegistrar
import me.ftmc.hotpot.soup.effects.HotpotEffectHelper
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionUtil
import net.minecraft.text.Text
import net.minecraft.util.Identifier


@Environment(EnvType.CLIENT)
object EveryXHotpotClient : ClientModInitializer {

    val HOTPOT_BEWLR = HotpotBlockEntityWithoutLevelRenderer()

    override fun onInitializeClient() {
        BlockEntityRendererFactories.register(BlockEntityRegistrar.HOTPOT_BLOCK_ENTITY, ::HotpotBlockEntityRenderer)
        BlockEntityRendererFactories.register(
            BlockEntityRegistrar.HOTPOT_PLACEABLE_BLOCK_ENTITY,
            ::HotpotPlateBlockEntityRenderer
        )
//        BuiltinItemRendererRegistry.INSTANCE.register(HOTPOT_LONG_PLATE_BLOCK_ITEM, HOTPOT_BEWLR)
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BlockRegistrar.HOTPOT_BLOCK)
        ModelLoadingPlugin.register {
            it.addModels(
                Identifier(MOD_ID, "soup/hotpot_clear_soup_bubble"),
                Identifier(MOD_ID, "soup/hotpot_clear_soup"),
                Identifier(MOD_ID, "soup/hotpot_spicy_soup_bubble_small"),
                Identifier(MOD_ID, "soup/hotpot_spicy_soup_bubble_large"),
                Identifier(MOD_ID, "soup/hotpot_spicy_soup_floating_pepper_1"),
                Identifier(MOD_ID, "soup/hotpot_spicy_soup_floating_pepper_2"),
                Identifier(MOD_ID, "soup/hotpot_spicy_soup"),
                Identifier(MOD_ID, "soup/hotpot_cheese_soup_bubble"),
                Identifier(MOD_ID, "soup/hotpot_cheese_soup"),
                Identifier(MOD_ID, "soup/hotpot_lava_soup"),
                Identifier(MOD_ID, "item/hotpot_chopstick_model"),
                Identifier(MOD_ID, "block/hotpot_plate_long"),
                Identifier(MOD_ID, "block/hotpot_plate_small"),
                Identifier(MOD_ID, "block/hotpot_chopstick_stand"),
                Identifier(MOD_ID, "item/hotpot_spice_pack_model")
            )
        }
        GetTooltipEventCallback.EVENT.register(object : GetTooltipEventCallback {
            override fun getTooltip(
                stack: ItemStack,
                list: MutableList<Text>,
                player: PlayerEntity?,
                context: TooltipContext
            ): MutableList<Text> {
                var realItem = stack
                if (stack.isOf(ItemRegistrar.HOTPOT_CHOPSTICK)) {
                    realItem = HotpotChopstickItem.getChopstickFoodItemStack(stack)
                }

                if (realItem.isEmpty) {
                    return list
                }

                if (HotpotEffectHelper.hasEffects(realItem)) {
                    PotionUtil.buildTooltip(HotpotEffectHelper.getListEffects(realItem), list, 1f)
                }

                return list
            }
        })
    }
}