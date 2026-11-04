package com.multiverselab;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MultiverseLab.MODID)
public final class MultiverseLab {
    public static final String MODID = "multiverselab";

    public MultiverseLab(FMLJavaModLoadingContext context) {
        var modBus = context.getModBusGroup();
        ModContent.register(modBus);
        EntityAttributeCreationEvent.getBus(modBus).addListener(
                e -> e.put(ModContent.EVIL_MORTY.get(), EvilMortyEntity.createAttributes().build()));

        // смерть игрока с готовой капсулой клона отменяется (true = отменить)
        LivingDeathEvent.BUS.addListener(e ->
                e.getEntity() instanceof ServerPlayer player && ClonePodBlock.tryRevive(player));
        ServerStartedEvent.BUS.addListener(e -> RickHouseMap.install(e.getServer()));
        PlayerEvent.Clone.BUS.addListener(e -> Implants.copyOnClone(e.getOriginal(), e.getEntity()));
        Implants.registerTick();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.multiverselab.client.MultiverseLabClient.register(modBus);
        }
    }
}
