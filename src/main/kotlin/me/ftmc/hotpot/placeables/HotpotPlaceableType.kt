package me.ftmc.hotpot.placeables

interface HotpotPlaceableType<out T : IHotpotPlaceable> {
    fun createPlaceable(): T
}