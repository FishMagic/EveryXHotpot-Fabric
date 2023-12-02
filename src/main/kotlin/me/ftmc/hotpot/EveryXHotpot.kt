package me.ftmc.hotpot

import com.mojang.datafixers.DSL
import me.ftmc.hotpot.blocks.HotpotBlock
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.blocks.HotpotPlaceableBlock
import me.ftmc.hotpot.blocks.HotpotPlaceableBlockEntity
import me.ftmc.hotpot.contents.HotpotPlayerContent
import me.ftmc.hotpot.event.FinishUsingItemCallback
import me.ftmc.hotpot.event.OnDeathEventCallback
import me.ftmc.hotpot.items.HotpotChopstickItem
import me.ftmc.hotpot.items.HotpotPlaceableBlockItem
import me.ftmc.hotpot.items.HotpotSpicePackItem
import me.ftmc.hotpot.placeables.HotpotPlaceables
import me.ftmc.hotpot.soup.effects.HotpotEffectHelper
import me.ftmc.hotpot.soup.effects.HotpotMobEffect
import me.ftmc.hotpot.spices.HotpotSpicePackRecipe
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import org.slf4j.LoggerFactory

object EveryXHotpot : ModInitializer {
    private val logger = LoggerFactory.getLogger("everyxhotpot")

    const val MOD_ID = "everyxhotpot"

    val TAG_LOCATION = Identifier(MOD_ID, "hotpot_tags")

    //===================BLOCK BLOW========================

    val HOTPOT_BLOCK: HotpotBlock = Registry.register(
        Registry.BLOCK,
        Identifier(MOD_ID, "hotpot"),
        HotpotBlock()
    )

    val HOTPOT_PLACEABLE: HotpotPlaceableBlock = Registry.register(
        Registry.BLOCK,
        Identifier(MOD_ID, "hotpot_plate"),
        HotpotPlaceableBlock()
    )

    //================BLOCK ENTITY BELOW=======================

    val HOTPOT_BLOCK_ENTITY: BlockEntityType<HotpotBlockEntity> = Registry.register(
        Registry.BLOCK_ENTITY_TYPE,
        Identifier(MOD_ID, "hotpot"),
        FabricBlockEntityTypeBuilder.create(::HotpotBlockEntity, HOTPOT_BLOCK).build(DSL.remainderType())
    )

    val HOTPOT_PLACEABLE_BLOCK_ENTITY: BlockEntityType<HotpotPlaceableBlockEntity> = Registry.register(
        Registry.BLOCK_ENTITY_TYPE,
        Identifier(MOD_ID, "hotpot_placeable"),
        FabricBlockEntityTypeBuilder.create(::HotpotPlaceableBlockEntity, HOTPOT_PLACEABLE).build(DSL.remainderType())
    )

    //===================ITEM GROUP BELOW=======================
    val EVERY_X_HOTPOT_TAB: ItemGroup =
        FabricItemGroupBuilder.create(Identifier(MOD_ID, "everyxhotpot"))
            .icon { HOTPOT_BLOCK_ITEM.defaultStack }
            .build()

    //=====================ITEM BELOW=========================

    val HOTPOT_BLOCK_ITEM: BlockItem = Registry.register(
        Registry.ITEM,
        Identifier(MOD_ID, "hotpot"),
        BlockItem(HOTPOT_BLOCK, FabricItemSettings().group(EVERY_X_HOTPOT_TAB))
    )

    val HOTPOT_SMALL_PLATE_BLOCK_ITEM: HotpotPlaceableBlockItem = Registry.register(
        Registry.ITEM,
        Identifier(MOD_ID, "hotpot_small_plate"),
        HotpotPlaceableBlockItem(HotpotPlaceables.getPlaceableOrElseEmpty("SmallPlate"))
    )

    val HOTPOT_LONG_PLATE_BLOCK_ITEM: HotpotPlaceableBlockItem = Registry.register(
        Registry.ITEM,
        Identifier(MOD_ID, "hotpot_long_plate"),
        HotpotPlaceableBlockItem(HotpotPlaceables.getPlaceableOrElseEmpty("LongPlate"))
    )

    val HOTPOT_CHOPSTICK: HotpotChopstickItem = Registry.register(
        Registry.ITEM,
        Identifier(MOD_ID, "hotpot_chopstick"),
        HotpotChopstickItem()
    )

    val HOTPOT_SPICE_PACK: HotpotSpicePackItem = Registry.register(
        Registry.ITEM,
        Identifier(MOD_ID, "hotpot_spice_pack"),
        HotpotSpicePackItem()
    )

    //===================EFFECT BELOW=======================

    val HOTPOT_WARM: StatusEffect = Registry.register(
        Registry.STATUS_EFFECT,
        Identifier(MOD_ID, "warm"),
        HotpotMobEffect(StatusEffectCategory.BENEFICIAL, 240 shl 16 or (240 shl 8) or 240)
    )

    val HOTPOT_ACRID: StatusEffect = Registry.register(
        Registry.STATUS_EFFECT,
        Identifier(MOD_ID, "acrid"),
        HotpotMobEffect(StatusEffectCategory.BENEFICIAL, 240 shl 16 or (84 shl 8) or 64)
            .addAttributeModifier(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                "46f33e49-ce96-4c75-b126-60a1e4117a8f",
                0.5,
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL
            )
    )

    //====================RECIPE BELOW==========================

    val HOTPOT_SPICE_PACK_SPECIAL_RECIPE: SpecialRecipeSerializer<HotpotSpicePackRecipe> = Registry.register(
        Registry.RECIPE_SERIALIZER,
        Identifier(MOD_ID, "crafting_special_hotpot_spice_pack"),
        SpecialRecipeSerializer(::HotpotSpicePackRecipe)
    )

    //===================DAMAGE BELOW=======================

    val HOTPOT_DAMAGE_SOURCE: DamageSource = DamageSource("in_hotpot").setFire()

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        FinishUsingItemCallback.EVENT.register(object : FinishUsingItemCallback {
            override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
                if (user !is ServerPlayerEntity || world.isClient) {
                    return stack
                }
                var realItem = stack
                if (stack.isOf(HOTPOT_CHOPSTICK)) {
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
                if (damageSource.equals(HOTPOT_DAMAGE_SOURCE)) {
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