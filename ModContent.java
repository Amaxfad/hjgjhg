package com.multiverselab;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModContent {
    private ModContent() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MultiverseLab.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MultiverseLab.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MultiverseLab.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, MultiverseLab.MODID);

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MultiverseLab.MODID, name);
    }

    // ------------------------------------------------------------ звук
    public static final RegistryObject<SoundEvent> PORTAL_SHOOT = SOUNDS.register("item.portal_gun.shoot",
            () -> SoundEvent.createVariableRangeEvent(id("item.portal_gun.shoot")));

    // ------------------------------------------------------------ блоки
    public static final RegistryObject<Block> LAB_FLASK = BLOCKS.register("lab_flask",
            () -> new FlaskBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("lab_flask"))
                    .strength(0.3f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistryObject<Block> PORTAL_FIELD = BLOCKS.register("portal_field",
            () -> new PortalFieldBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("portal_field"))
                    .strength(-1f, 3600000f)
                    .noCollision()
                    .noOcclusion()
                    .noLootTable()
                    .lightLevel(state -> 11)));

    public static final RegistryObject<Block> CLONE_POD = BLOCKS.register("clone_pod",
            () -> new ClonePodBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("clone_pod"))
                    .strength(2.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(ClonePodBlock.POD) == ClonePodBlock.PodState.EMPTY ? 0 : 10)));

    // ------------------------------------------------------------ предметы
    public static final RegistryObject<Item> CLONE_POD_ITEM = ITEMS.register("clone_pod",
            () -> new BlockItem(CLONE_POD.get(),
                    new Item.Properties().setId(ITEMS.key("clone_pod")).useBlockDescriptionPrefix()));

    public static final RegistryObject<Item> CLONE_VIAL = ITEMS.register("clone_serum",
            () -> new Item(new Item.Properties().setId(ITEMS.key("clone_serum")).stacksTo(16)));

    public static final RegistryObject<Item> LAB_FLASK_ITEM = ITEMS.register("lab_flask",
            () -> new BlockItem(LAB_FLASK.get(),
                    new Item.Properties().setId(ITEMS.key("lab_flask")).useBlockDescriptionPrefix()));

    /** Все скины пушки (модели и текстуры лежат в assets/.../models/item/guns). */
    public static final List<String> GUN_IDS = List.of(
            "portal_gun", "alt_portal_gun", "flintlock_portal_gun", "futuristic_portal_gun",
            "golden_portal_gun", "junk_portal_gun", "portal_gun_mk2", "prime_portal_gun",
            "sidestep_portal_gun", "sloped_portal_gun", "square_portal_gun", "symmetrical_portal_gun");

    public static final Map<String, RegistryObject<Item>> GUNS = new LinkedHashMap<>();

    static {
        for (String name : GUN_IDS) {
            GUNS.put(name, ITEMS.register(name,
                    () -> new PortalGunItem(new Item.Properties().setId(ITEMS.key(name)).stacksTo(1))));
        }
    }

    public static final RegistryObject<Item> PORTAL_GUN = GUNS.get("portal_gun");

    public static final RegistryObject<Item> PORTAL_VIAL = ITEMS.register("portal_fluid_vial",
            () -> new Item(new Item.Properties().setId(ITEMS.key("portal_fluid_vial")).stacksTo(16)));

    public static final RegistryObject<Item> SERUM_VIAL = ITEMS.register("antigrav_serum",
            () -> new SerumVialItem(new Item.Properties().setId(ITEMS.key("antigrav_serum")).stacksTo(16)));

    public static final RegistryObject<Item> FLURBO = ITEMS.register("flurbo_crystal",
            () -> new Item(new Item.Properties().setId(ITEMS.key("flurbo_crystal"))));

    public static final RegistryObject<Item> MEGA_SEED = ITEMS.register("mega_seed_extract",
            () -> new Item(new Item.Properties().setId(ITEMS.key("mega_seed_extract"))));

    public static final RegistryObject<Item> GOO = ITEMS.register("interdimensional_goo",
            () -> new Item(new Item.Properties().setId(ITEMS.key("interdimensional_goo"))));

    public static final RegistryObject<Item> MIXING_ROD = ITEMS.register("mixing_rod",
            () -> new Item(new Item.Properties().setId(ITEMS.key("mixing_rod")).stacksTo(1)));

    public static final RegistryObject<Item> NOTEBOOK = ITEMS.register("lab_notebook",
            () -> new NotebookItem(new Item.Properties().setId(ITEMS.key("lab_notebook")).stacksTo(1)));

    // ------------------------------------------------------------ вкладка творческого режима
    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.multiverselab"))
                    .icon(() -> PORTAL_GUN.get().getDefaultInstance())
                    .displayItems((params, out) -> {
                        GUNS.values().forEach(g -> out.accept(g.get()));
                        out.accept(LAB_FLASK_ITEM.get());
                        out.accept(CLONE_POD_ITEM.get());
                        out.accept(CLONE_VIAL.get());
                        out.accept(MIXING_ROD.get());
                        out.accept(NOTEBOOK.get());
                        out.accept(FLURBO.get());
                        out.accept(MEGA_SEED.get());
                        out.accept(GOO.get());
                        out.accept(PORTAL_VIAL.get());
                        out.accept(SERUM_VIAL.get());
                    })
                    .build());

    public static void register(BusGroup bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
        SOUNDS.register(bus);
    }
}
