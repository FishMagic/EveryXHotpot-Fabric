package me.ftmc.hotpot

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList


interface IHotpotSavableWIthSlot<T : IHotpotSavableWIthSlot<T>> : IHotpotSavable<T> {
    fun save(compoundTag: NbtCompound, slot: Byte): NbtCompound {
        compoundTag.putString("Type", id)
        compoundTag.putByte("Slot", slot)
        return save(compoundTag)
    }

    companion object {
        fun isTagValid(compoundTag: NbtCompound): Boolean {

            return compoundTag.contains("Type", NbtElement.STRING_TYPE.toInt()) && compoundTag.contains(
                "Slot",
                NbtElement.BYTE_TYPE.toInt()
            )
        }

        fun loadAll(listTag: NbtList, size: Int, consumer: (NbtCompound) -> Unit) {
            listTag
                .filter { tag ->
                    tag is NbtCompound && isTagValid(tag) && tag.getByte(
                        "Slot"
                    ) < size
                }
                .forEach { tag -> consumer(tag as NbtCompound) }
        }

        fun <U : IHotpotSavableWIthSlot<*>> saveAll(list: List<U>): NbtList {
            return list.map { savable -> savable.save(NbtCompound(), list.indexOf(savable).toByte()) }
                .toCollection(NbtList())
        }
    }
}
