package com.multiverselab.client;

import com.multiverselab.ModContent;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;

/** Клиентская часть: регистрация отрисовки Злого Морти. */
public final class MultiverseLabClient {
    private MultiverseLabClient() {}

    public static void register(BusGroup modBus) {
        EntityRenderersEvent.RegisterRenderers.getBus(modBus).addListener(
                e -> e.registerEntityRenderer(ModContent.EVIL_MORTY.get(), EvilMortyRenderer::new));
    }
}
