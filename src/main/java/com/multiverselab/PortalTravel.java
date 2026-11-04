package com.multiverselab;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Общая логика перемещения между измерениями и размещения порталов. */
public final class PortalTravel {
    private PortalTravel() {}

    /** Пути измерений. Первое - обычный мир, остальные описаны в data/multiverselab/dimension/. */
    public static final List<String> NAMES =
            List.of("overworld", "cronenberg", "gazorpazorp", "citadel", "snowball", "rick_house");

    /** Индекс измерения "Дом Рика" в списке NAMES. */
    public static final int RICK_HOUSE = 5;

    public static ResourceKey<Level> keyOf(int index) {
        String name = NAMES.get(Math.floorMod(index, NAMES.size()));
        if (name.equals("overworld")) {
            return Level.OVERWORLD;
        }
        return ResourceKey.create(Registries.DIMENSION, ModContent.id(name));
    }

    /** Индекс текущего измерения в списке (0, если измерение не из нашего списка). */
    public static int indexOf(Level level) {
        for (int i = 0; i < NAMES.size(); i++) {
            if (level.dimension() == keyOf(i)) {
                return i;
            }
        }
        return 0;
    }

    /** Ставит портал на {@code pos}, если там и выше свободно. Возвращает true при успехе. */
    public static boolean placePortal(ServerLevel level, BlockPos pos, Direction facing, int destIndex, boolean gold) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        BlockState state = ModContent.PORTAL_FIELD.get().defaultBlockState()
                .setValue(PortalFieldBlock.AXIS, facing.getAxis())
                .setValue(PortalFieldBlock.DEST, destIndex)
                .setValue(PortalFieldBlock.AGE, 0)
                .setValue(PortalFieldBlock.GOLD, gold);
        level.setBlock(pos, state, 3);
        level.scheduleTick(pos, ModContent.PORTAL_FIELD.get(), 5);
        return true;
    }

    /** Переносит игрока в измерение {@code destIndex} и ставит там обратный портал. */
    public static void travel(ServerPlayer player, int destIndex, boolean gold) {
        ServerLevel here = (ServerLevel) player.level();
        ServerLevel dest = here.getServer().getLevel(keyOf(destIndex));
        if (dest == null || dest == here) {
            return;
        }
        int originIndex = indexOf(here);
        Direction facing = player.getDirection();

        BlockPos target;
        if (destIndex == RICK_HOUSE) {
            if (!RickHouseMap.isInstalled(here.getServer())) {
                player.displayClientMessage(Component.translatable("multiverselab.msg.no_map"), false);
                return;
            }
            target = BlockPos.containing(RickHouseMap.SPAWN_X, RickHouseMap.SPAWN_Y, RickHouseMap.SPAWN_Z);
            dest.getChunk(target.getX() >> 4, target.getZ() >> 4);
        } else {
            target = findSafe(dest, player.getBlockX(), player.getBlockZ());
        }

        here.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(),
                80, 0.5, 1.0, 0.5, 0.3);
        here.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 0.7f);

        player.teleportTo(dest, target.getX() + 0.5, target.getY(), target.getZ() + 0.5,
                Set.<Relative>of(), player.getYRot(), player.getXRot(), true);

        dest.sendParticles(ParticleTypes.PORTAL, target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5,
                80, 0.5, 1.0, 0.5, 0.3);
        dest.playSound(null, target, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1.2f);

        // обратный портал за спиной игрока
        placePortal(dest, target.relative(facing.getOpposite(), 2), facing, originIndex, gold);
    }

    // ---------------------------------------------------------------- безопасная точка

    public static BlockPos findSafe(ServerLevel lvl, int x0, int z0) {
        for (int i = 0; i < 8; i++) {
            int x = x0 + i * 24;
            int z = z0;
            lvl.getChunk(x >> 4, z >> 4); // генерирует чанк
            int top = lvl.dimensionType().hasCeiling() ? 120 : lvl.getMaxY() - 2;
            int bottom = lvl.getMinY() + 2;
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x, top, z);
            for (int y = top; y > bottom; y--) {
                p.setY(y);
                BlockPos below = p.below();
                BlockPos head = p.above();
                BlockState feet = lvl.getBlockState(p);
                BlockState hd = lvl.getBlockState(head);
                BlockState bs = lvl.getBlockState(below);
                if (feet.getCollisionShape(lvl, p).isEmpty() && feet.getFluidState().isEmpty()
                        && hd.getCollisionShape(lvl, head).isEmpty() && hd.getFluidState().isEmpty()
                        && !bs.getCollisionShape(lvl, below).isEmpty() && bs.getFluidState().isEmpty()) {
                    return p.immutable();
                }
            }
        }
        // ничего подходящего - делаем платформу
        BlockPos c = new BlockPos(x0, 80, z0);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                lvl.setBlock(c.offset(dx, -1, dz), Blocks.STONE.defaultBlockState(), 3);
                lvl.setBlock(c.offset(dx, 0, dz), Blocks.AIR.defaultBlockState(), 3);
                lvl.setBlock(c.offset(dx, 1, dz), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        return c;
    }
}
