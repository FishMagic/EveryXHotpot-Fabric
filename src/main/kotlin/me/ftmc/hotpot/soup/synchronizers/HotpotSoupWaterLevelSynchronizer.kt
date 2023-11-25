package me.ftmc.hotpot.soup.synchronizers

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.soup.IHotpotSoup
import kotlin.math.max
import kotlin.math.min


class HotpotSoupWaterLevelSynchronizer : IHotpotSoupSynchronizer {
    private var collectedWaterLevel = 0f
    override fun collect(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        val soup: IHotpotSoup = hotpotBlockEntity.getSoup()
        collectedWaterLevel += soup.getWaterLevel(
            hotpotBlockEntity,
            pos
        ) + soup.getOverflowWaterLevel(hotpotBlockEntity, pos)
    }

    override fun integrate(size: Int, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        val averageWaterLevel =
            max(0.0, min(1.0, (collectedWaterLevel / size).toDouble())).toFloat()
        hotpotBlockEntity.getSoup().setWaterLevel(hotpotBlockEntity, pos, averageWaterLevel)
    }
}
