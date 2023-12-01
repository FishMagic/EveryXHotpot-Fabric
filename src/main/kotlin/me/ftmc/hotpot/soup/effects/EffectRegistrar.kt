package me.ftmc.hotpot.soup.effects

import me.ftmc.hotpot.MOD_ID
import me.ftmc.hotpot.logger
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object EffectRegistrar {

    val HOTPOT_WARM: StatusEffect = Registry.register(
        Registries.STATUS_EFFECT,
        Identifier(MOD_ID, "warm"),
        HotpotMobEffect(StatusEffectCategory.BENEFICIAL, 240 shl 16 or (240 shl 8) or 240)
    )

    val HOTPOT_ACRID: StatusEffect = Registry.register(
        Registries.STATUS_EFFECT,
        Identifier(MOD_ID, "acrid"),
        HotpotMobEffect(StatusEffectCategory.BENEFICIAL, 240 shl 16 or (84 shl 8) or 64)
            .addAttributeModifier(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                "46f33e49-ce96-4c75-b126-60a1e4117a8f",
                0.5,
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL
            )
    )

    fun register() {
        logger.debug("Effects Registered")
    }
}