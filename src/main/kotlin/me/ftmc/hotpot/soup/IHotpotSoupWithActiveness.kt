package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity


interface IHotpotSoupWithActiveness : IHotpotSoup {
    fun getActiveness(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Float
    fun setActiveness(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, activeness: Float)
}

