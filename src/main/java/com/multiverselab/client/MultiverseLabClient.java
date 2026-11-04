package com.multiverselab.client;

import com.multiverselab.ModContent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class MultiverseLabClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModContent.EVIL_MORTY, EvilMortyRenderer::new);
    }
}
