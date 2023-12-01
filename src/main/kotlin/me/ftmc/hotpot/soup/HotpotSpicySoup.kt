package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.soup.effects.EffectRegistrar
import me.ftmc.hotpot.soup.effects.HotpotEffectHelper
import me.ftmc.hotpot.soup.renderers.HotpotSpicySoupBubbleRenderer
import me.ftmc.hotpot.soup.renderers.HotpotSpicySoupFloatingPepperRenderer
import me.ftmc.hotpot.soup.renderers.IHotpotSoupCustomElementRenderer
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random


class HotpotSpicySoup : AbstractHotpotWaterBasedSoup() {
    override val id: String
        get() = "spicy_soup"

    override fun animateTick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, randomSource: Random) {}
    override fun addEffectToItem(itemStack: ItemStack, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        HotpotEffectHelper.saveEffects(itemStack, StatusEffectInstance(EffectRegistrar.HOTPOT_WARM, 15 * 20, 0))
        HotpotEffectHelper.saveEffects(itemStack, StatusEffectInstance(EffectRegistrar.HOTPOT_ACRID, 15 * 20, 1))
        HotpotEffectHelper.saveEffects(itemStack, StatusEffectInstance(StatusEffects.SPEED, 10 * 20, 1))
    }

    override val bubbleResourceLocation: Identifier
        get() = Identifier(MOD_ID, "soup/hotpot_spicy_soup_bubble_small")
    override val soupResourceLocation: Identifier
        get() = Identifier(MOD_ID, "soup/hotpot_spicy_soup")
    override val customElementRenderers: List<IHotpotSoupCustomElementRenderer>
        get() = listOf(HOTPOT_BUBBLE_RENDERER, HOTPOT_SPICY_SOUP_FLOATING_PEPPER_RENDERER)

    companion object {
        val HOTPOT_BUBBLE_RENDERER: HotpotSpicySoupBubbleRenderer = HotpotSpicySoupBubbleRenderer()
        val HOTPOT_SPICY_SOUP_FLOATING_PEPPER_RENDERER: HotpotSpicySoupFloatingPepperRenderer =
            HotpotSpicySoupFloatingPepperRenderer()
    }
}
