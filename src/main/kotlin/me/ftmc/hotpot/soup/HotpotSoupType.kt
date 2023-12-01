package me.ftmc.hotpot.soup


fun interface HotpotSoupType<out T : IHotpotSoup> {
    fun createSoup(): T
}
