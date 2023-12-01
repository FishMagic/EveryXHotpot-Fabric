package me.ftmc.hotpot.blocks

import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.logger
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object BlockRegistrar {

    val HOTPOT_BLOCK: HotpotBlock = Registry.register(
        Registries.BLOCK,
        Identifier(MOD_ID, "hotpot"),
        HotpotBlock()
    )

    val HOTPOT_PLACEABLE: HotpotPlaceableBlock = Registry.register(
        Registries.BLOCK,
        Identifier(MOD_ID, "hotpot_plate"),
        HotpotPlaceableBlock()
    )

    fun register() {
        logger.debug("Blocks Registered")
    }
}