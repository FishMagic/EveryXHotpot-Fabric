package me.ftmc.hotpot.placeables

import net.minecraft.util.math.Direction


object HotpotPlaceables {
    val POS_TO_DIRECTION: Map<Int, Direction> = mapOf(
        -1 to Direction.NORTH,
        +1 to Direction.SOUTH,
        +2 to Direction.EAST,
        -2 to Direction.WEST
    )
    val DIRECTION_TO_POS: Map<Direction, Int> = mapOf(
        Direction.NORTH to -1,
        Direction.SOUTH to 1,
        Direction.EAST to 2,
        Direction.WEST to -2
    )
    val emptyPlaceable: HotpotPlaceableType<HotpotEmptyPlaceable>
        get() = PlaceableRegistrar.EMPTY_PLACEABLE
}
