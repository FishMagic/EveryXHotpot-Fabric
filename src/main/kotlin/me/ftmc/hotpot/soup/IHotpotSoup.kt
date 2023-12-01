package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.IHotpotSavable
import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.IHotpotContent
import me.ftmc.hotpot.soup.renderers.IHotpotSoupCustomElementRenderer
import me.ftmc.hotpot.soup.synchronizers.IHotpotSoupSynchronizer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random


interface IHotpotSoup : IHotpotSavable<IHotpotSoup> {
    fun interact(
        hitSection: Int,
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        selfPos: BlockPosWithLevel
    ): IHotpotContent?

    fun remapContent(
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): IHotpotContent?

    fun getSynchronizer(
        selfHotpotBlockEntity: HotpotBlockEntity,
        selfPos: BlockPosWithLevel
    ): IHotpotSoupSynchronizer?

    fun takeOutContentViaChopstick(
        content: IHotpotContent,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack

    fun takeOutContentViaHand(
        content: IHotpotContent,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    )

    fun contentUpdate(content: IHotpotContent, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel)
    fun animateTick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, randomSource: Random)
    fun getWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Float
    fun getOverflowWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Float
    fun setWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, waterLevel: Float)
    fun discardOverflowWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel)
    fun getContentTickSpeed(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Int
    fun entityInside(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, entity: Entity)
    fun tick(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel)
    val bubbleResourceLocation: Identifier?
    val soupResourceLocation: Identifier?
    val customElementRenderers: List<IHotpotSoupCustomElementRenderer>

    companion object {

        val ID_FIXES = mapOf(
            "ClearSoup" to "clear_soup",
            "SpicySoup" to "spicy_soup",
            "CheeseSoup" to "cheese_soup",
            "LavaSoup" to "lava_soup",
            "EmptySoup" to "empty_soup",
            "Empty" to "empty_soup"
        )

        fun fixID(id: String): String {
            return ID_FIXES[id] ?: id
        }

        fun loadSoup(compoundTag: NbtCompound): IHotpotSoup {
            return if (isTagValid(compoundTag)) SoupRegistrar.SOUPS.get(
                Identifier(
                    MOD_ID,
                    fixID(compoundTag.getString("Type"))
                )
            ).createSoup() else HotpotSoups.emptySoup.createSoup()
        }

        fun isTagValid(compoundTag: NbtCompound): Boolean {
            return compoundTag.contains("Type", NbtElement.STRING_TYPE.toInt())
        }

        fun save(soup: IHotpotSoup): NbtCompound? {
            val soupTag = NbtCompound()
            soupTag.putString("Type", soup.id)
            return soup.save(soupTag)
        }
    }
}

