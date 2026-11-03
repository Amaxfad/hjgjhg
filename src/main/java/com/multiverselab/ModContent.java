package com.multiverselab;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModContent {
    private ModContent() {}

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MultiverseLab.MODID, name);
    }

    private static <B extends Block> B block(String name, Function<BlockBehaviour.Properties, B> factory,
                                             BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(props.setId(key)));
    }

    private static <I extends Item> I item(String name, Function<Item.Properties, I> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
    }

    private static Item simple(String name, Item.Properties props) {
        return item(name, Item::new, props);
    }

    // ------------------------------------------------------------ звук
    public static final SoundEvent PORTAL_SHOOT = Registry.register(BuiltInRegistries.SOUND_EVENT,
            id("item.portal_gun.shoot"), SoundEvent.createVariableRangeEvent(id("item.portal_gun.shoot")));

    // ------------------------------------------------------------ блоки
    public static final Block LAB_FLASK = block("lab_flask", FlaskBlock::new,
            BlockBehaviour.Properties.of().strength(0.3f).sound(SoundType.GLASS).noOcclusion());

    public static final Block PORTAL_FIELD = block("portal_field", PortalFieldBlock::new,
            BlockBehaviour.Properties.of().strength(-1f, 3600000f).noCollision().noOcclusion()
                    .noLootTable().lightLevel(state -> 11));

    public static final Block CLONE_POD = block("clone_pod", ClonePodBlock::new,
            BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.GLASS).noOcclusion()
                    .lightLevel(state -> state.getValue(ClonePodBlock.POD) == ClonePodBlock.PodState.EMPTY ? 0 : 10));

    // ------------------------------------------------------------ предметы
    public static final Item CLONE_POD_ITEM = item("clone_pod",
            p -> new BlockItem(CLONE_POD, p), new Item.Properties().useBlockDescriptionPrefix());

    public static final Item CLONE_VIAL = simple("clone_serum", new Item.Properties().stacksTo(16));

    public static final Item LAB_FLASK_ITEM = item("lab_flask",
            p -> new BlockItem(LAB_FLASK, p), new Item.Properties().useBlockDescriptionPrefix());

    /** Все скины пушки (модели и текстуры лежат в assets/.../models/item/guns). */
    public static final List<String> GUN_IDS = List.of(
            "portal_gun", "alt_portal_gun", "flintlock_portal_gun", "futuristic_portal_gun",
            "golden_portal_gun", "junk_portal_gun", "portal_gun_mk2", "prime_portal_gun",
            "sidestep_portal_gun", "sloped_portal_gun", "square_portal_gun", "symmetrical_portal_gun");

    public static final Map<String, Item> GUNS = new LinkedHashMap<>();

    static {
        for (String name : GUN_IDS) {
            GUNS.put(name, item(name, PortalGunItem::new, new Item.Properties().stacksTo(1)));
        }
    }

    public static final Item PORTAL_GUN = GUNS.get("portal_gun");

    public static final Item PORTAL_VIAL = simple("portal_fluid_vial", new Item.Properties().stacksTo(16));
    public static final Item SERUM_VIAL = item("antigrav_serum", SerumVialItem::new, new Item.Properties().stacksTo(16));
    public static final Item FLURBO = simple("flurbo_crystal", new Item.Properties());
    public static final Item MEGA_SEED = simple("mega_seed_extract", new Item.Properties());
    public static final Item GOO = simple("interdimensional_goo", new Item.Properties());
    public static final Item MIXING_ROD = simple("mixing_rod", new Item.Properties().stacksTo(1));
    public static final Item NOTEBOOK = item("lab_notebook", NotebookItem::new, new Item.Properties().stacksTo(1));

    // ------------------------------------------------------------ вкладка творческого режима
    public static final CreativeModeTab MAIN_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            id("main"), CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.multiverselab"))
                    .icon(() -> PORTAL_GUN.getDefaultInstance())
                    .displayItems((params, out) -> {
                        GUNS.values().forEach(out::accept);
                        out.accept(LAB_FLASK_ITEM);
                        out.accept(CLONE_POD_ITEM);
                        out.accept(CLONE_VIAL);
                        out.accept(MIXING_ROD);
                        out.accept(NOTEBOOK);
                        out.accept(FLURBO);
                        out.accept(MEGA_SEED);
                        out.accept(GOO);
                        out.accept(PORTAL_VIAL);
                        out.accept(SERUM_VIAL);
                    })
                    .build());

    /** Вызов заставляет загрузиться класс и зарегистрировать всё выше. */
    public static void init() {}
}
