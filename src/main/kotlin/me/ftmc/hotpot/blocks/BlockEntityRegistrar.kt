package me.ftmc.hotpot.blocks

import com.mojang.datafixers.DSL
import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.logger
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object BlockEntityRegistrar {

    val HOTPOT_BLOCK_ENTITY: BlockEntityType<HotpotBlockEntity> = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier(MOD_ID, "hotpot"),
        FabricBlockEntityTypeBuilder.create(::HotpotBlockEntity, BlockRegistrar.HOTPOT_BLOCK).build(DSL.remainderType())
    )

    val HOTPOT_PLACEABLE_BLOCK_ENTITY: BlockEntityType<HotpotPlaceableBlockEntity> = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier(MOD_ID, "hotpot_placeable"),
        FabricBlockEntityTypeBuilder.create(::HotpotPlaceableBlockEntity, BlockRegistrar.HOTPOT_PLACEABLE)
            .build(DSL.remainderType())
    )

    fun register() {
        logger.debug("Block Entities Registered")
    }
}