package me.ftmc.hotpot.spices

import me.ftmc.hotpot.forge.net.minecraft.item.copyWithCount
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack


class HotpotSpiceAssembler(private val craftingContainer: CraftingInventory) {
    private var assembled: ItemStack = ItemStack.EMPTY
    private var filter: (ItemStack) -> Boolean = { true }

    fun filter(predicate: (ItemStack) -> Boolean): HotpotSpiceAssembler {
        filter = predicate
        return this
    }

    fun forEach(consumer: (ItemStack, ItemStack) -> Unit): HotpotSpiceAssembler {
        for (i in 0 until craftingContainer.size()) {
            val itemStack: ItemStack = craftingContainer.getStack(i)
            if (!itemStack.isEmpty && filter(itemStack)) {
                consumer(assembled, itemStack)
            }
        }
        return this
    }

    fun withExisting(predicate: (ItemStack) -> Boolean, supplier: () -> ItemStack): HotpotSpiceAssembler {
        for (i in 0 until craftingContainer.size()) {
            val itemStack: ItemStack = craftingContainer.getStack(i)
            if (!itemStack.isEmpty && predicate(itemStack)) {
                assembled = itemStack.copyWithCount(1)
                return filter { !predicate(it) }
            }
        }
        return with(supplier)
    }

    fun with(supplier: () -> ItemStack): HotpotSpiceAssembler {
        assembled = supplier()
        return this
    }

    fun assemble(): ItemStack {
        return assembled
    }
}

