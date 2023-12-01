package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.HotpotContents
import me.ftmc.hotpot.contents.IHotpotContent
import me.ftmc.hotpot.soup.renderers.IHotpotSoupCustomElementRenderer
import me.ftmc.hotpot.soup.synchronizers.IHotpotSoupSynchronizer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.potion.PotionUtil
import net.minecraft.potion.Potions
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random
import java.util.concurrent.ConcurrentHashMap


class HotpotEmptySoup : IHotpotSoup {
    override fun load(compoundTag: NbtCompound): IHotpotSoup {
        return this
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        return compoundTag
    }

    override fun isValid(compoundTag: NbtCompound): Boolean {
        return true
    }

    override val id: String
        get() = "empty_soup"

    override fun interact(
        hitSection: Int,
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        selfPos: BlockPosWithLevel
    ): IHotpotContent? {
        ifMatchEmptyFill(itemStack) { returnable: HotpotEmptyFill? ->
            player.setStackInHand(hand, ItemUsage.exchangeStack(itemStack, player, returnable!!.returned()))
            hotpotBlockEntity.setSoup(returnable.soup(), selfPos)
            hotpotBlockEntity.getSoup().setWaterLevel(hotpotBlockEntity, selfPos, returnable.waterLevel)
            selfPos.level.playSound(null, selfPos.pos, returnable.soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f)
        }
        return null
    }

    override fun remapContent(
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): IHotpotContent {
        return HotpotContents.emptyContent.createContent()
    }

    override fun getSynchronizer(
        selfHotpotBlockEntity: HotpotBlockEntity,
        selfPos: BlockPosWithLevel
    ): IHotpotSoupSynchronizer? {
        return null
    }

    override fun takeOutContentViaChopstick(
        content: IHotpotContent,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack {
        return itemStack
    }

    override fun takeOutContentViaHand(
        content: IHotpotContent,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ) {
        pos.dropItemStack(itemStack)
    }

    override fun contentUpdate(content: IHotpotContent, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
    }

    override fun animateTick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, randomSource: Random) {
    }

    override fun getWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Float {
        return 0f
    }

    override fun getOverflowWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Float {
        return 0f
    }

    override fun setWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, waterLevel: Float) {}
    override fun discardOverflowWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {}
    override fun getContentTickSpeed(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Int {
        return 0
    }

    override fun entityInside(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, entity: Entity) {}
    override fun tick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {}
    override val bubbleResourceLocation: Identifier?
        get() = null
    override val soupResourceLocation: Identifier?
        get() = null
    override val customElementRenderers: List<IHotpotSoupCustomElementRenderer>
        get() = listOf()

    class HotpotEmptyFill(
        val soup: () -> IHotpotSoup,
        val waterLevel: Float,
        val soundEvent: SoundEvent,
        val returned: () -> ItemStack
    )

    companion object {
        val HOTPOT_EMPTY_FILL_TYPES: ConcurrentHashMap<(ItemStack) -> Boolean, HotpotEmptyFill> =
            ConcurrentHashMap(
                mapOf(
                    Pair(
                        { it.isOf(Items.WATER_BUCKET) },
                        HotpotEmptyFill(
                            { SoupRegistrar.CLEAR_SOUP.createSoup() }, 1f, SoundEvents.ITEM_BUCKET_EMPTY
                        ) { ItemStack(Items.BUCKET) }
                    ),
                    Pair(
                        { it.isOf(Items.POTION) && PotionUtil.getPotion(it) === Potions.WATER },
                        HotpotEmptyFill(
                            { SoupRegistrar.CLEAR_SOUP.createSoup() }, 0.333f, SoundEvents.ITEM_BOTTLE_FILL
                        ) { ItemStack(Items.GLASS_BOTTLE) }
                    ),
                    Pair(
                        { it.isOf(Items.MILK_BUCKET) },
                        HotpotEmptyFill(
                            { SoupRegistrar.CHEESE_SOUP.createSoup() }, 1f, SoundEvents.ITEM_BUCKET_EMPTY
                        ) { ItemStack(Items.BUCKET) }
                    ),
                    Pair(
                        { it.isOf(Items.LAVA_BUCKET) },
                        HotpotEmptyFill(
                            { SoupRegistrar.LAVA_SOUP.createSoup() }, 1f, SoundEvents.ITEM_BUCKET_EMPTY_LAVA
                        ) { ItemStack(Items.BUCKET) })
                )
            )

        fun ifMatchEmptyFill(itemStack: ItemStack, consumer: (HotpotEmptyFill) -> Unit) {
            HOTPOT_EMPTY_FILL_TYPES.keys.firstOrNull { it(itemStack) }?.let { consumer(HOTPOT_EMPTY_FILL_TYPES[it]!!) }
        }
    }
}

