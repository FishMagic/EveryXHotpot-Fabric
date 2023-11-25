package me.ftmc.hotpot.blocks

import me.ftmc.hotpot.BlockPosWithLevel
import net.minecraft.block.entity.BlockEntity


class BlockEntityFinder<T : BlockEntity>(
    selfPos: BlockPosWithLevel,
    private val targetClass: Class<T>,
    filter: (T, BlockPosWithLevel) -> Boolean
) {
    private val iterator: BlockPosIterator = BlockPosIterator(selfPos) { pos ->
        val blockEntity: BlockEntity? = pos.blockEntity
        targetClass.isInstance(blockEntity) && filter(targetClass.cast(blockEntity), pos)
    }

    fun getAll(predicate: (T, BlockPosWithLevel) -> Boolean): Map<T, BlockPosWithLevel> {
        val map = mutableMapOf<T, BlockPosWithLevel>()
        iterator.forEach { pos ->
            val blockEntity = targetClass.cast(pos?.blockEntity)
            if (predicate(blockEntity, pos!!)) {
                map[blockEntity] = pos
            }
        }
        return map
    }

    val all: Map<T, BlockPosWithLevel>
        get() = getAll { _, _ -> true }

    fun getFirst(
        maximumBlocks: Int,
        predicate: (T, BlockPosWithLevel) -> Boolean,
        consumer: (T, BlockPosWithLevel) -> Unit
    ) {
        var count = 0
        while (iterator.hasNext() && count++ < maximumBlocks) {
            val pos: BlockPosWithLevel = iterator.next()
            val blockEntity = targetClass.cast(pos.blockEntity)
            if (predicate(blockEntity, pos)) {
                consumer(blockEntity, pos)
                break
            }
        }
    }
}

