package me.ftmc.hotpot.spices

import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.HotpotTagsHelper
import me.ftmc.hotpot.items.ItemRegistrar
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import net.minecraft.world.World


class HotpotSpicePackRecipe(id: Identifier, category: CraftingRecipeCategory) : SpecialCraftingRecipe(id, category) {
    override fun matches(craftingContainer: RecipeInputInventory, level: World): Boolean {
        val list: MutableList<ItemStack> = mutableListOf()
        return HotpotSpiceMatcher(craftingContainer)
            .with { it.isIn(ItemTags.SMALL_FLOWERS) }
            .collect(list::add).atLeast(1)
            .with {
                it.isOf(ItemRegistrar.HOTPOT_SPICE_PACK)
                        && (if (HotpotTagsHelper.hasHotpotTag(it)) HotpotTagsHelper.getHotpotTag(it)
                    .getList("Spices", NbtElement.COMPOUND_TYPE.toInt())
                    .size else 0) + list.size <= 4
            }.once()
            .withRemaining().empty()
            .match()
    }

    override fun craft(craftingContainer: RecipeInputInventory, registryAccess: DynamicRegistryManager): ItemStack {
        return HotpotSpiceAssembler(craftingContainer)
            .withExisting({ it.isOf(ItemRegistrar.HOTPOT_SPICE_PACK) }) { ItemStack(ItemRegistrar.HOTPOT_SPICE_PACK) } /*.filter(itemStack -> !HotpotSpicePackRecipe.PREDICATE.test(itemStack))*/
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
