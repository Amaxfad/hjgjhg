package com.multiverselab;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Антигравитационная сыворотка: мгновенно даёт прыжок и мягкое падение. */
public class SerumVialItem extends Item {
    public SerumVialItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 1200, 2));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0));
        stack.consume(1, player);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (!player.getInventory().add(bottle)) {
            player.drop(bottle, false);
        }
        return InteractionResult.SUCCESS;
    }
}
