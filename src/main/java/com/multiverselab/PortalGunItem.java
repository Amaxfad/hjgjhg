package com.multiverselab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * Портальная пушка.
 *  ПКМ                          - выстрелить порталом в 2 блоках перед собой (1 топливо)
 *  Shift + ПКМ                  - выбрать следующее измерение
 *  ПКМ с флаконом в другой руке - заправка (+4 топлива, максимум 16)
 */
public class PortalGunItem extends Item {
    public static final int MAX_FUEL = 16;
    public static final int FUEL_PER_VIAL = 4;

    public PortalGunItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack gun = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel here = (ServerLevel) level;
        int count = PortalTravel.NAMES.size();
        int idx = getInt(gun, "target") % count;
        int fuel = getInt(gun, "fuel");

        if (player.isShiftKeyDown()) {
            idx = (idx + 1) % count;
            setInt(gun, "target", idx);
            player.displayClientMessage(Component.translatable("multiverselab.msg.target",
                    Component.translatable("multiverselab.dim." + PortalTravel.NAMES.get(idx))), true);
            return InteractionResult.SUCCESS;
        }

        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack vial = player.getItemInHand(other);
        if (vial.is(ModContent.PORTAL_VIAL.get())) {
            if (fuel + FUEL_PER_VIAL > MAX_FUEL) {
                player.displayClientMessage(Component.translatable("multiverselab.msg.full"), true);
                return InteractionResult.FAIL;
            }
            vial.consume(1, player);
            fuel += FUEL_PER_VIAL;
            setInt(gun, "fuel", fuel);
            player.displayClientMessage(Component.translatable("multiverselab.msg.fuel", fuel, MAX_FUEL), true);
            return InteractionResult.SUCCESS;
        }

        if (fuel <= 0) {
            player.displayClientMessage(Component.translatable("multiverselab.msg.nofuel"), true);
            return InteractionResult.FAIL;
        }
        if (PortalTravel.indexOf(here) == idx) {
            player.displayClientMessage(Component.translatable("multiverselab.msg.here"), true);
            return InteractionResult.FAIL;
        }
        if (here.getServer().getLevel(PortalTravel.keyOf(idx)) == null) {
            player.displayClientMessage(Component.translatable("multiverselab.msg.missing"), true);
            return InteractionResult.FAIL;
        }

        Direction dir = player.getDirection();
        BlockPos spot = player.blockPosition().relative(dir, 2);
        if (!PortalTravel.placePortal(here, spot, dir, idx)) {
            player.displayClientMessage(Component.translatable("multiverselab.msg.blocked"), true);
            return InteractionResult.FAIL;
        }

        here.playSound(null, spot, ModContent.PORTAL_SHOOT.get(), SoundSource.PLAYERS, 1f, 1f);
        setInt(gun, "fuel", fuel - 1);
        player.getCooldowns().addCooldown(gun, 20);
        return InteractionResult.SUCCESS;
    }

    // ---------------------------------------------------------------- данные в предмете

    public static int getInt(ItemStack s, String k) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? 0 : d.copyTag().getIntOr(k, 0);
    }

    public static void setInt(ItemStack s, String k, int v) {
        CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putInt(k, v));
    }
}
