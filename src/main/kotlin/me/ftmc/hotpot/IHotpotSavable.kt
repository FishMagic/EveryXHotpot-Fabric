package me.ftmc.hotpot

import net.minecraft.nbt.NbtCompound


interface IHotpotSavable<T : IHotpotSavable<T>> {
    fun load(compoundTag: NbtCompound): T
    fun save(compoundTag: NbtCompound): NbtCompound
    fun isValid(compoundTag: NbtCompound): Boolean
    val id: String?

    fun loadOrElseGet(compoundTag: NbtCompound, supplier: () -> T): T {
        return if (isValid(compoundTag)) load(compoundTag) else supplier()
    }
}
