package me.ftmc.hotpot.contents

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.AbstractCookingRecipe


abstract class AbstractHotpotCookingRecipeContent : AbstractHotpotItemStackContent {
    constructor(itemStack: ItemStack) : super(itemStack)
    constructor() : super()

    abstract fun getRecipe(itemStack: ItemStack, pos: BlockPosWithLevel): AbstractCookingRecipe?
    override fun remapCookingTime(
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): Int {
        return getRecipe(itemStack, pos)?.cookTime ?: -1
    }

    override fun remapExperience(
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): Float? {
        return getRecipe(itemStack, pos)?.experience
    }

    override fun remapResult(
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack? {
        return getRecipe(itemStack, pos)?.craft(
            SimpleInventory(itemStack),
            pos.level.registryManager
        )

    }

    val iD: String
        get() = "BlastingItemStack"

    companion object {
        fun hasBlastingRecipe(itemStack: ItemStack?, pos: BlockPosWithLevel): Boolean {
            return HotpotContents.BLAST_FURNACE_QUICK_CHECK.getFirstMatch(SimpleInventory(itemStack), pos.level)
                .isPresent
        }
    }
}

