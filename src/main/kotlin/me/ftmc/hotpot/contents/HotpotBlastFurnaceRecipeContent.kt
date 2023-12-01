package me.ftmc.hotpot.contents

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.BlastingRecipe
import kotlin.jvm.optionals.getOrNull


class HotpotBlastFurnaceRecipeContent : AbstractHotpotCookingRecipeContent {
    constructor(itemStack: ItemStack) : super(itemStack)
    constructor() : super()

    override fun getRecipe(itemStack: ItemStack, pos: BlockPosWithLevel): BlastingRecipe? {
        return HotpotContents.BLAST_FURNACE_QUICK_CHECK.getFirstMatch(SimpleInventory(itemStack), pos.level).getOrNull()
    }

    override fun remapCookingTime(
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): Int {
        return (super.remapCookingTime(itemStack, hotpotBlockEntity, pos) * 1.5f).toInt()
    }

    override val id: String
        get() = "blasting_recipe_content"

    companion object {
        fun hasBlastingRecipe(itemStack: ItemStack, pos: BlockPosWithLevel): Boolean {
            return HotpotContents.BLAST_FURNACE_QUICK_CHECK.getFirstMatch(SimpleInventory(itemStack), pos.level)
                .isPresent
        }
    }
}
