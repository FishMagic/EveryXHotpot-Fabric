package me.ftmc.hotpot.forge.net.minecraft.item

import net.minecraft.item.ItemStack


fun ItemStack.copyWithCount(count: Int): ItemStack {
    val copied = this.copy()
    copied.count = count
    return copied
}