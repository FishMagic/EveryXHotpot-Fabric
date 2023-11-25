package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.HotpotCampfireRecipeContent
import me.ftmc.hotpot.contents.IHotpotContent
import net.minecraft.item.ItemStack


abstract class AbstractEffectiveFluidBasedSoup(refills: Map<(ItemStack) -> Boolean, HotpotFluidRefill>) :
    AbstractHotpotFluidBasedSoup(refills) {
    override fun takeOutContentViaChopstick(
        content: IHotpotContent,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack {
        val result: ItemStack = super.takeOutContentViaChopstick(content, itemStack, hotpotBlockEntity, pos)
        if (content is HotpotCampfireRecipeContent && content.foodProperties != null && content.cookingTime < 0
        ) {
            addEffectToItem(itemStack, hotpotBlockEntity, pos)
        }
        return result
    }

    abstract fun addEffectToItem(itemStack: ItemStack, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel)
    override fun remapItemStack(
        copy: Boolean,
        itemStack: ItemStack,
        pos: BlockPosWithLevel
    ): IHotpotContent {
        return HotpotCampfireRecipeContent(if (copy) itemStack.copy() else itemStack)
    }
}