package me.ftmc.hotpot.spices

import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack


class HotpotSpiceMatcher(val items: MutableList<ItemStack>) {
    private var matched = true

    constructor(craftingContainer: RecipeInputInventory) : this(craftingContainer.inputStacks.toMutableList())

    fun collect(predicate: (ItemStack) -> Boolean, consumer: (ItemStack) -> Unit): HotpotSpiceMatcher {
        items.filter(predicate).forEach(consumer)
        return this
    }

    fun discard(list: List<ItemStack>): HotpotSpiceMatcher {
        items.removeAll(list.toSet())
        return this
    }

    fun with(predicate: (ItemStack) -> Boolean): HotpotSpiceMatchContext {
        return HotpotSpiceMatchContext(this, predicate)
    }

    fun withRemaining(): HotpotSpiceMatchContext {
        return HotpotSpiceMatchContext(this) { true }
    }

    fun withEmpty(): HotpotSpiceMatchContext {
        return HotpotSpiceMatchContext(this, ItemStack::isEmpty)
    }

    fun mismatch(): HotpotSpiceMatcher {
        matched = false
        return this
    }

    fun match(): Boolean {
        return items.size == 0 && matched
    }

    class HotpotSpiceMatchContext(private val matcher: HotpotSpiceMatcher, predicate: (ItemStack) -> Boolean) {
        private val collected: MutableList<ItemStack> = mutableListOf()

        init {
            matcher.collect(predicate, collected::add)
        }

        fun collect(consumer: (ItemStack) -> Unit): HotpotSpiceMatchContext {
            collected.forEach(consumer)
            return this
        }

        fun once(): HotpotSpiceMatcher {
            return require(1)
        }

        fun require(count: Int): HotpotSpiceMatcher {
            return range(count, count)
        }

        fun atLeast(from: Int): HotpotSpiceMatcher {
            return range(from, Int.MAX_VALUE)
        }

        fun range(from: Int, to: Int): HotpotSpiceMatcher {
            return if (collected.size in from..to) matcher.discard(collected) else matcher.mismatch()
        }

        fun discard(): HotpotSpiceMatcher {
            return matcher.discard(collected)
        }

        fun empty(): HotpotSpiceMatcher {
            return if (collected.stream()
                    .allMatch(ItemStack::isEmpty)
            ) matcher.discard(collected) else matcher.mismatch()
        }
    }
}
