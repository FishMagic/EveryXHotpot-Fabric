package me.ftmc.hotpot.contents

import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.logger
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

object ContentRegistrar {
    val CONTENT_REGISTRY_KEY: RegistryKey<Registry<HotpotContentType<*>>> =
        RegistryKey.ofRegistry(Identifier(MOD_ID, "content"))

    val CONTENTS: DefaultedRegistry<HotpotContentType<*>> = Registries.create(
        CONTENT_REGISTRY_KEY,
        Identifier(MOD_ID, "empty_content").toString(),
        object : Registries.Initializer<HotpotContentType<*>> {
            override fun run(registry: Registry<HotpotContentType<*>>?): HotpotContentType<*> {
                return object : HotpotContentType<HotpotEmptyContent> {
                    override fun createContent(): HotpotEmptyContent {
                        return HotpotEmptyContent()
                    }
                }
            }
        })

    val CAMPFIRE_RECIPE_CONTENT: HotpotContentType<HotpotCampfireRecipeContent> = Registry.register(
        CONTENTS,
        Identifier(MOD_ID, "campfire_recipe_content"),
        object : HotpotContentType<HotpotCampfireRecipeContent> {
            override fun createContent(): HotpotCampfireRecipeContent {
                return HotpotCampfireRecipeContent()
            }
        })

    val BLASTING_RECIPE_CONTENT: HotpotContentType<HotpotBlastFurnaceRecipeContent> = Registry.register(
        CONTENTS,
        Identifier(MOD_ID, "blasting_recipe_content"),
        object : HotpotContentType<HotpotBlastFurnaceRecipeContent> {
            override fun createContent(): HotpotBlastFurnaceRecipeContent {
                return HotpotBlastFurnaceRecipeContent()
            }
        }
    )

    val PLAYER_CONTENT: HotpotContentType<HotpotPlayerContent> = Registry.register(
        CONTENTS,
        Identifier(MOD_ID, "player_content"),
        object : HotpotContentType<HotpotPlayerContent> {
            override fun createContent(): HotpotPlayerContent {
                return HotpotPlayerContent()
            }
        }
    )

    val EMPTY_CONTENT: HotpotContentType<HotpotEmptyContent> = Registry.register(
        CONTENTS,
        Identifier(MOD_ID, "empty_content"),
        object : HotpotContentType<HotpotEmptyContent> {
            override fun createContent(): HotpotEmptyContent {
                return HotpotEmptyContent()
            }
        }
    )

    fun register() {
        logger.debug("Contents Registered")
    }
}