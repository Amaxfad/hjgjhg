package com.multiverselab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Лабораторная колба. Всё состояние хранится в blockstate (без BlockEntity):
 * a/b/c - сколько реагентов внутри (0..4), liquid - что внутри, fill - уровень жидкости 0..4.
 * Когда реакция прошла успешно, fill = число готовых флаконов.
 */
public class FlaskBlock extends Block {
    public static final IntegerProperty A = IntegerProperty.create("a", 0, 4);
    public static final IntegerProperty B = IntegerProperty.create("b", 0, 4);
    public static final IntegerProperty C = IntegerProperty.create("c", 0, 4);
    public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, 4);
    public static final EnumProperty<Liquid> LIQUID = EnumProperty.create("liquid", Liquid.class);

    public enum Liquid implements StringRepresentable {
        NONE, MURKY, PORTAL, SERUM, CLONE;

        boolean finished() {
            return this == PORTAL || this == SERUM || this == CLONE;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    /** Рецепт: пропорция реагентов A:B:C (Flurbo : Mega Seed : Goo). */
    private record Reaction(int a, int b, int c, Liquid out) {
        boolean matches(int x, int y, int z) {
            return x * b == y * a && x * c == z * a && y * c == z * b;
        }
    }

    private static final Reaction[] REACTIONS = {
            new Reaction(2, 1, 1, Liquid.PORTAL),
            new Reaction(1, 2, 1, Liquid.SERUM),
            new Reaction(1, 1, 2, Liquid.CLONE)
    };

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 6, 14),
            Block.box(6, 6, 6, 10, 13, 10));

    public FlaskBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(A, 0).setValue(B, 0).setValue(C, 0)
                .setValue(FILL, 0).setValue(LIQUID, Liquid.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(A, B, C, FILL, LIQUID);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    // ---------------------------------------------------------------- взаимодействие

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        int reagent = reagentIndex(stack);
        if (reagent >= 0) {
            if (state.getValue(LIQUID).finished()) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("multiverselab.msg.occupied"), true);
                }
                return InteractionResult.FAIL;
            }
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            int[] v = {state.getValue(A), state.getValue(B), state.getValue(C)};
            v[reagent]++;
            stack.consume(1, player);
            if (v[reagent] > 4) {
                // передозировка одного реагента - сразу взрыв
                blowUp(level, pos, v[0] + v[1] + v[2], player);
                return InteractionResult.SUCCESS;
            }
            setMixture(level, pos, v[0], v[1], v[2]);
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        if (stack.is(ModContent.MIXING_ROD)) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            react(state, level, pos, player);
            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.GLASS_BOTTLE)) {
            Liquid liquid = state.getValue(LIQUID);
            int fill = state.getValue(FILL);
            if (liquid.finished() && fill > 0) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }
                stack.consume(1, player);
                Item vialItem = switch (liquid) {
                    case PORTAL -> ModContent.PORTAL_VIAL;
                    case SERUM -> ModContent.SERUM_VIAL;
                    default -> ModContent.CLONE_VIAL;
                };
                ItemStack out = new ItemStack(vialItem);
                if (!player.getInventory().add(out)) {
                    player.drop(out, false);
                }
                int left = fill - 1;
                level.setBlock(pos, left <= 0 ? defaultBlockState()
                        : state.setValue(FILL, left), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1f, 1f);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            level.setBlock(pos, defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 1f);
            player.displayClientMessage(Component.translatable("multiverselab.msg.cleared"), true);
        } else {
            player.displayClientMessage(Component.translatable("multiverselab.msg.contents",
                    state.getValue(A), state.getValue(B), state.getValue(C)), true);
        }
        return InteractionResult.SUCCESS;
    }

    // ---------------------------------------------------------------- химия

    private static int reagentIndex(ItemStack stack) {
        if (stack.is(ModContent.FLURBO)) return 0;
        if (stack.is(ModContent.MEGA_SEED)) return 1;
        if (stack.is(ModContent.GOO)) return 2;
        return -1;
    }

    private void setMixture(Level level, BlockPos pos, int a, int b, int c) {
        int total = a + b + c;
        BlockState s = defaultBlockState()
                .setValue(A, a).setValue(B, b).setValue(C, c)
                .setValue(LIQUID, total > 0 ? Liquid.MURKY : Liquid.NONE)
                .setValue(FILL, Math.min(4, (total + 2) / 3));
        level.setBlock(pos, s, Block.UPDATE_ALL);
    }

    private void react(BlockState state, Level level, BlockPos pos, Player player) {
        int a = state.getValue(A), b = state.getValue(B), c = state.getValue(C);
        int total = a + b + c;
        if (state.getValue(LIQUID) != Liquid.MURKY || total == 0) {
            player.displayClientMessage(Component.translatable("multiverselab.msg.nothing"), true);
            return;
        }
        for (Reaction r : REACTIONS) {
            if (r.matches(a, b, c)) {
                int k = total / (r.a() + r.b() + r.c());
                int vials = Math.min(4, k * 2);
                level.setBlock(pos, defaultBlockState()
                        .setValue(LIQUID, r.out()).setValue(FILL, vials), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1f, 0.6f);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.0);
                }
                player.displayClientMessage(Component.translatable("multiverselab.msg.success", vials), true);
                return;
            }
        }
        blowUp(level, pos, total, player);
    }

    private void blowUp(Level level, BlockPos pos, int total, Player player) {
        level.removeBlock(pos, false);
        float power = Math.min(8f, 2.5f + total * 0.6f);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                power, true, Level.ExplosionInteraction.TNT);
        if (player != null) {
            player.displayClientMessage(Component.translatable("multiverselab.msg.boom"), true);
        }
    }
}
