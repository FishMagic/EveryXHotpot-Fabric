package me.ftmc.hotpot.soup

import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.logger
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

object SoupRegistrar {
    val SOUP_REGISTRY_KEY: RegistryKey<Registry<HotpotSoupType<*>>> =
        RegistryKey.ofRegistry(Identifier(MOD_ID, "soup"))

    val SOUPS: DefaultedRegistry<HotpotSoupType<*>> = Registries.create(
        SOUP_REGISTRY_KEY,
        Identifier(MOD_ID, "empty_soup").toString()
    ) { HotpotSoupType { HotpotEmptySoup() } }

    val CLEAR_SOUP: HotpotSoupType<HotpotClearSoup> = Registry.register(
        SOUPS,
        Identifier(MOD_ID, "clear_soup"),
        HotpotSoupType { HotpotClearSoup() }
    )

    val SPICY_SOUP: HotpotSoupType<HotpotSpicySoup> = Registry.register(
        SOUPS,
        Identifier(MOD_ID, "spicy_soup"),
        HotpotSoupType { HotpotSpicySoup() }
    )

    val CHEESE_SOUP: HotpotSoupType<HotpotCheeseSoup> = Registry.register(
        SOUPS,
        Identifier(MOD_ID, "cheese_soup"),
        HotpotSoupType { HotpotCheeseSoup() }
    )

    val LAVA_SOUP: HotpotSoupType<HotpotLavaSoup> = Registry.register(
        SOUPS,
        Identifier(MOD_ID, "lava_soup"),
        HotpotSoupType { HotpotLavaSoup() }
    )

    val EMPTY_SOUP: HotpotSoupType<HotpotEmptySoup> = Registry.register(
        SOUPS,
        Identifier(MOD_ID, "empty_soup"),
        HotpotSoupType { HotpotEmptySoup() }
    )

    fun register() {
        logger.debug("Soups Registered")
    }
}