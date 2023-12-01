package me.ftmc.hotpot

import me.ftmc.hotpot.blocks.BlockEntityRegistrar
import me.ftmc.hotpot.blocks.BlockRegistrar
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.ContentRegistrar
import me.ftmc.hotpot.contents.HotpotPlayerContent
import me.ftmc.hotpot.event.FinishUsingItemCallback
import me.ftmc.hotpot.event.OnDeathEventCallback
import me.ftmc.hotpot.items.HotpotChopstickItem
import me.ftmc.hotpot.items.ItemRegistrar
import me.ftmc.hotpot.placeables.PlaceableRegistrar
import me.ftmc.hotpot.soup.SoupRegistrar
import me.ftmc.hotpot.soup.effects.EffectRegistrar
import me.ftmc.hotpot.soup.effects.HotpotEffectHelper
import me.ftmc.hotpot.spices.HotpotSpicePackRecipe
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageType
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.slf4j.LoggerFactory

const val MOD_ID = "everyxhotpot"

val TAG_LOCATION = Identifier(MOD_ID, "hotpot_tags")

val logger = LoggerFactory.getLogger("everyxhotpot")

object EveryXHotpot : ModInitializer {

    //====================RECIPE BELOW==========================

    val HOTPOT_SPICE_PACK_SPECIAL_RECIPE: SpecialRecipeSerializer<HotpotSpicePackRecipe> = Registry.register(
        Registries.RECIPE_SERIALIZER,
        Identifier(MOD_ID, "crafting_special_hotpot_spice_pack"),
        SpecialRecipeSerializer(::HotpotSpicePackRecipe)
    )

    //===================ITEM GROUP BELOW=======================

    val EVERY_X_HOTPOT_TAB: ItemGroup =
        Registry.register(Registries.ITEM_GROUP, Identifier(MOD_ID, "every_x_hotpot_tab"), FabricItemGroup.builder()
            .icon { ItemRegistrar.HOTPOT_BLOCK_ITEM.defaultStack }
            .displayName(Text.translatable("itemGroup.EveryXHotpot"))
            .entries { _, entries ->
                entries.add(ItemRegistrar.HOTPOT_BLOCK_ITEM)
                entries.add(ItemRegistrar.HOTPOT_SMALL_PLATE_BLOCK_ITEM)
                entries.add(ItemRegistrar.HOTPOT_LONG_PLATE_BLOCK_ITEM)
                entries.add(ItemRegistrar.HOTPOT_CHOPSTICK)
                entries.add(ItemRegistrar.HOTPOT_SPICE_PACK)
            }.build()
        )

    //===================DAMAGE BELOW=======================

    val IN_HOTPOT_DAMAGE_KEY: RegistryKey<DamageType> = RegistryKey.of(
        RegistryKeys.DAMAGE_TYPE,
        Identifier(MOD_ID, "in_hotpot")
    )

    val IN_HOTPOT_DAMAGE_TYPE: (World) -> RegistryEntry<DamageType> = {
        it.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(IN_HOTPOT_DAMAGE_KEY)
    }

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        BlockRegistrar.register()
        BlockEntityRegistrar.register()
        ItemRegistrar.register()
        EffectRegistrar.register()
        ContentRegistrar.register()
        PlaceableRegistrar.register()
        SoupRegistrar.register()
        FinishUsingItemCallback.EVENT.register(object : FinishUsingItemCallback {
            override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
                if (user !is ServerPlayerEntity || world.isClient) {
                    return stack
                }
                var realItem = stack
                if (stack.isOf(ItemRegistrar.HOTPOT_CHOPSTICK)) {
                    realItem = HotpotChopstickItem.getChopstickFoodItemStack(stack)
                }
                if (realItem.isEmpty) {
                    return stack
                }
                if (HotpotEffectHelper.hasEffects(realItem)) {
                    HotpotEffectHelper.getListEffects(realItem).forEach { user.addStatusEffect(it) }
                }

                return stack
            }
        })

        OnDeathEventCallback.EVENT.register(object : OnDeathEventCallback {
            override fun onDeath(entity: ServerPlayerEntity, damageSource: DamageSource) {
                if (damageSource.isOf(IN_HOTPOT_DAMAGE_KEY)) {
                    val vec3 = damageSource.position ?: return
                    val pos = BlockPosWithLevel.fromVec3(entity.world, vec3)
                    val hotpotBlockEntity = pos.blockEntity
                    if (hotpotBlockEntity is HotpotBlockEntity) {
                        hotpotBlockEntity.tryPlaceContent(0, HotpotPlayerContent(entity, true), pos)
                        hotpotBlockEntity.tryPlaceContent(0, HotpotPlayerContent(entity, false), pos)
                        hotpotBlockEntity.tryPlaceContent(0, HotpotPlayerContent(entity, false), pos)
                    }
                }
            }
        })
    }
}