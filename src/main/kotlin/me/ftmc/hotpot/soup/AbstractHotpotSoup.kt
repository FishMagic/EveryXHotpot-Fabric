package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.IHotpotContent
import me.ftmc.hotpot.soup.synchronizers.HotpotSoupWaterLevelSynchronizer
import me.ftmc.hotpot.soup.synchronizers.IHotpotSoupSynchronizer
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Hand


abstract class AbstractHotpotSoup : IHotpotSoup {
    private var waterLevel = 1f
    private var overflowWaterLevel = 0f
    override fun load(compoundTag: NbtCompound): IHotpotSoup {
        setWaterLevel(compoundTag.getFloat("WaterLevel"))
        return this
    }

    override fun save(compoundTag: NbtCompound): NbtCompound {
        compoundTag.putFloat("WaterLevel", getWaterLevel())
        return compoundTag
    }

    override fun isValid(compoundTag: NbtCompound): Boolean {
        return compoundTag.contains("WaterLevel", NbtElement.FLOAT_TYPE.toInt())
    }

    override fun interact(
        hitSection: Int,
        player: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        selfPos: BlockPosWithLevel
    ): IHotpotContent? {
        if (itemStack.isEmpty) {
            if (player.isCollidable && hotpotBlockEntity.canBeRemoved()) {
                hotpotBlockEntity.setSoup(HotpotSoups.emptySoup.createSoup(), selfPos)
                hotpotBlockEntity.onRemove(selfPos)
            } else {
                player.damage(player.damageSources.onFire(), 5F)
                hotpotBlockEntity.tryTakeOutContentViaHand(hitSection, selfPos)
            }
            return null
        }
        return remapItemStack(player.abilities.creativeMode, itemStack, selfPos)
    }

    abstract fun remapItemStack(copy: Boolean, itemStack: ItemStack, pos: BlockPosWithLevel): IHotpotContent?
    override fun remapContent(
        content: IHotpotContent,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): IHotpotContent {
        content.placed(hotpotBlockEntity, pos)
        return content
    }

    override fun contentUpdate(content: IHotpotContent, hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        hotpotBlockEntity.getContents()
            .filter { content1 -> content1 !== content }
            .forEach { content1 -> content1.onOtherContentUpdate(content, hotpotBlockEntity, pos) }
    }

    override fun takeOutContentViaChopstick(
        content: IHotpotContent,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ): ItemStack {
        return itemStack
    }

    override fun takeOutContentViaHand(
        content: IHotpotContent,
        itemStack: ItemStack,
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel
    ) {
        pos.dropItemStack(itemStack)
    }

    override fun setWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, waterLevel: Float) {
        setWaterLevel(waterLevel)
    }

    override fun entityInside(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel, entity: Entity) {
        if (entity is ItemEntity) {
            val stack: ItemStack = entity.stack
            if (!stack.isEmpty) {
                remapItemStack(false, stack, pos)?.let { content ->
                    hotpotBlockEntity.tryPlaceContent(
                        HotpotBlockEntity.getPosSection(pos.pos, entity.pos),
                        content,
                        pos
                    )
                }
                entity.stack = stack
            }
            return
        }
        if (entity.isAttackable) {
            entity.damage(DamageSource(EveryXHotpot.IN_HOTPOT_DAMAGE_TYPE(pos.level), pos.toVec3()), 3f)
        }
    }

    override fun getSynchronizer(
        selfHotpotBlockEntity: HotpotBlockEntity,
        selfPos: BlockPosWithLevel
    ): IHotpotSoupSynchronizer? {
        return IHotpotSoupSynchronizer
            .collectOnly { hotpotBlockEntity, pos -> hotpotBlockEntity.getSoup().tick(hotpotBlockEntity, pos) }
            .andThen(HotpotSoupWaterLevelSynchronizer())
            .andThen { hotpotBlockEntity, pos ->
                hotpotBlockEntity.getSoup().discardOverflowWaterLevel(hotpotBlockEntity, pos)
            }
    }

    override fun getWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Float {
        return waterLevel
    }

    override fun getOverflowWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel): Float {
        return overflowWaterLevel
    }

    override fun discardOverflowWaterLevel(hotpotBlockEntity: HotpotBlockEntity, pos: BlockPosWithLevel) {
        overflowWaterLevel = 0f
    }

    fun setWaterLevel(waterLevel: Float) {
        if (waterLevel > 1f) {
            this.waterLevel = 1f
            overflowWaterLevel = waterLevel - 1f
            return
        } else if (waterLevel < 0f) {
            this.waterLevel = 0f
            return
        }
        this.waterLevel = waterLevel
    }

    fun getWaterLevel(): Float {
        return waterLevel
    }
}

