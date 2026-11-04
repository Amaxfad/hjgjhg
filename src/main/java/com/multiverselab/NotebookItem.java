package com.multiverselab;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Лабораторный блокнот: показывает рецепты (пропорции) в чате. */
public class NotebookItem extends Item {
    public NotebookItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.translatable("multiverselab.notebook.title"), false);
            player.displayClientMessage(Component.translatable("multiverselab.notebook.portal"), false);
            player.displayClientMessage(Component.translatable("multiverselab.notebook.serum"), false);
            player.displayClientMessage(Component.translatable("multiverselab.notebook.clone"), false);
            player.displayClientMessage(Component.translatable("multiverselab.notebook.gold"), false);
            player.displayClientMessage(Component.translatable("multiverselab.notebook.warning"), false);
        }
        return InteractionResult.SUCCESS;
    }
}
