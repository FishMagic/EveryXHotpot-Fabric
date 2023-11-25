package me.ftmc.hotpot.spices

import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import java.util.function.Supplier


class HotpotSpiceAssembler(private val craftingContainer: RecipeInputInventory) {
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
            if (!itemStack.isEmpty() && predicate(itemStack)) {
                assembled = itemStack.copyWithCount(1)
                return filter { !predicate(it) }
            }
        }
        return with(supplier)
    }

    fun with(supplier: Supplier<ItemStack>): HotpotSpiceAssembler {
        assembled = supplier.get()
        return this
    }

    fun assemble(): ItemStack {
        return assembled
    }
}

