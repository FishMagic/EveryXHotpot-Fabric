package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.HotpotTagsHelper
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.IHotpotContent
import me.ftmc.hotpot.soup.effects.HotpotEffectHelper
import me.ftmc.hotpot.soup.renderers.HotpotBubbleRenderer
import me.ftmc.hotpot.soup.renderers.IHotpotSoupCustomElementRenderer
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random


class HotpotCheeseSoup : AbstractEffectiveFluidBasedSoup(
    mapOf(
        Pair(
            { it.isIn(HotpotSoups.MILK_ITEM_TAG) },
            HotpotFluidRefill(1f, SoundEvents.ITEM_BUCKET_EMPTY) { ItemStack(Items.BUCKET) }
        ),
        Pair(
            { it.isIn(HotpotSoups.MILK_BOTTLE_ITEM_TAG) },
            HotpotFluidRefill(1f, SoundEvents.ITEM_BOTTLE_FILL) { ItemStack(Items.GLASS_BOTTLE) }
        )
    )
) {
    override val id: String
        get() = "CheeseSoup"

    override fun animateTick(
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel,
        randomSource: Random
    ) {
    }

    override fun takeOutContentViaChopstick(
        content: IHotpotContent,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack {
        val result: ItemStack = super.takeOutContentViaChopstick(content, itemStack, hotpotBlockEntity, pos)
        HotpotTagsHelper.updateHotpotTag(itemStack) { compoundTag -> compoundTag.putBoolean("Cheesed", true) }
        return result
    }

    override fun addEffectToItem(
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ) {
        HotpotEffectHelper.saveEffects(
            itemStack,
            StatusEffectInstance(EveryXHotpot.HOTPOT_WARM, 15 * 20, 0)
        )
        HotpotEffectHelper.saveEffects(itemStack, StatusEffectInstance(StatusEffects.ABSORPTION, 20 * 20, 2))
        HotpotEffectHelper.saveEffects(itemStack, StatusEffectInstance(StatusEffects.STRENGTH, 20 * 20, 2))
    }

    override val bubbleResourceLocation: Identifier
        get() = Identifier(EveryXHotpot.MOD_ID, "soup/hotpot_cheese_soup_bubble")
    override val soupResourceLocation: Identifier
        get() = Identifier(EveryXHotpot.MOD_ID, "soup/hotpot_cheese_soup")
    override val customElementRenderers: List<IHotpotSoupCustomElementRenderer>
        get() = listOf(HOTPOT_BUBBLE_RENDERER)
    override val waterLevelDropRate: Float
        get() = 0.03f

    companion object {
        val HOTPOT_BUBBLE_RENDERER: HotpotBubbleRenderer = HotpotBubbleRenderer(
            0.35f,
            0.8f,
            55,
            Identifier(EveryXHotpot.MOD_ID, "soup/hotpot_cheese_soup_bubble")
        )
    }
}
