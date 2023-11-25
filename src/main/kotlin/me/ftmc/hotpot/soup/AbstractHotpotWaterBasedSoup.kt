package me.ftmc.hotpot.soup

import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundEvents


abstract class AbstractHotpotWaterBasedSoup : AbstractEffectiveFluidBasedSoup(
    mapOf(
        Pair(
            { it.isOf(Items.WATER_BUCKET) },
            HotpotFluidRefill(1f, SoundEvents.ITEM_BUCKET_EMPTY) { ItemStack(Items.BUCKET) }
        ),
        Pair(
            { it.isOf(Items.POTION) },
            HotpotFluidRefill(0.333f, SoundEvents.ITEM_BOTTLE_FILL) { ItemStack(Items.GLASS_BOTTLE) }
        )
    )
) {
    override val waterLevelDropRate: Float
        get() = 0.04f
}
