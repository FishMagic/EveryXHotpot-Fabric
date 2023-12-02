package me.ftmc.hotpot.items

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.EveryXHotpot
import me.ftmc.hotpot.blocks.HotpotPlaceableBlockEntity
import me.ftmc.hotpot.placeables.IHotpotPlaceable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction


open class HotpotPlaceableBlockItem(
    private val supplier: () -> IHotpotPlaceable,
    properties: Settings = Settings().maxCount(64).group(EveryXHotpot.EVERY_X_HOTPOT_TAB)
) : BlockItem(
    EveryXHotpot.HOTPOT_PLACEABLE,
    properties
) {
    open fun shouldPlace(player: PlayerEntity?, hand: Hand, pos: BlockPosWithLevel): Boolean {
        return true
    }

    open fun setAdditional(
        hotpotPlaceableBlockEntity: HotpotPlaceableBlockEntity,
        pos: BlockPosWithLevel,
        placeable: IHotpotPlaceable,
        itemStack: ItemStack
    ) {
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val selfPos: BlockPosWithLevel = BlockPosWithLevel.fromUseOnContext(context)
        val direction: Direction = context.playerFacing
        val pos: Int = HotpotPlaceableBlockEntity.getHitPos(context)
        if (!shouldPlace(context.player, context.hand, selfPos)) {
            return ActionResult.PASS
        }
        val placeable: IHotpotPlaceable = supplier()
        val player = context.player
        if (selfPos.isOf(EveryXHotpot.HOTPOT_PLACEABLE)
            && placeable.tryPlace(pos, direction)
            && tryPlace(selfPos, placeable, context.stack.copy())
        ) {
            playSound(selfPos, context.player)
            if (player == null || !player.abilities.creativeMode) {
                context.stack.decrement(1)
                return ActionResult.success(!selfPos.isServerSide)
            }
            return ActionResult.FAIL
        }
        return super.useOnBlock(context)
    }

    override fun appendStacks(group: ItemGroup, stacks: DefaultedList<ItemStack?>) {
        if (isIn(group)) {
            stacks.add(ItemStack(this))
        }
    }

    override fun place(context: ItemPlacementContext): ActionResult {
        val selfPos: BlockPosWithLevel = BlockPosWithLevel.fromBlockPlaceContext(context)
        val direction: Direction = context.playerLookDirection
        val pos: Int = HotpotPlaceableBlockEntity.getHitPos(context)
        val itemStack: ItemStack = context.stack.copy()
        val placeable: IHotpotPlaceable = supplier()
        if (placeable.tryPlace(pos, direction)) {
            val result = super.place(context)
            tryPlace(selfPos, placeable, itemStack)
            return result
        }
        return ActionResult.FAIL
    }

    fun tryPlace(selfPos: BlockPosWithLevel, placeable: IHotpotPlaceable, itemStack: ItemStack): Boolean {
        val hotpotPlateBlockEntity = selfPos.blockEntity
        if (selfPos.isServerSide && hotpotPlateBlockEntity is HotpotPlaceableBlockEntity) {
            setAdditional(hotpotPlateBlockEntity, selfPos, placeable, itemStack)
            return hotpotPlateBlockEntity.tryPlace(placeable)
        }
        return false
    }

    fun playSound(pos: BlockPosWithLevel, player: PlayerEntity?) {
        val soundtype = pos.getSoundType(player)
        val soundEvent = this.getPlaceSound(pos.blockState)
        val volume: Float = (soundtype.getVolume() + 1.0f) / 2.0f
        val pitch: Float = soundtype.getPitch() * 0.8f
        pos.level.playSound(player, pos.pos, soundEvent, SoundCategory.BLOCKS, volume, pitch)
    }

    override fun getTranslationKey(): String {
        return super.getOrCreateTranslationKey()
    }
}
