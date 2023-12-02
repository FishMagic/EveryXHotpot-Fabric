package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.soup.effects.HotpotEffectHelper
import me.ftmc.hotpot.soup.renderers.HotpotBubbleRenderer
import me.ftmc.hotpot.soup.renderers.IHotpotSoupCustomElementRenderer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random


class HotpotClearSoup : AbstractHotpotWaterBasedSoup() {
    override val id: String
        get() = "ClearSoup"

    override fun animateTick(
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel,
        randomSource: Random
    ) {
    }

    override fun addEffectToItem(itemStack: ItemStack, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        HotpotEffectHelper.saveEffects(itemStack, StatusEffectInstance(EveryXHotpot.HOTPOT_WARM, 15 * 20, 0))
    }

    override val bubbleResourceLocation: Identifier
        get() = Identifier(EveryXHotpot.MOD_ID, "soup/hotpot_clear_soup_bubble")
    override val soupResourceLocation: ModelIdentifier?
        get() = ModelIdentifier(EveryXHotpot.MOD_ID, "soup/hotpot_clear_soup")
    override val customElementRenderers: List<IHotpotSoupCustomElementRenderer>
        get() = listOf(HOTPOT_BUBBLE_RENDERER)

    companion object {
        val HOTPOT_BUBBLE_RENDERER: HotpotBubbleRenderer = HotpotBubbleRenderer(
            0.35f,
            0.6f,
            50,
            ModelIdentifier(EveryXHotpot.MOD_ID, "soup/hotpot_clear_soup_bubble")
        )
    }
}
