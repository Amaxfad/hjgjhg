package com.multiverselab;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/** Обработчики игровых событий (не шины мода). */
public final class ModEvents {
    private ModEvents() {}

    public static void register() {
        LivingDeathEvent.BUS.addListener(ModEvents::onDeath);
    }

    /**
     * Смерть игрока с готовой капсулой клона отменяется: игрок появляется в капсуле.
     * Возвращаемое значение true означает "отменить событие".
     */
    private static boolean onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            return ClonePodBlock.tryRevive(player);
        }
        return false;
    }
}
