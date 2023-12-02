package me.ftmc.hotpot.blocks

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.placeables.HotpotEmptyPlaceable
import me.ftmc.hotpot.placeables.HotpotPlaceables
import me.ftmc.hotpot.placeables.IHotpotPlaceable
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.util.Hand
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World


class HotpotPlaceableBlockEntity(pos: BlockPos, state: BlockState) :
    AbstractChopstickInteractiveBlockEntity(EveryXHotpot.HOTPOT_PLACEABLE_BLOCK_ENTITY, pos, state) {
    private var contentChanged = true
    private val placeables: DefaultedList<IHotpotPlaceable> =
        DefaultedList.ofSize(4, HotpotPlaceables.emptyPlaceable())
    var isInfiniteContent = false
        private set
    private var canBeRemoved = true
    override fun tryPlaceContentViaChopstick(
        hitSection: Int,
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        selfPos: BlockPosWithLevel
    ): ItemStack {
        tryPlaceContentViaInteraction(hitSection, player, hand, itemStack, selfPos)
        return itemStack
    }

    override fun tryPlaceContentViaInteraction(
        hitSection: Int,
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        selfPos: BlockPosWithLevel
    ) {
        if (isEmpty) {
            selfPos.level.removeBlock(selfPos.pos, true)
        }
        val placeable: IHotpotPlaceable = getPlaceableInPos(hitSection)
        placeable.interact(player, hand, itemStack, hitSection, this, selfPos)
        markDataChanged()
    }

    override fun tryTakeOutContentViaChopstick(hitSection: Int, pos: BlockPosWithLevel): ItemStack {
        if (isEmpty) {
            pos.level.removeBlock(pos.pos, true)
        }
        val placeable: IHotpotPlaceable = getPlaceableInPos(hitSection)
        val itemStack: ItemStack = placeable.takeOutContent(hitSection, this, pos)
        markDataChanged()
        return itemStack
    }

    fun tryTakeOutContentViaHand(hitSection: Int, pos: BlockPosWithLevel) {
        pos.dropItemStack(tryTakeOutContentViaChopstick(hitSection, pos))
    }

    fun tryRemove(placeable: IHotpotPlaceable, pos: BlockPosWithLevel) {
        if (!canBeRemoved()) {
            return
        }
        tryRemove(placeable.anchorPos, pos)
    }

    fun tryRemove(hitSection: Int, pos: BlockPosWithLevel) {
        val placeable: IHotpotPlaceable = getPlaceableInPos(hitSection)
        if (placeable !is HotpotEmptyPlaceable) {
            placeable.onRemove(this, pos)
            pos.dropItemStack(placeable.getCloneItemStack(this, pos))
            placeables[placeable.anchorPos] = HotpotPlaceables.emptyPlaceable()
            markDataChanged()
        }
        if (isEmpty) {
            pos.level.removeBlock(pos.pos, true)
        }
    }

    val isEmpty: Boolean
        get() = placeables.stream().allMatch { placeable -> placeable is HotpotEmptyPlaceable }

    fun tryPlace(placeable: IHotpotPlaceable): Boolean {
        if (canPlaceableFit(placeable)) {
            val toReplace: IHotpotPlaceable = placeables[placeable.anchorPos]
            if (toReplace is HotpotEmptyPlaceable) {
                placeables[placeable.anchorPos] = placeable
                markDataChanged()
                return true
            }
        }
        return false
    }

    fun onRemove(pos: BlockPosWithLevel) {
        for (i in 0 until placeables.size) {
            val placeable: IHotpotPlaceable = placeables[i]
            placeable.onRemove(this, pos)
            pos.dropItemStack(placeable.getCloneItemStack(this, pos))
            placeables[i] = HotpotPlaceables.emptyPlaceable()
        }
        markDataChanged()
    }

    fun getPlaceableInPos(hitPos: Int): IHotpotPlaceable {
        return placeables.firstOrNull { it.getPos().contains(hitPos) } ?: HotpotPlaceables.emptyPlaceable()
    }

    fun markDataChanged() {
        contentChanged = true
        super.markDirty()
    }

    //Load
    override fun readNbt(compoundTag: NbtCompound) {
        super.readNbt(compoundTag)
        canBeRemoved =
            !compoundTag.contains(
                "CanBeRemoved",
                NbtElement.NUMBER_TYPE.toInt()
            ) || compoundTag.getBoolean("CanBeRemoved")
        isInfiniteContent =
            compoundTag.contains(
                "InfiniteContent",
                NbtElement.NUMBER_TYPE.toInt()
            ) && compoundTag.getBoolean("InfiniteContent")
        if (compoundTag.contains("Placeables", NbtElement.LIST_TYPE.toInt())) {
            placeables.clear()
            IHotpotPlaceable.loadAll(compoundTag.getList("Placeables", NbtElement.COMPOUND_TYPE.toInt()), placeables)
        }
    }

    //Save
    override fun writeNbt(compoundTag: NbtCompound) {
        super.writeNbt(compoundTag)
        compoundTag.putBoolean("CanBeRemoved", canBeRemoved)
        compoundTag.putBoolean("InfiniteContent", isInfiniteContent)
        compoundTag.put("Placeables", IHotpotPlaceable.saveAll(placeables))
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener> {
        return BlockEntityUpdateS2CPacket.create(this) {
            val compoundTag = NbtCompound()
            compoundTag.putBoolean("CanBeRemoved", canBeRemoved)
            compoundTag.putBoolean("InfiniteContent", isInfiniteContent)
            if (contentChanged) {
                compoundTag.put("Placeables", IHotpotPlaceable.saveAll(placeables))
                contentChanged = false
            }
            compoundTag
        }
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val compoundTag = super.toInitialChunkDataNbt()
        compoundTag.putBoolean("CanBeRemoved", canBeRemoved)
        compoundTag.putBoolean("InfiniteContent", isInfiniteContent)
        compoundTag.put("Placeables", IHotpotPlaceable.saveAll(placeables))
        return compoundTag
    }

    fun getPlaceables(): List<IHotpotPlaceable> {
        return placeables
    }

    fun canPlaceableFit(plate2: IHotpotPlaceable): Boolean {
        return placeables.stream().noneMatch { plate -> plate2.getPos().stream().anyMatch(plate::isConflict) }
    }

    fun canBeRemoved(): Boolean {
        return canBeRemoved
    }

    companion object {
        fun tick(level: World, pos: BlockPos, state: BlockState, blockEntity: HotpotPlaceableBlockEntity) {
            if (blockEntity.contentChanged) {
                level.updateListeners(pos, state, state, 3)
            }
        }

        fun getHitPos(pos: BlockPos, location: Vec3d): Int {
            val blockPos: BlockPos = pos.offset(Direction.UP)
            val vec = location.subtract(
                blockPos.x.toDouble(),
                blockPos.y.toDouble(),
                blockPos.z.toDouble()
            )
            return (if (vec.z < 0.5) 0 else 1) or if (vec.x < 0.5) 0 else 2
        }

        fun getHitPos(result: BlockHitResult): Int {
            return getHitPos(result.blockPos, result.pos)
        }

        fun getHitPos(context: ItemPlacementContext): Int {
            return getHitPos(context.blockPos, context.hitPos)
        }

        fun getHitPos(context: ItemUsageContext): Int {
            return getHitPos(context.blockPos, context.hitPos)
        }
    }
}
