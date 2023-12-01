package me.ftmc.hotpot.blocks

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.items.HotpotPlaceableBlockItem
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World


class HotpotPlaceableBlock : BlockWithEntity(
    Settings.create()
        .solid()
        .nonOpaque()
        .mapColor(MapColor.GRAY)
        .sounds(BlockSoundGroup.COPPER)
        .strength(0.5f)
) {
    @Deprecated("Deprecated in Java")
    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return SHAPE
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(
        state: BlockState,
        level: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        result: BlockHitResult
    ): ActionResult {
        val selfPos = BlockPosWithLevel(level, pos)
        val itemStack = player.getStackInHand(hand)
        val hotpotPlaceableBlockEntity = selfPos.blockEntity
        if (hotpotPlaceableBlockEntity is HotpotPlaceableBlockEntity) {
            if (itemStack.itemMatches {
                    val item = it.value()
                    item is HotpotPlaceableBlockItem && item.shouldPlace(player, hand, selfPos)
                }) {
                return ActionResult.PASS
            }
            if (selfPos.isServerSide) {
                val hitPos = HotpotPlaceableBlockEntity.getHitPos(result)
                hotpotPlaceableBlockEntity.interact(hitPos, player, hand, itemStack, selfPos)
            }
            return ActionResult.success(!selfPos.isServerSide)
        }
        return ActionResult.PASS
    }

    override fun getPickStack(blockGetter: BlockView, pos: BlockPos, state: BlockState): ItemStack {
        return ItemStack.EMPTY
    }

    @Deprecated("Deprecated in Java")
    override fun onStateReplaced(
        state: BlockState,
        level: World,
        pos: BlockPos,
        newState: BlockState,
        b: Boolean
    ) {
        val hotpotPlaceableBlockEntity = level.getBlockEntity(pos)
        if (!state.isOf(newState.block) && hotpotPlaceableBlockEntity is HotpotPlaceableBlockEntity) {
            hotpotPlaceableBlockEntity.onRemove(BlockPosWithLevel(level, pos))
        }
        super.onStateReplaced(state, level, pos, newState, b)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return HotpotPlaceableBlockEntity(pos, state)
    }

    override fun <T : BlockEntity?> getTicker(
        level: World,
        blockState: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (level.isClient) null else checkType(
            blockEntityType,
            BlockEntityRegistrar.HOTPOT_PLACEABLE_BLOCK_ENTITY,
            HotpotPlaceableBlockEntity::tick
        )
    }

    companion object {
        private val SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0)
    }
}
