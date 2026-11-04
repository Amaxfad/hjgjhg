package com.multiverselab;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Протезы как у Рика: устанавливаются навсегда (хранятся у игрока, переживают смерть). */
public final class Implants {
    private Implants() {}

    public static final int ARM = 1, LEG = 2, EYE = 4;

    public static final AttachmentType<Integer> MASK = AttachmentRegistry.create(ModContent.id("implants"),
            b -> b.persistent(Codec.INT).copyOnDeath().initializer(() -> 0));

    /** Раз в 5 секунд обновляет эффекты у игроков с протезами. */
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 100 != 0) {
            return;
        }
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            int mask = p.getAttachedOrElse(MASK, 0);
            if (mask == 0) {
                continue;
            }
            if ((mask & ARM) != 0) {
                p.addEffect(new MobEffectInstance(MobEffects.HASTE, 400, 1, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 400, 0, false, false));
            }
            if ((mask & LEG) != 0) {
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 400, 1, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 400, 1, false, false));
            }
            if ((mask & EYE) != 0) {
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
            }
        }
    }

    /** Предмет-протез: ПКМ устанавливает его в игрока. */
    public static class ImplantItem extends Item {
        private final int bit;

        public ImplantItem(Properties props, int bit) {
            super(props);
            this.bit = bit;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            ItemStack stack = player.getItemInHand(hand);
            int mask = player.getAttachedOrElse(MASK, 0);
            if ((mask & bit) != 0) {
                player.displayClientMessage(Component.translatable("multiverselab.msg.has_implant"), true);
                return InteractionResult.FAIL;
            }
            player.setAttached(MASK, mask | bit);
            stack.consume(1, player);
            level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8f, 1.4f);
            player.displayClientMessage(Component.translatable("multiverselab.msg.implanted"), true);
            return InteractionResult.SUCCESS;
        }
    }
}
