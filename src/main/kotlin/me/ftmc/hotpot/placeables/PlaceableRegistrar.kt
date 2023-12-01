package me.ftmc.hotpot.placeables

import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.logger
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

object PlaceableRegistrar {
    val PLACEABLE_REGISTRY_KEY: RegistryKey<Registry<HotpotPlaceableType<*>>> =
        RegistryKey.ofRegistry(Identifier(MOD_ID, "placeable"))

    val PLACEABLES: DefaultedRegistry<HotpotPlaceableType<*>> = Registries.create(
        PLACEABLE_REGISTRY_KEY,
        Identifier(MOD_ID, "empty_placeable").toString(),
        object : Registries.Initializer<HotpotPlaceableType<*>> {
            override fun run(registry: Registry<HotpotPlaceableType<*>>?): HotpotPlaceableType<*> {
                return object : HotpotPlaceableType<HotpotEmptyPlaceable> {
                    override fun createPlaceable(): HotpotEmptyPlaceable {
                        return HotpotEmptyPlaceable()
                    }
                }
            }
        }
    )

    val SMALL_PLATE: HotpotPlaceableType<HotpotSmallPlate> = Registry.register(
        PLACEABLES,
        Identifier(MOD_ID, "small_plate"),
        object : HotpotPlaceableType<HotpotSmallPlate> {
            override fun createPlaceable(): HotpotSmallPlate {
                return HotpotSmallPlate()
            }
        }
    )

    val LONG_PLATE: HotpotPlaceableType<HotpotLongPlate> = Registry.register(
        PLACEABLES,
        Identifier(MOD_ID, "long_plate"),
        object : HotpotPlaceableType<HotpotLongPlate> {
            override fun createPlaceable(): HotpotLongPlate {
                return HotpotLongPlate()
            }
        }
    )

    val PLACED_CHOPSTICK: HotpotPlaceableType<HotpotPlacedChopstick> = Registry.register(
        PLACEABLES,
        Identifier(MOD_ID, "placed_chopstick"),
        object : HotpotPlaceableType<HotpotPlacedChopstick> {
            override fun createPlaceable(): HotpotPlacedChopstick {
                return HotpotPlacedChopstick()
            }
        }
    )

    val EMPTY_PLACEABLE: HotpotPlaceableType<HotpotEmptyPlaceable> = Registry.register(
        PLACEABLES,
        Identifier(MOD_ID, "empty_placeable"),
        object : HotpotPlaceableType<HotpotEmptyPlaceable> {
            override fun createPlaceable(): HotpotEmptyPlaceable {
                return HotpotEmptyPlaceable()
            }
        }
    )

    fun register() {
        logger.debug("Placeables Registered")
    }
}