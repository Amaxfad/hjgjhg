package com.multiverselab;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Капсула клонирования.
 * EMPTY   - пустая капсула.
 * READY   - внутри клон; активируется сывороткой клона (ПКМ) и привязывается к игроку.
 * OPENING - клон только что "ожил", капсула открывается 3 секунды.
 *
 * Когда привязанный игрок умирает, смерть отменяется, и он появляется в капсуле;
 * через 3 секунды капсула открывается, и игрок может выйти. Капсула при этом расходуется.
 */
public class ClonePodBlock extends Block {
    public enum PodState implements StringRepresentable {
        EMPTY, READY, OPENING;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public static final EnumProperty<PodState> POD = EnumProperty.create("pod", PodState.class);

    /** Привязка игрока к капсуле: "измерение|позиция" (сохраняется вместе с игроком). */
    private static final AttachmentType<String> BOUND_POD =
            AttachmentRegistry.createPersistent(ModContent.id("bound_pod"), Codec.STRING);
    private static final int OPEN_TICKS = 60; // 3 секунды

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public ClonePodBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(POD, PodState.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POD);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    // ---------------------------------------------------------------- взаимодействие

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ModContent.CLONE_VIAL) && state.getValue(POD) == PodState.EMPTY) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            stack.consume(1, player);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
            level.setBlock(pos, state.setValue(POD, PodState.READY), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1f, 0.5f);
            bind((ServerPlayer) player, (ServerLevel) level, pos);
            player.displayClientMessage(Component.translatable("multiverselab.msg.pod_bound"), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            String key = switch (state.getValue(POD)) {
                case EMPTY -> "multiverselab.msg.pod_empty";
                case READY -> "multiverselab.msg.pod_ready";
                case OPENING -> "multiverselab.msg.pod_opening";
            };
            player.displayClientMessage(Component.translatable(key), true);
        }
        return InteractionResult.SUCCESS;
    }

    /** Через 3 секунды капсула открывается и становится пустой. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POD) == PodState.OPENING) {
            level.setBlock(pos, state.setValue(POD, PodState.EMPTY), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1f, 0.8f);
            level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    30, 0.4, 0.8, 0.4, 0.05);
        }
    }

    // ---------------------------------------------------------------- привязка и воскрешение

    private static void bind(ServerPlayer player, ServerLevel level, BlockPos pos) {
        player.setAttached(BOUND_POD, level.dimension().toString() + "|" + pos.asLong());
    }

    /**
     * Вызывается при смерти игрока. Если у него есть готовая капсула - воскрешает его там
     * и возвращает true (смерть нужно отменить).
     */
    public static boolean tryRevive(ServerPlayer player) {
        String bound = player.getAttached(BOUND_POD);
        if (bound == null) {
            return false;
        }
        int cut = bound.lastIndexOf('|');
        if (cut < 0) {
            return false;
        }
        String dim = bound.substring(0, cut);
        BlockPos pos;
        try {
            pos = BlockPos.of(Long.parseLong(bound.substring(cut + 1)));
        } catch (NumberFormatException e) {
            return false;
        }

        ServerLevel target = null;
        for (ServerLevel l : ((ServerLevel) player.level()).getServer().getAllLevels()) {
            if (l.dimension().toString().equals(dim)) {
                target = l;
                break;
            }
        }
        if (target == null) {
            return false;
        }
        BlockState state = target.getBlockState(pos);
        if (!(state.getBlock() instanceof ClonePodBlock) || state.getValue(POD) != PodState.READY) {
            return false;
        }

        // капсула расходуется
        player.removeAttached(BOUND_POD);

        player.setHealth(player.getMaxHealth());
        player.clearFire();
        player.removeAllEffects();
        player.resetFallDistance();
        player.getFoodData().setFoodLevel(20);

        player.teleportTo(target, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                Set.<Relative>of(), player.getYRot(), player.getXRot(), true);

        // на время открытия капсулы игрок не может двигаться и не получает урон
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, OPEN_TICKS, 10, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, OPEN_TICKS + 20, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, OPEN_TICKS, 0, false, false));

        target.setBlock(pos, state.setValue(POD, PodState.OPENING), Block.UPDATE_ALL);
        target.scheduleTick(pos, state.getBlock(), OPEN_TICKS);
        target.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1.5f);
        target.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                25, 0.3, 0.8, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("multiverselab.msg.pod_revived"), true);
        return true;
    }
}
