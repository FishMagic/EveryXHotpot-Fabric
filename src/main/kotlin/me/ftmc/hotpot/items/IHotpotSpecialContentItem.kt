package me.ftmc.hotpot.items

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.IHotpotContent
import net.minecraft.item.ItemStack


interface IHotpotSpecialContentItem {
    fun onOtherContentUpdate(
        selfItemStack: ItemStack,
        itemStack: ItemStack,
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack

    fun getSelfItemStack(
        selfItemStack: ItemStack,
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack
}

