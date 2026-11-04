package com.multiverselab;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.server.level.ServerPlayer;

public final class MultiverseLab implements ModInitializer {
    public static final String MODID = "multiverselab";

    @Override
    public void onInitialize() {
        ModContent.init();
        FabricDefaultAttributeRegistry.register(ModContent.EVIL_MORTY, EvilMortyEntity.createAttributes());
        ServerTickEvents.END_SERVER_TICK.register(Implants::tick);
        ServerLifecycleEvents.SERVER_STARTED.register(RickHouseMap::install);
        // Смерть игрока с готовой капсулой клона отменяется: игрок появляется в капсуле.
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) ->
                !(entity instanceof ServerPlayer player && ClonePodBlock.tryRevive(player)));
    }
}
