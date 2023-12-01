package me.ftmc.hotpot.items

import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.blocks.BlockRegistrar
import me.ftmc.hotpot.logger
import me.ftmc.hotpot.placeables.PlaceableRegistrar
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.BlockItem
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ItemRegistrar {

    val HOTPOT_BLOCK_ITEM: BlockItem = Registry.register(
        Registries.ITEM,
        Identifier(MOD_ID, "hotpot"),
        BlockItem(BlockRegistrar.HOTPOT_BLOCK, FabricItemSettings())
    )

    val HOTPOT_SMALL_PLATE_BLOCK_ITEM: HotpotPlaceableBlockItem = Registry.register(
        Registries.ITEM,
        Identifier(MOD_ID, "hotpot_small_plate"),
        HotpotPlaceableBlockItem({
                                     PlaceableRegistrar.PLACEABLES.get(Identifier(MOD_ID, "small_plate"))
                                         .createPlaceable()
                                 })
    )

    val HOTPOT_LONG_PLATE_BLOCK_ITEM: HotpotPlaceableBlockItem = Registry.register(
        Registries.ITEM,
        Identifier(MOD_ID, "hotpot_long_plate"),
        HotpotPlaceableBlockItem({
                                     PlaceableRegistrar.PLACEABLES.get(Identifier(MOD_ID, "long_plate"))
                                         .createPlaceable()
                                 })
    )

    val HOTPOT_CHOPSTICK: HotpotChopstickItem = Registry.register(
        Registries.ITEM,
        Identifier(MOD_ID, "hotpot_chopstick"),
        HotpotChopstickItem()
    )

    val HOTPOT_SPICE_PACK: HotpotSpicePackItem = Registry.register(
        Registries.ITEM,
        Identifier(MOD_ID, "hotpot_spice_pack"),
        HotpotSpicePackItem()
    )

    fun register() {
        logger.debug("Items Registered")
    }
}