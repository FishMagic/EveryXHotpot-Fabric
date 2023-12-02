package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.HotpotBlastFurnaceRecipeContent
import me.ftmc.hotpot.contents.IHotpotContent
import me.ftmc.hotpot.soup.renderers.IHotpotSoupCustomElementRenderer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random


class HotpotLavaSoup : AbstractHotpotFluidBasedSoup(
    mapOf(
        Pair(
            { it.isOf(Items.LAVA_BUCKET) },
            HotpotFluidRefill(1f, SoundEvents.ITEM_BUCKET_EMPTY_LAVA) { ItemStack(Items.BUCKET) }
        )
    )
) {
    override val id: String
        get() = "LavaSoup"

    override fun remapItemStack(copy: Boolean, itemStack: ItemStack, pos: BlockPosWithLevel): IHotpotContent? {
        return if (HotpotBlastFurnaceRecipeContent.hasBlastingRecipe(itemStack, pos))
            HotpotBlastFurnaceRecipeContent(if (copy) itemStack.copy() else itemStack)
        else null
    }

    override fun animateTick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, randomSource: Random) {
    }

    override val waterLevelDropRate: Float
        get() = 0.05f
    override val bubbleResourceLocation: Identifier
        get() = Identifier(EveryXHotpot.MOD_ID, "soup/hotpot_lava_soup_bubble")
    override val soupResourceLocation: ModelIdentifier?
        get() = ModelIdentifier(EveryXHotpot.MOD_ID, "soup/hotpot_lava_soup")
    override val customElementRenderers: List<IHotpotSoupCustomElementRenderer>
        get() = listOf()
}
