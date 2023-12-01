package me.ftmc.hotpot.contents

import net.minecraft.inventory.Inventory
import net.minecraft.recipe.BlastingRecipe
import net.minecraft.recipe.CampfireCookingRecipe
import net.minecraft.recipe.RecipeManager
import net.minecraft.recipe.RecipeType


object HotpotContents {
    val CAMPFIRE_QUICK_CHECK: RecipeManager.MatchGetter<Inventory, CampfireCookingRecipe> =
        RecipeManager.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING)

    val BLAST_FURNACE_QUICK_CHECK: RecipeManager.MatchGetter<Inventory, BlastingRecipe> =
        RecipeManager.createCachedMatchGetter(RecipeType.BLASTING)

    val emptyContent: HotpotContentType<HotpotEmptyContent>
        get() {
            return ContentRegistrar.EMPTY_CONTENT
        }
}
