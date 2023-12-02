package me.ftmc.hotpot.spices

import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.HotpotTagsHelper
import me.ftmc.hotpot.forge.net.minecraft.item.copyWithCount
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.tag.ItemTags
import net.minecraft.util.Identifier
import net.minecraft.world.World


class HotpotSpicePackRecipe(id: Identifier) : SpecialCraftingRecipe(id) {
    override fun matches(craftingContainer: CraftingInventory, level: World): Boolean {
        val list: MutableList<ItemStack> = mutableListOf()
        return HotpotSpiceMatcher(craftingContainer)
            .with { it.isIn(ItemTags.SMALL_FLOWERS) }
            .collect(list::add).atLeast(1)
            .with {
                it.isOf(EveryXHotpot.HOTPOT_SPICE_PACK)
                        && (if (HotpotTagsHelper.hasHotpotTag(it)) HotpotTagsHelper.getHotpotTag(it)
                    .getList("Spices", NbtElement.COMPOUND_TYPE.toInt())
                    .size else 0) + list.size <= 4
            }.once()
            .withRemaining().empty()
            .match()
    }

    override fun craft(craftingContainer: CraftingInventory): ItemStack {
        return HotpotSpiceAssembler(craftingContainer)
            .withExisting({ it.isOf(EveryXHotpot.HOTPOT_SPICE_PACK) }) { ItemStack(EveryXHotpot.HOTPOT_SPICE_PACK) } /*.filter(itemStack -> !HotpotSpicePackRecipe.PREDICATE.test(itemStack))*/
            .forEach { assembled, itemStack ->
                val list = HotpotTagsHelper.getHotpotTag(assembled).getList("Spices", NbtElement.COMPOUND_TYPE.toInt())
                list.add(itemStack.copyWithCount(1).writeNbt(NbtCompound()))
                HotpotTagsHelper.updateHotpotTag(assembled) { compoundTag -> compoundTag.put("Spices", list) }
                HotpotTagsHelper.updateHotpotTag(assembled) { compoundTag -> compoundTag.putInt("SpiceAmount", 20) }
            }
            .assemble()
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width * width >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return EveryXHotpot.HOTPOT_SPICE_PACK_SPECIAL_RECIPE
    }
}
