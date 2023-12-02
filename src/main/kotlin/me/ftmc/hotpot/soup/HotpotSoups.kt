package me.ftmc.hotpot.soup

import me.ftmc.hotpot.BlockPosWithLevel
import me.ftmc.hotpot.blocks.HotpotBlockEntity
import me.ftmc.hotpot.contents.HotpotCampfireRecipeContent
import me.ftmc.hotpot.contents.HotpotEmptyContent
import me.ftmc.hotpot.contents.IHotpotContent
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


object HotpotSoups {
    val SINOFEAST_LOADED: Boolean =
        FabricLoader.getInstance().allMods.any { it.metadata.id == "getModContainerIterator" }
    val SPICY_ITEM_TAG: TagKey<Item>? = TagKey.of(Registry.ITEM_KEY, Identifier("sinofeast", "tastes/primary/spicy"))
    val ACRID_ITEM_TAG: TagKey<Item>? = TagKey.of(Registry.ITEM_KEY, Identifier("sinofeast", "tastes/primary/acrid"))
    val MILK_ITEM_TAG: TagKey<Item>? = TagKey.of(Registry.ITEM_KEY, Identifier("forge", "milk/milk"))
    val MILK_BOTTLE_ITEM_TAG: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("forge", "milk/milk_bottle"))
    val HOTPOT_SOUP_TYPES: ConcurrentHashMap<String, () -> IHotpotSoup> =
        ConcurrentHashMap(
            mapOf(
                "ClearSoup" to ::HotpotClearSoup,
                "SpicySoup" to ::HotpotSpicySoup,
                "CheeseSoup" to ::HotpotCheeseSoup,
                "LavaSoup" to ::HotpotLavaSoup,
                "Empty" to ::HotpotEmptySoup
            )
        )
    val HOTPOT_SOUP_MATCHES: List<(HotpotBlockEntity) -> SoupProcessor> = listOf(
        {
            SoupProcessor(it, "SpicySoup")
                .enable(SINOFEAST_LOADED)
                .withSoup(HotpotClearSoup::class)
                .withItemTag(SoupProcessor.ItemTagPredicate(SPICY_ITEM_TAG, SoupProcessor.RequireMode(6)))
                .withItemTag(SoupProcessor.ItemTagPredicate(ACRID_ITEM_TAG, SoupProcessor.RequireMode(2)))
        },
        {
            SoupProcessor(it, "SpicySoup")
                .enable(!SINOFEAST_LOADED)
                .withSoup(HotpotClearSoup::class)
                .withItem(SoupProcessor.ItemPredicate(Items.REDSTONE, SoupProcessor.RequireMode(3)))
                .withItem(SoupProcessor.ItemPredicate(Items.BLAZE_POWDER, SoupProcessor.RequireMode(3)))
                .withItem(SoupProcessor.ItemPredicate(Items.GUNPOWDER, SoupProcessor.RequireMode(2)))
        }
    )

    val emptySoup: () -> IHotpotSoup
        get() = HOTPOT_SOUP_TYPES["Empty"]!!

    fun getSoupOrElseEmpty(key: String): () -> IHotpotSoup {
        return HOTPOT_SOUP_TYPES.getOrDefault(key, emptySoup)
    }

    fun ifMatchSoup(
        hotpotBlockEntity: HotpotBlockEntity,
        pos: BlockPosWithLevel,
        consumer: (IHotpotSoup) -> Unit
    ) {
        HOTPOT_SOUP_MATCHES.forEach { processor ->
            processor(hotpotBlockEntity).let {
                if (it.match()) {
                    consumer(it.assemble())
                }
            }
        }
    }

    class SoupProcessor(val hotpotBlockEntity: HotpotBlockEntity, val key: String) {

        interface Predicate
        interface ContentPredicateMode

        class RequireMode(val count: Int) : ContentPredicateMode
        class AtLeastMode(val count: Int) : ContentPredicateMode
        class RangeMode(val min: Int, val max: Int) : ContentPredicateMode

        class SoupPredicate(val soups: List<KClass<out IHotpotSoup>>) : Predicate

        open class ContentPredicate(val mode: ContentPredicateMode) : Predicate

        class ItemPredicate(val item: Item, mode: ContentPredicateMode) : ContentPredicate(mode)
        class ItemTagPredicate(val itemTag: TagKey<Item>?, mode: ContentPredicateMode) : ContentPredicate(mode)

        private var enable = true
        private var soupMatched = false
        private var contentMatched = true
        private val queuedReplaces = mutableMapOf<ContentPredicate, (IHotpotContent) -> IHotpotContent>()

        fun enable(predicate: Boolean): SoupProcessor {
            enable = predicate
            return this
        }

        fun withSoup(predicate: SoupPredicate): SoupProcessor {
            predicate.soups.forEach(::withSoup)
            return this
        }

        fun withSoup(predicate: List<KClass<out IHotpotSoup>>): SoupProcessor {
            predicate.forEach(::withSoup)
            return this
        }

        fun withSoup(predicate: KClass<out IHotpotSoup>): SoupProcessor {
            if (enable) {
                soupMatched = soupMatched || predicate.isInstance(hotpotBlockEntity.getSoup())
            }
            return this
        }

        private fun countCheck(predicate: ContentPredicate, count: Int): Boolean {
            val countMatched = when (predicate.mode) {
                is RequireMode -> count == predicate.mode.count
                is AtLeastMode -> count > predicate.mode.count
                is RangeMode -> count > predicate.mode.min && count < predicate.mode.max
                else -> false
            }
            contentMatched = contentMatched && countMatched
            return countMatched
        }

        fun withItem(predicate: ItemPredicate): SoupProcessor {
            if (enable) {
                val count = hotpotBlockEntity.getContents().count {
                    it is HotpotCampfireRecipeContent &&
                            it.getItemStack()?.isOf(predicate.item) ?: false
                }
                if (countCheck(predicate, count)) queuedReplaces[predicate] = {
                    if (it is HotpotCampfireRecipeContent && it.getItemStack()
                            ?.isOf(predicate.item) == true
                    ) HotpotEmptyContent() else it
                }
            }
            return this
        }

        fun withItemTag(predicate: ItemTagPredicate): SoupProcessor {
            if (enable) {
                val count = hotpotBlockEntity.getContents().count {
                    it is HotpotCampfireRecipeContent &&
                            it.getItemStack()?.isIn(predicate.itemTag) ?: false
                }
                if (countCheck(predicate, count)) queuedReplaces[predicate] = {
                    if (it is HotpotCampfireRecipeContent && it.getItemStack()
                            ?.isIn(predicate.itemTag) == true
                    ) HotpotEmptyContent() else it
                }
            }
            return this
        }

        fun match(): Boolean {
            return soupMatched && contentMatched
        }

        fun assemble(): IHotpotSoup {
            if (soupMatched && contentMatched) {
                queuedReplaces.forEach { (_, v) -> hotpotBlockEntity.getContents().replaceAll { v(it) } }
                return getSoupOrElseEmpty(key)()
            }
            return emptySoup()
        }
    }
}
