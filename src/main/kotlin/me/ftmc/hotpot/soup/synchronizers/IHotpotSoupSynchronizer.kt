package me.ftmc.hotpot.soup.synchronizers

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity


interface IHotpotSoupSynchronizer {
    fun collect(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel)
    fun integrate(size: Int, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel)
    fun andThen(after: IHotpotSoupSynchronizer): IHotpotSoupSynchronizer {
        return combine(this, after)
    }

    fun andThen(consumer: (HotpotBlockEntity, BlockPosWithLevel) -> Unit): IHotpotSoupSynchronizer {
        return combine(this, collectOnly(consumer))
    }

    companion object {
        fun empty(): IHotpotSoupSynchronizer {
            return object : IHotpotSoupSynchronizer {
                override fun collect(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {}
                override fun integrate(size: Int, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {}
            }
        }

        fun combine(before: IHotpotSoupSynchronizer, after: IHotpotSoupSynchronizer): IHotpotSoupSynchronizer {
            return object : IHotpotSoupSynchronizer {
                override fun collect(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
                    before.collect(hotpotBlockEntity, pos)
                    after.collect(hotpotBlockEntity, pos)
                }

                override fun integrate(size: Int, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
                    before.integrate(size, hotpotBlockEntity, pos)
                    after.integrate(size, hotpotBlockEntity, pos)
                }
            }
        }

        fun collectOnly(consumer: (HotpotBlockEntity, BlockPosWithLevel) -> Unit): IHotpotSoupSynchronizer {
            return object : IHotpotSoupSynchronizer {
                override fun collect(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
                    consumer(hotpotBlockEntity, pos)
                }

                override fun integrate(size: Int, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {}
            }
        }
    }
}
