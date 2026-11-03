package com.multiverselab;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public final class MultiverseLab implements ModInitializer {
    public static final String MODID = "multiverselab";

    @Override
    public void onInitialize() {
        ModContent.init();
        // Смерть игрока с готовой капсулой клона отменяется: игрок появляется в капсуле.
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) ->
                !(entity instanceof ServerPlayer player && ClonePodBlock.tryRevive(player)));
    }
}
