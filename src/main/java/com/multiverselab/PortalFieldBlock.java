package com.multiverselab;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Зелёный анимированный портал (временный блок). Раз в 5 тиков проверяет, не стоит ли в нём игрок,
 * и если стоит - переносит его в измерение DEST. Исчезает через ~20 секунд.
 * AXIS - ось, вдоль которой смотрит плоскость портала (нормаль).
 */
public class PortalFieldBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final IntegerProperty DEST = IntegerProperty.create("dest", 0, 7);
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 80);

    private static final VoxelShape SHAPE_Z = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape SHAPE_X = Block.box(6, 0, 0, 10, 16, 16);

    public PortalFieldBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Z).setValue(DEST, 0).setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, DEST, AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AABB box = new AABB(pos).inflate(0.3, 0, 0.3).expandTowards(0, 1, 0);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, box);
        if (!players.isEmpty()) {
            level.removeBlock(pos, false);
            PortalTravel.travel(players.get(0), state.getValue(DEST));
            return;
        }
        int age = state.getValue(AGE);
        if (age >= 80) {
            level.removeBlock(pos, false);
            return;
        }
        level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_INVISIBLE);
        level.scheduleTick(pos, this, 5);
    }
}
