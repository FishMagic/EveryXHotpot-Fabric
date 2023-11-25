package me.ftmc.hotpot.contents

import net.minecraft.inventory.Inventory
import net.minecraft.recipe.BlastingRecipe
import net.minecraft.recipe.CampfireCookingRecipe
import net.minecraft.recipe.RecipeManager
import net.minecraft.recipe.RecipeType
import java.util.concurrent.ConcurrentHashMap


object HotpotContents {
    val CAMPFIRE_QUICK_CHECK: RecipeManager.MatchGetter<Inventory, CampfireCookingRecipe> =
        RecipeManager.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING)
    val BLAST_FURNACE_QUICK_CHECK: RecipeManager.MatchGetter<Inventory, BlastingRecipe> =
        RecipeManager.createCachedMatchGetter(RecipeType.BLASTING)
    val HOTPOT_CONTENT_TYPES: ConcurrentHashMap<String, () -> IHotpotContent> =
        ConcurrentHashMap(
            mapOf(
                "ItemStack" to ::HotpotCampfireRecipeContent,
                "BlastingItemStack" to ::HotpotBlastFurnaceRecipeContent,
                "Player" to ::HotpotPlayerContent,
                "Empty" to ::HotpotEmptyContent
            )

        )
    val emptyContent: () -> IHotpotContent
        get() = HOTPOT_CONTENT_TYPES["Empty"]!!

    fun getContentOrElseEmpty(key: String): () -> IHotpotContent {
        return HOTPOT_CONTENT_TYPES[key] ?: emptyContent
    }
}
