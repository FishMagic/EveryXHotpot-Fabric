package me.ftmc.hotpot.blocks

import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import me.ftmc.hotpot.BlockPosWithLevel
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Equipment
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

class HotpotBlock : BlockWithEntity(
    Settings.create()
        .solid()
        .nonOpaque()
        .mapColor(MapColor.IRON_GRAY)
        .sounds(BlockSoundGroup.COPPER)
        .requiresTool()
        .luminance { 15 }
        .strength(3f, 6f)
        .solidBlock(Blocks::never)
), Equipment {
    private val shapesByIndex: Array<VoxelShape> = makeShapes()
    private val stateToIndex: Object2IntMap<BlockState> = Object2IntOpenHashMap()

    companion object {
        val NORTH: BooleanProperty = BooleanProperty.of("north")
        val SOUTH: BooleanProperty = BooleanProperty.of("south")
        val EAST: BooleanProperty = BooleanProperty.of("east")
        val WEST: BooleanProperty = BooleanProperty.of("west")
        val WEST_NORTH: BooleanProperty = BooleanProperty.of("west_north")
        val NORTH_EAST: BooleanProperty = BooleanProperty.of("north_east")
        val EAST_SOUTH: BooleanProperty = BooleanProperty.of("east_south")
        val SOUTH_WEST: BooleanProperty = BooleanProperty.of("south_west")
        val SEPARATOR_NORTH: BooleanProperty = BooleanProperty.of("separator_north")
        val SEPARATOR_SOUTH: BooleanProperty = BooleanProperty.of("separator_south")
        val SEPARATOR_EAST: BooleanProperty = BooleanProperty.of("separator_east")
        val SEPARATOR_WEST: BooleanProperty = BooleanProperty.of("separator_west")
    }

    private fun indexFor(direction: Direction): Int {
        return 1 shl direction.horizontal
    }


    init {
        this.defaultState =
            this.stateManager.defaultState
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(EAST, false)
                .with(WEST, false)
                .with(WEST_NORTH, false)
                .with(NORTH_EAST, false)
                .with(EAST_SOUTH, false)
                .with(SOUTH_WEST, false)
                .with(SEPARATOR_NORTH, false)
                .with(SEPARATOR_SOUTH, false)
                .with(SEPARATOR_EAST, false)
                .with(SEPARATOR_WEST, false)
    }

    private fun makeShapes(): Array<VoxelShape> {
        val base: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
        val south: VoxelShape = createCuboidShape(0.0, 8.0, 15.0, 16.0, 16.0, 16.0) //south(2^0)
        val west: VoxelShape = createCuboidShape(0.0, 8.0, 0.0, 1.0, 16.0, 16.0) //west(2^1)
        val north: VoxelShape = createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 1.0) //north(2^2)
        val east: VoxelShape = createCuboidShape(15.0, 8.0, 0.0, 16.0, 16.0, 16.0) //east(2^3)
        val faces: Array<VoxelShape> = arrayOf<VoxelShape>(
            VoxelShapes.empty(),  //0000 (0)
            south,  //0001 (1)
            west,  //0010 (2)
            VoxelShapes.union(south, west),  //0011 (3)
            north,  //0100 (4)
            VoxelShapes.union(north, south),  //0101 (5)
            VoxelShapes.union(north, west),  //0110 (6)
            VoxelShapes.union(north, west, south),  //0111 (7)
            east,  //1000 (8)
            VoxelShapes.union(east, south),  //1001 (9)
            VoxelShapes.union(east, west),  //1010 (10)
            VoxelShapes.union(east, west, south),  //1011 (11)
            VoxelShapes.union(east, north),  //1100 (12)
            VoxelShapes.union(east, north, south),  //1101 (13)
            VoxelShapes.union(east, north, west),  //1110 (14)
            VoxelShapes.union(east, north, west, south) //1111 (15)
        )
        for (i in faces.indices) {
            faces[i] = VoxelShapes.union(base, faces[i])
        }
        return faces
    }

    private fun updateState(state: BlockState, pos: BlockPos, accessor: WorldAccess): BlockState {
        if (accessor !is World) {
            return defaultState
        }
        val selfPos = BlockPosWithLevel(accessor, pos)
        val north: BlockPosWithLevel = selfPos.north()
        val south: BlockPosWithLevel = selfPos.south()
        val east: BlockPosWithLevel = selfPos.east()
        val west: BlockPosWithLevel = selfPos.west()
        val westNorth: BlockPosWithLevel = north.west()
        val northEast: BlockPosWithLevel = east.north()
        val eastSouth: BlockPosWithLevel = south.east()
        val southWest: BlockPosWithLevel = west.south()
        val northValue: Boolean = north.isOf(BlockRegistrar.HOTPOT_BLOCK)
        val southValue: Boolean = south.isOf(BlockRegistrar.HOTPOT_BLOCK)
        val eastValue: Boolean = east.isOf(BlockRegistrar.HOTPOT_BLOCK)
        val westValue: Boolean = west.isOf(BlockRegistrar.HOTPOT_BLOCK)
        return state
            .with(NORTH, northValue)
            .with(SOUTH, southValue)
            .with(EAST, eastValue)
            .with(WEST, westValue)
            .with(WEST_NORTH, westValue && northValue && westNorth.isOf(BlockRegistrar.HOTPOT_BLOCK))
            .with(NORTH_EAST, northValue && eastValue && northEast.isOf(BlockRegistrar.HOTPOT_BLOCK))
            .with(EAST_SOUTH, eastValue && southValue && eastSouth.isOf(BlockRegistrar.HOTPOT_BLOCK))
            .with(SOUTH_WEST, southValue && westValue && southWest.isOf(BlockRegistrar.HOTPOT_BLOCK))
            .with(SEPARATOR_NORTH, northValue && !HotpotBlockEntity.isSameSoup(selfPos, north))
            .with(SEPARATOR_SOUTH, southValue && !HotpotBlockEntity.isSameSoup(selfPos, south))
            .with(SEPARATOR_EAST, eastValue && !HotpotBlockEntity.isSameSoup(selfPos, east))
            .with(SEPARATOR_WEST, westValue && !HotpotBlockEntity.isSameSoup(selfPos, west))
    }

    private fun getHitSection(result: BlockHitResult): Int {
        val blockPos: BlockPos = result.blockPos.offset(Direction.UP)
        return HotpotBlockEntity.getPosSection(blockPos, result.pos)
    }

    @SuppressWarnings("deprecation")
    private fun getShapeIndex(state: BlockState): Int {
        return stateToIndex.computeIntIfAbsent(state) { blockState: BlockState ->
            var index =
                if (blockState.get(SOUTH)) 0 else (0 or indexFor(Direction.SOUTH))
            index =
                if (blockState.get(WEST)) index else index or indexFor(Direction.WEST)
            index =
                if (blockState.get(NORTH)) index else index or indexFor(Direction.NORTH)
            index =
                if (blockState.get(EAST)) index else index or indexFor(Direction.EAST)
            index
        }
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
        val levelPos = BlockPosWithLevel(level, pos)
        val hotpotBlockEntity = levelPos.blockEntity
        if (hotpotBlockEntity is HotpotBlockEntity) {
            val itemStack: ItemStack = player.getStackInHand(hand)
            val hitSection = getHitSection(result)
            if (levelPos.isServerSide) {
                hotpotBlockEntity.interact(hitSection, player, hand, itemStack, levelPos)
            }
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    @Deprecated("Deprecated in Java")
    override fun onStateReplaced(state: BlockState, level: World, pos: BlockPos, newState: BlockState, b: Boolean) {
        val hotpotBlockEntity = level.getBlockEntity(pos)
        if (!state.isOf(newState.block) && hotpotBlockEntity is HotpotBlockEntity) {
            hotpotBlockEntity.onRemove(BlockPosWithLevel(level, pos))
        }
        super.onStateReplaced(state, level, pos, newState, b)
    }

    @Deprecated("Deprecated in Java")
    @SuppressWarnings("deprecation")
    override fun onEntityCollision(state: BlockState, level: World, pos: BlockPos, entity: Entity) {
        super.onEntityCollision(state, level, pos, entity)
        val levelPos = BlockPosWithLevel(level, pos)
        val hotpotBlockEntity = levelPos.blockEntity
        if (hotpotBlockEntity is HotpotBlockEntity && levelPos.isServerSide) {
            hotpotBlockEntity.getSoup().entityInside(hotpotBlockEntity, levelPos, entity)
        }
    }

    override fun randomDisplayTick(
        blockState: BlockState,
        level: World,
        pos: BlockPos,
        RandomSourceSource: Random
    ) {
        val levelPos = BlockPosWithLevel(level, pos)
        val hotpotBlockEntity = levelPos.blockEntity
        if (hotpotBlockEntity is HotpotBlockEntity) {
            hotpotBlockEntity.getSoup().animateTick(hotpotBlockEntity, levelPos, RandomSourceSource)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction?,
        nearbyState: BlockState?,
        accessor: WorldAccess,
        pos: BlockPos,
        nearbyPos: BlockPos?
    ): BlockState {
        return updateState(state, pos, accessor)
    }

    override fun <T : BlockEntity> getTicker(
        level: World,
        state: BlockState?,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (level.isClient) null else checkType(
            blockEntityType,
            BlockEntityRegistrar.HOTPOT_BLOCK_ENTITY,
            HotpotBlockEntity::tick
        )
    }

    @Deprecated("Deprecated in Java")
    @SuppressWarnings("deprecation")
    override fun getCollisionShape(
        state: BlockState,
        getter: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        return shapesByIndex[getShapeIndex(state)]
    }

    override fun getPlacementState(context: ItemPlacementContext): BlockState {
        return updateState(super.getPlacementState(context)!!, context.blockPos, context.world)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(
            NORTH,
            SOUTH,
            EAST,
            WEST,
            WEST_NORTH,
            NORTH_EAST,
            EAST_SOUTH,
            SOUTH_WEST,
            SEPARATOR_NORTH,
            SEPARATOR_SOUTH,
            SEPARATOR_EAST,
            SEPARATOR_WEST
        )
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return HotpotBlockEntity(pos, state)
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun getSlotType(): EquipmentSlot {
        return EquipmentSlot.HEAD
    }
}
