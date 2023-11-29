package me.ftmc.hotpot.items

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.HotpotTagsHelper
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.IHotpotContent
import me.ftmc.hotpot.soup.effects.HotpotEffectHelper
import net.minecraft.block.SuspiciousStewIngredient
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.potion.PotionUtil
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.round


class HotpotSpicePackItem : Item(Settings()), IHotpotSpecialContentItem {
//    fun initializeClient(consumer: Consumer<IClientItemExtensions?>) {
//        consumer.accept(object : IClientItemExtensions() {
//            val customRenderer: BlockEntityWithoutLevelRenderer
//                get() = HotpotModEntry.HOTPOT_BEWLR
//        })
//    }

    override fun onOtherContentUpdate(
        selfItemStack: ItemStack,
        itemStack: ItemStack,
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack {
        if (itemStack.isOf(EveryXHotpot.HOTPOT_SPICE_PACK)) {
            return itemStack
        }
        if (!isSpiceTagValid(selfItemStack)) {
            return itemStack
        }
        val amount = getSpiceAmount(selfItemStack)
        if (amount <= 0) {
            return itemStack
        }
        setSpiceAmount(selfItemStack, amount - 1)
        getSpiceEffects(selfItemStack).forEach { HotpotEffectHelper.saveEffects(itemStack, it) }
        return itemStack
    }

    override fun getSelfItemStack(
        selfItemStack: ItemStack,
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack {
        return if (isSpiceTagValid(selfItemStack) && getSpiceAmount(selfItemStack) <= 0) ItemStack(EveryXHotpot.HOTPOT_SPICE_PACK) else selfItemStack
    }

    override fun appendTooltip(
        itemStack: ItemStack,
        level: World?,
        tooltips: MutableList<Text>,
        flag: TooltipContext
    ) {
        super.appendTooltip(itemStack, level, tooltips, flag)
        if (isSpiceTagValid(itemStack)) {
            tooltips.add(
                Text.translatable("item.everyxhotpot.hotpot_spice_pack.amount", getSpiceAmount(itemStack))
                    .formatted(Formatting.BLUE)
            )
            PotionUtil.buildTooltip(HotpotEffectHelper.mergeEffects(getSpiceEffects(itemStack)), tooltips, 1.0f)
        }
    }

    override fun isItemBarVisible(stack: ItemStack): Boolean {
        return getSpiceAmount(stack) != 20
    }

    override fun getItemBarStep(stack: ItemStack): Int {
        return round((getSpiceAmount(stack) * 13) / 20f).toInt()
    }

    override fun getItemBarColor(stack: ItemStack): Int {
        val f = max(0f, getSpiceAmount(stack) / 20f)
        return MathHelper.hsvToRgb(f / 3.0f, 1.0f, 1.0f)
    }

    fun setSpiceAmount(itemStack: ItemStack, amount: Int) {
        HotpotTagsHelper.updateHotpotTag(itemStack) { compoundTag -> compoundTag.putInt("SpiceAmount", amount) }
    }

    private fun getSpiceAmount(itemStack: ItemStack): Int {
        return HotpotTagsHelper.getHotpotTag(itemStack).getInt("SpiceAmount")
    }

    private fun isSpiceTagValid(itemStack: ItemStack): Boolean {
        return HotpotTagsHelper.hasHotpotTag(itemStack) && HotpotTagsHelper.getHotpotTag(itemStack)
            .contains("Spices", NbtElement.LIST_TYPE.toInt()) && HotpotTagsHelper.getHotpotTag(itemStack)
            .contains("SpiceAmount", NbtElement.NUMBER_TYPE.toInt())
    }

    private fun getSpiceEffects(itemStack: ItemStack): List<StatusEffectInstance> {
        return HotpotTagsHelper.getHotpotTag(itemStack).getList("Spices", NbtElement.COMPOUND_TYPE.toInt())
            .asSequence()
            .map { ItemStack.fromNbt(it as NbtCompound) }
            .map { SuspiciousStewIngredient.of(it.item) }
            .filterNotNull()
            .map { StatusEffectInstance(it.effectInStew, it.effectInStewDuration * 2, 1) }
            .toList()
    }
}
