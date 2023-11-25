package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.HotpotCampfireRecipeContent
import me.ftmc.hotpot.contents.IHotpotContent
import me.ftmc.hotpot.soup.synchronizers.HotpotSoupActivenessSynchronizer
import me.ftmc.hotpot.soup.synchronizers.IHotpotSoupSynchronizer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Hand
import kotlin.math.max
import kotlin.math.min


abstract class AbstractHotpotFluidBasedSoup(private val refills: Map<(ItemStack) -> Boolean, HotpotFluidRefill>) :
    AbstractHotpotSoup(), IHotpotSoupWithActiveness {
    private var activeness = 0f

    override fun load(compoundTag: NbtCompound): IHotpotSoup {
        activeness = compoundTag.getFloat("Activeness")
        return super.load(compoundTag)
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        compoundTag.putFloat("Activeness", activeness)
        return super.save(compoundTag)
    }

    override fun isValid(compoundTag: NbtCompound): Boolean {
        return super.isValid(compoundTag) && compoundTag.contains("Activeness", NbtElement.FLOAT_TYPE.toInt())
    }

    override fun contentUpdate(
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ) {
        super.contentUpdate(content, hotpotBlockEntity, pos)
        if (content is HotpotCampfireRecipeContent) {
            activeness = 1f.coerceAtMost(
                activeness + 0.025f * (content.foodProperties?.hunger ?: 1)
            )
        }
    }

    override fun interact(
        hitSection: Int,
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        selfPos: BlockPosWithLevel
    ): IHotpotContent? {
        return if (ifMatchedFluidFilled(itemStack) { hotpotRefillReturnable: HotpotFluidRefill? ->
                setWaterLevel(
                    hotpotBlockEntity,
                    selfPos,
                    getWaterLevel(hotpotBlockEntity, selfPos) + hotpotRefillReturnable!!.waterLevel
                )
                player.setStackInHand(
                    hand,
                    ItemUsage.exchangeStack(itemStack, player, hotpotRefillReturnable.returned())
                )
                selfPos.level
                    .playSound(
                        null,
                        selfPos.pos,
                        hotpotRefillReturnable.soundEvent,
                        SoundCategory.BLOCKS,
                        1.0f,
                        1.0f
                    )
            }) {
            null
        } else super.interact(hitSection, player, hand, itemStack, hotpotBlockEntity, selfPos)
    }

    override fun getContentTickSpeed(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Int {
        return Math.round(2f * (getWaterLevel() * 2f - 1f) + activeness * 4f)
    }

    override fun tick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        setWaterLevel(
            getWaterLevel(
                hotpotBlockEntity,
                pos
            ) - (if (hotpotBlockEntity.isInfiniteWater) 0f else waterLevelDropRate) / 20f / 60f
        )
        activeness = max(0.0, (activeness - 0.55f / 20f / 60f).toDouble()).toFloat()
    }

    abstract val waterLevelDropRate: Float

    override fun getSynchronizer(
        selfHotpotBlockEntity: HotpotBlockEntity,
        selfPos: BlockPosWithLevel
    ): IHotpotSoupSynchronizer? {
        return (super.getSynchronizer(selfHotpotBlockEntity, selfPos) ?: (IHotpotSoupSynchronizer.empty()))
            .andThen(HotpotSoupActivenessSynchronizer())
    }

    override fun getActiveness(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Float {
        return activeness
    }

    override fun setActiveness(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, activeness: Float) {
        this.activeness = min(1.0, max(0.0, activeness.toDouble())).toFloat()
    }

    fun ifMatchedFluidFilled(itemStack: ItemStack, consumer: (HotpotFluidRefill) -> Unit): Boolean {
        return refills.keys.firstOrNull { it(itemStack) }?.let {
            consumer(refills[it]!!)
            return true
        } ?: false
    }

    class HotpotFluidRefill(val waterLevel: Float, val soundEvent: SoundEvent, val returned: () -> ItemStack)
}
