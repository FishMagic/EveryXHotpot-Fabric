package me.ftmc.hotpot.blocks

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.contents.HotpotContents
import me.ftmc.hotpot.contents.HotpotEmptyContent
import me.ftmc.hotpot.contents.IHotpotContent
import me.ftmc.hotpot.soup.HotpotSoups
import me.ftmc.hotpot.soup.IHotpotSoup
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.util.Hand
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.joml.Math


class HotpotBlockEntity(pos: BlockPos, state: BlockState) :
    AbstractChopstickInteractiveBlockEntity(BlockEntityRegistrar.HOTPOT_BLOCK_ENTITY, pos, state) {
    private var contentChanged = true
    private var soupSynchronized = false
    private val contents: MutableList<IHotpotContent> =
        DefaultedList.ofSize(8, HotpotContents.emptyContent.createContent())
    private var soup: IHotpotSoup = HotpotSoups.emptySoup()
    var renderedWaterLevel = -1f
    var waterLevel = 0f
        private set
    var time = 0
        private set
    var isInfiniteWater = false
    private var canBeRemoved = true
    private fun tryFindEmptyContent(
        hitSection: Int, selfPos: BlockPosWithLevel, consumer: (Int, HotpotBlockEntity, BlockPosWithLevel) -> Unit
    ) {
        BlockEntityFinder(
            selfPos, HotpotBlockEntity::class.java
        ) { _, pos -> isSameSoup(selfPos, pos) }.getFirst(
            10,
            { hotpotBlockEntity, _ -> hotpotBlockEntity.hasEmptyContent() }) { hotpotBlockEntity, pos ->
            listOf(
                getContentSection(hitSection), 0, 1, 2, 3, 4, 5, 6, 7
            ).firstOrNull(hotpotBlockEntity::isEmptyContent)?.also {
                consumer(it, hotpotBlockEntity, pos)
            }

        }
    }

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
        soup.interact(hitSection, player, hand, itemStack, this, selfPos)?.let { content ->
            tryFindEmptyContent(
                hitSection,
                selfPos
            ) { section: Int, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel ->
                hotpotBlockEntity.placeContent(
                    section, content, pos
                )
            }
        }
    }

    fun tryPlaceContent(hitSection: Int, content: IHotpotContent, selfPos: BlockPosWithLevel) {
        tryFindEmptyContent(
            hitSection,
            selfPos
        ) { section, hotpotBlockEntity, pos -> hotpotBlockEntity.placeContent(section, content, pos) }
    }

    private fun placeContent(section: Int, content: IHotpotContent, pos: BlockPosWithLevel) {
        val remappedContent: IHotpotContent? = soup.remapContent(content, this, pos)
        contents[section] = remappedContent ?: HotpotContents.emptyContent.createContent()
        HotpotSoups.ifMatchSoup(this, pos) { setSoup(it, pos) }
        markDataChanged()
    }

    fun consumeContent(operator: (IHotpotContent) -> IHotpotContent) {
        contents.replaceAll(operator)
        markDataChanged()
    }

    fun consumeAllContents() {
        consumeContent { HotpotContents.emptyContent.createContent() }
    }

    private fun synchronizeSoup(selfPos: BlockPosWithLevel) {
        if (soupSynchronized) {
            return
        }
        soup.getSynchronizer(this, selfPos)?.let { synchronizer ->
            val neighbors: Map<HotpotBlockEntity, BlockPosWithLevel> = BlockEntityFinder(
                selfPos, HotpotBlockEntity::class.java
            ) { hotpotBlockEntity, pos ->
                isSameSoup(
                    selfPos, pos
                ) && !hotpotBlockEntity.soupSynchronized
            }.all
            neighbors.forEach { (hotpotBlockEntity, pos) ->
                hotpotBlockEntity.soupSynchronized = true
                synchronizer.collect(hotpotBlockEntity, pos)
            }
            neighbors.forEach { (hotpotBlockEntity, pos) ->
                synchronizer.integrate(neighbors.size, hotpotBlockEntity, pos)
            }
        }
    }

    fun tryTakeOutContentViaHand(hitSection: Int, pos: BlockPosWithLevel) {
        val contentSection = getContentSection(hitSection)
        val content: IHotpotContent = contents[contentSection]
        if (content !is HotpotEmptyContent) {
            soup.takeOutContentViaHand(
                content, soup.takeOutContentViaChopstick(content, content.takeOut(this, pos), this, pos), this, pos
            )
            contents[contentSection] = HotpotContents.emptyContent.createContent()
            markDataChanged()
        }
    }

    override fun tryTakeOutContentViaChopstick(hitSection: Int, pos: BlockPosWithLevel): ItemStack {
        val contentSection = getContentSection(hitSection)
        val content: IHotpotContent = contents[contentSection]
        if (content !is HotpotEmptyContent) {
            val itemStack: ItemStack = soup.takeOutContentViaChopstick(content, content.takeOut(this, pos), this, pos)
            contents[contentSection] = HotpotContents.emptyContent.createContent()
            markDataChanged()
            return itemStack
        }
        return ItemStack.EMPTY
    }

    fun getContentSection(hitSection: Int): Int {
        val sectionSize = (360f / 8f).toDouble()
        val degree = time / 20f / 60f * 360f + sectionSize / 2f
        val rootSection = Math.floor(degree % 360f / sectionSize).toInt()
        val contentSection = hitSection - rootSection
        return if (contentSection < 0) 8 + contentSection else contentSection
    }

    fun onRemove(pos: BlockPosWithLevel) {
        for (i in 0 until contents.size) {
            val content: IHotpotContent = contents[i]
            soup.takeOutContentViaHand(content, content.takeOut(this, pos), this, pos)
            contents[i] = HotpotContents.emptyContent.createContent()
        }
        markDataChanged()
    }

    fun setSoup(soup: IHotpotSoup, pos: BlockPosWithLevel) {
        this.soup = soup
        markDataChanged()
        pos.markAndNotifyBlock()
    }

    override fun readNbt(compoundTag: NbtCompound) {
        super.readNbt(compoundTag)
        canBeRemoved =
            !compoundTag.contains(
                "CanBeRemoved",
                NbtElement.NUMBER_TYPE.toInt()
            ) || compoundTag.getBoolean("CanBeRemoved")
        isInfiniteWater =
            compoundTag.contains(
                "InfiniteWater",
                NbtElement.NUMBER_TYPE.toInt()
            ) && compoundTag.getBoolean("InfiniteWater")
        time = if (compoundTag.contains("Time", NbtElement.NUMBER_TYPE.toInt())) compoundTag.getInt("Time") else 0
        waterLevel = if (compoundTag.contains(
                "WaterLevel",
                NbtElement.FLOAT_TYPE.toInt()
            )
        ) compoundTag.getFloat("WaterLevel") else 0f
        if (compoundTag.contains("Soup", NbtElement.COMPOUND_TYPE.toInt())) {
            soup = IHotpotSoup.loadSoup(compoundTag.getCompound("Soup"))
        }
        if (compoundTag.contains("Contents", NbtElement.LIST_TYPE.toInt())) {
            contents.clear()
            IHotpotContent.loadAll(compoundTag.getList("Contents", NbtElement.COMPOUND_TYPE.toInt()), contents)
        }
    }

    override fun writeNbt(compoundTag: NbtCompound) {
        super.writeNbt(compoundTag)
        compoundTag.putBoolean("CanBeRemoved", canBeRemoved)
        compoundTag.putBoolean("InfiniteWater", isInfiniteWater)
        compoundTag.putInt("Time", time)
        compoundTag.putFloat("WaterLevel", waterLevel)
        compoundTag.put("Soup", IHotpotSoup.save(soup))
        compoundTag.put("Contents", IHotpotContent.saveAll(contents))
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener> {
        return BlockEntityUpdateS2CPacket.create(this) {
            val compoundTag = NbtCompound()
            compoundTag.putBoolean("CanBeRemoved", canBeRemoved)
            compoundTag.putBoolean("InfiniteWater", isInfiniteWater)
            compoundTag.putInt("Time", time)
            compoundTag.putFloat("WaterLevel", waterLevel)
            if (contentChanged) {
                compoundTag.put("Soup", IHotpotSoup.save(soup))
                compoundTag.put("Contents", IHotpotContent.saveAll(contents))
                contentChanged = false
            }
            compoundTag
        }
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val compoundTag: NbtCompound = super.toInitialChunkDataNbt()
        compoundTag.putBoolean("CanBeRemoved", canBeRemoved)
        compoundTag.putBoolean("InfiniteWater", isInfiniteWater)
        compoundTag.putInt("Time", time)
        compoundTag.putFloat("WaterLevel", waterLevel)
        compoundTag.put("Soup", IHotpotSoup.save(soup))
        compoundTag.put("Contents", IHotpotContent.saveAll(contents))
        return compoundTag
    }

    fun markDataChanged() {
        contentChanged = true
        markDirty()
    }

    fun getContents(): MutableList<IHotpotContent> {
        return contents
    }

    fun hasEmptyContent(): Boolean {
        return contents.any { content -> content is HotpotEmptyContent }
    }

    fun isEmptyContent(section: Int): Boolean {
        return getContent(section) is HotpotEmptyContent
    }

    fun getContent(section: Int): IHotpotContent {
        return contents[section]
    }

    fun getSoup(): IHotpotSoup {
        return soup
    }

    fun canBeRemoved(): Boolean {
        return canBeRemoved
    }

    fun setCanBeRemoved(canBeRemoved: Boolean) {
        this.canBeRemoved = canBeRemoved
    }

    companion object {
        fun tick(level: World, pos: BlockPos, state: BlockState, blockEntity: HotpotBlockEntity) {
            val selfPos = BlockPosWithLevel(level, pos)
            blockEntity.time++
            blockEntity.synchronizeSoup(selfPos)
            blockEntity.soupSynchronized = false
            blockEntity.waterLevel = blockEntity.soup.getWaterLevel(blockEntity, selfPos)
            val tickSpeed: Int = blockEntity.soup.getContentTickSpeed(blockEntity, selfPos)
            if (tickSpeed < 0) {
                if (blockEntity.time % -tickSpeed == 0) {
                    tickContent(blockEntity, selfPos)
                }
            } else {
                var i = 0
                do {
                    tickContent(blockEntity, selfPos)
                } while (++i < tickSpeed)
            }
            level.updateListeners(pos, state, state, 3)
            blockEntity.markDirty()
        }

        fun tickContent(blockEntity: HotpotBlockEntity, selfPos: BlockPosWithLevel) {
            for (content in blockEntity.contents) {
                if (content.tick(blockEntity, selfPos)) {
                    blockEntity.soup.contentUpdate(content, blockEntity, selfPos)
                    blockEntity.markDataChanged()
                }
            }
        }

        fun isSameSoup(selfPos: BlockPosWithLevel, pos: BlockPosWithLevel): Boolean {
            val selfBlockEntity = selfPos.blockEntity
            val hotpotBlockEntity = pos.blockEntity
            return if (selfBlockEntity is HotpotBlockEntity && hotpotBlockEntity is HotpotBlockEntity) {
                selfBlockEntity.getSoup().id.equals(hotpotBlockEntity.getSoup().id)
            } else false
        }

        fun getPosSection(blockPos: BlockPos, pos: Vec3d): Int {
            val vec = pos.subtract(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble())
            val x: Double = vec.x - 0.5f
            val z: Double = vec.z - 0.5f
            val sectionSize = (360f / 8f).toDouble()
            var degree = Math.atan2(x, z) / Math.PI * 180f + sectionSize / 2f
            degree = if (degree < 0f) degree + 360f else degree
            return Math.floor(degree / sectionSize).toInt()
        }
    }
}
