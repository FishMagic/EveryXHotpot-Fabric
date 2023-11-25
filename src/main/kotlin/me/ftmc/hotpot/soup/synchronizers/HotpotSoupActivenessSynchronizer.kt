package me.ftmc.hotpot.soup.synchronizers

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.soup.IHotpotSoup
import me.ftmc.hotpot.soup.IHotpotSoupWithActiveness
import kotlin.math.max
import kotlin.math.min


class HotpotSoupActivenessSynchronizer : IHotpotSoupSynchronizer {
    private var collectedActiveness = 0f
    override fun collect(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        val soup: IHotpotSoup = hotpotBlockEntity.getSoup()
        if (soup is IHotpotSoupWithActiveness) {
            collectedActiveness += soup.getActiveness(hotpotBlockEntity, pos)
        }
    }

    override fun integrate(size: Int, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        val averageActiveness =
            max(0.0, min(1.0, (collectedActiveness / size).toDouble())).toFloat()
        val withActiveness = hotpotBlockEntity.getSoup()
        if (withActiveness is IHotpotSoupWithActiveness) {
            withActiveness.setActiveness(hotpotBlockEntity, pos, averageActiveness)
        }
    }
}

