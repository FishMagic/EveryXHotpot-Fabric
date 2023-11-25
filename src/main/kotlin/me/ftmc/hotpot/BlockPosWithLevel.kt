package me.ftmc.hotpot

import com.google.common.base.Objects
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.chunk.WorldChunk

class BlockPosWithLevel(val level: World, val pos: BlockPos) {
    val blockEntity: BlockEntity?
        get() = level.getBlockEntity(pos)
    val blockState: BlockState
        get() = level.getBlockState(pos)
    val chunkAt: WorldChunk
        get() = level.getWorldChunk(pos)

    fun <T> mapPos(function: (BlockPos) -> T): T {
        return function(pos)
    }

    fun updatePos(function: (BlockPos) -> BlockPos): BlockPosWithLevel {
        return BlockPosWithLevel(level, function(pos))
    }

    fun dropItemStack(itemStack: ItemStack?) {
        ItemScatterer.spawn(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack)
    }

    fun markAndNotifyBlock() {
        level.setBlockState(pos, blockState, 3, 512)
    }

    fun toVec3(): Vec3d {
        return Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
    }

    fun getSoundType(entity: Entity?): BlockSoundGroup {
        return blockState.block.getSoundGroup(blockState)
    }

    val isServerSide: Boolean
        get() = !level.isClient

    fun north(): BlockPosWithLevel {
        return updatePos(BlockPos::north)
    }

    fun south(): BlockPosWithLevel {
        return updatePos(BlockPos::south)
    }

    fun east(): BlockPosWithLevel {
        return updatePos(BlockPos::east)
    }

    fun west(): BlockPosWithLevel {
        return updatePos(BlockPos::west)
    }

    fun isOf(block: Block?): Boolean {
        return blockState.isOf(block)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val pos1 = other as BlockPosWithLevel
        return Objects.equal(level, pos1.level) && Objects.equal(pos, pos1.pos)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(level, pos)
    }

    class Builder(val level: World) {
        fun of(pos: BlockPos): BlockPosWithLevel {
            return BlockPosWithLevel(level, pos)
        }
    }

    companion object {
        fun fromVec3(level: World, vec: Vec3d): BlockPosWithLevel {
            return BlockPosWithLevel(level, BlockPos(vec.x.toInt(), vec.y.toInt(), vec.z.toInt()))
        }

        fun fromUseOnContext(context: ItemUsageContext): BlockPosWithLevel {
            return BlockPosWithLevel(context.world, context.blockPos)
        }

        fun fromBlockPlaceContext(context: ItemPlacementContext): BlockPosWithLevel {
            return BlockPosWithLevel(context.world, context.blockPos)
        }
    }
}
