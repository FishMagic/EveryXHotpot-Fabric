package me.ftmc.hotpot.contents

interface HotpotContentType<out T : IHotpotContent> {
    fun createContent(): T
}