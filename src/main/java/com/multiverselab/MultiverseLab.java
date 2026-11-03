package com.multiverselab;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MultiverseLab.MODID)
public final class MultiverseLab {
    public static final String MODID = "multiverselab";

    public MultiverseLab(FMLJavaModLoadingContext context) {
        ModContent.register(context.getModBusGroup());
        ModEvents.register();
    }
}
