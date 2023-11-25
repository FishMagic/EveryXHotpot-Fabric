package me.ftmc.hotpot.contents

import me.ftmc.hotpot.BlockPosWithLevel
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.AbstractCookingRecipe
import kotlin.jvm.optionals.getOrNull


class HotpotCampfireRecipeContent : AbstractHotpotCookingRecipeContent {
    constructor(itemStack: ItemStack) : super(itemStack)
    constructor() : super()

    override fun getRecipe(itemStack: ItemStack, pos: BlockPosWithLevel): AbstractCookingRecipe? {
        return HotpotContents.CAMPFIRE_QUICK_CHECK.getFirstMatch(SimpleInventory(itemStack), pos.level).getOrNull()
    }

    override val id: String
        get() = "ItemStack"
}
