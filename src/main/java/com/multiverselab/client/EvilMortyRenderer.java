package com.multiverselab.client;

import com.multiverselab.EvilMortyEntity;
import com.multiverselab.ModContent;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** Отрисовка Злого Морти моделью игрока и его скином. */
public class EvilMortyRenderer
        extends HumanoidMobRenderer<EvilMortyEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    private static final Identifier TEXTURE = ModContent.id("textures/entity/evil_morty.png");

    public EvilMortyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return TEXTURE;
    }
}
