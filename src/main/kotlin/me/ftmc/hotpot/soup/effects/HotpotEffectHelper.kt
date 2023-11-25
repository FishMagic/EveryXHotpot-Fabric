package me.ftmc.hotpot.soup.effects

import me.ftmc.hotpot.HotpotTagsHelper
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import java.util.stream.Collectors


object HotpotEffectHelper {
    fun saveEffects(itemStack: ItemStack, mobEffectInstance: StatusEffectInstance) {
        val effects =
            HotpotTagsHelper.getHotpotTag(itemStack).getList("HotpotEffects", NbtElement.COMPOUND_TYPE.toInt())
                .map { tag -> tag as NbtCompound }.mapNotNull(StatusEffectInstance::fromNbt).toMutableList()
        mergeEffects(effects, mobEffectInstance)
        HotpotTagsHelper.updateHotpotTag(itemStack) { compoundTag ->
            compoundTag.put(
                "HotpotEffects",
                effects.map { it.writeNbt(NbtCompound()) }.stream().collect(Collectors.toCollection(::NbtList))
            )
        }
    }

    fun hasEffects(itemStack: ItemStack): Boolean {
        return HotpotTagsHelper.hasHotpotTag(itemStack) && HotpotTagsHelper.getHotpotTag(itemStack)
            .contains("HotpotEffects", NbtElement.LIST_TYPE.toInt())
    }

    fun listEffects(itemStack: ItemStack, consumer: (StatusEffectInstance) -> Unit) {
        if (!hasEffects(itemStack)) return
        HotpotTagsHelper.getHotpotTag(itemStack).getList("HotpotEffects", NbtElement.COMPOUND_TYPE.toInt())
            .mapNotNull { tag -> StatusEffectInstance.fromNbt(tag as NbtCompound) }
            .forEach(consumer)
    }

    fun getListEffects(itemStack: ItemStack): List<StatusEffectInstance> {
        val effects: MutableList<StatusEffectInstance> = mutableListOf()
        listEffects(itemStack) { effects.add(it) }
        return effects
    }

    fun mergeEffects(effects: List<StatusEffectInstance>): List<StatusEffectInstance> {
        val list: MutableList<StatusEffectInstance> = mutableListOf()
        effects.forEach { mergeEffects(list, it) }
        return list
    }

    fun mergeEffects(effects: MutableList<StatusEffectInstance>, mobEffectInstance: StatusEffectInstance) {
        for (effect in effects) {
            if (effect.effectType.equals(mobEffectInstance.effectType)) {
                effect.upgrade(mobEffectInstance)
                return
            }
        }
        effects.add(mobEffectInstance)
    }
}
