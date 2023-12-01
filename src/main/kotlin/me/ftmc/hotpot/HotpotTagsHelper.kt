package me.ftmc.hotpot

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement


object HotpotTagsHelper {
    fun hasHotpotTag(itemStack: ItemStack): Boolean {
        return itemStack.hasNbt() && itemStack.nbt
            ?.contains(TAG_LOCATION.toString(), NbtElement.COMPOUND_TYPE.toInt()) ?: false
    }

    fun getHotpotTag(itemStack: ItemStack): NbtCompound {
        return if (hasHotpotTag(itemStack)) itemStack.nbt
            ?.getCompound(TAG_LOCATION.toString()) ?: NbtCompound() else NbtCompound()
    }

    fun setHotpotTag(itemStack: ItemStack, compoundTag: NbtCompound) {
        itemStack.orCreateNbt.put(TAG_LOCATION.toString(), compoundTag)
    }

    fun updateHotpotTag(itemStack: ItemStack, consumer: (NbtCompound) -> Unit) {
        val hotpotTag = if (hasHotpotTag(itemStack)) getHotpotTag(itemStack) else NbtCompound()
        consumer(hotpotTag)
        setHotpotTag(itemStack, hotpotTag)
    }
}
