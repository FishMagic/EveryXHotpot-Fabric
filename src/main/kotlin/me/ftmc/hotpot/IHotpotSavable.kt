package me.ftmc.hotpot

import net.minecraft.nbt.NbtCompound
import java.util.function.Supplier


interface IHotpotSavable<T : IHotpotSavable<T>> {
    fun load(compoundTag: NbtCompound): T
    fun save(compoundTag: NbtCompound): NbtCompound
    fun isValid(compoundTag: NbtCompound): Boolean
    val id: String?

    fun loadOrElseGet(compoundTag: NbtCompound, supplier: Supplier<T>): T {
        return if (isValid(compoundTag)) load(compoundTag) else supplier.get()
    }
}
