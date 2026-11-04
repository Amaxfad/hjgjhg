package com.multiverselab;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MultiverseLab.MODID);

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MultiverseLab.MODID, name);
    }

    private static RegistryObject<Item> simple(String name, int stack) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().setId(ITEMS.key(name)).stacksTo(stack)));
    }

    // ------------------------------------------------------------ звук
    public static final RegistryObject<SoundEvent> PORTAL_SHOOT = SOUNDS.register("item.portal_gun.shoot",
            () -> SoundEvent.createVariableRangeEvent(id("item.portal_gun.shoot")));

    // ------------------------------------------------------------ существа
    public static final RegistryObject<EntityType<EvilMortyEntity>> EVIL_MORTY = ENTITIES.register("evil_morty",
            () -> EntityType.Builder.of(EvilMortyEntity::new, MobCategory.MONSTER).sized(0.6f, 1.8f)
                    .build(ENTITIES.key("evil_morty")));

    // ------------------------------------------------------------ блоки
    public static final RegistryObject<Block> LAB_FLASK = BLOCKS.register("lab_flask",
            () -> new FlaskBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("lab_flask")).strength(0.3f).sound(SoundType.GLASS).noOcclusion()));

    public static final RegistryObject<Block> PORTAL_FIELD = BLOCKS.register("portal_field",
            () -> new PortalFieldBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("portal_field")).strength(-1f, 3600000f).noCollision().noOcclusion()
                    .noLootTable().lightLevel(state -> 11)));

    public static final RegistryObject<Block> CLONE_POD = BLOCKS.register("clone_pod",
            () -> new ClonePodBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("clone_pod")).strength(2.0f).sound(SoundType.GLASS).noOcclusion()
                    .lightLevel(state -> state.getValue(ClonePodBlock.POD) == ClonePodBlock.PodState.EMPTY ? 0 : 10)));

    // ------------------------------------------------------------ предметы
    public static final RegistryObject<Item> CLONE_POD_ITEM = ITEMS.register("clone_pod",
            () -> new BlockItem(CLONE_POD.get(),
                    new Item.Properties().setId(ITEMS.key("clone_pod")).useBlockDescriptionPrefix()));

    public static final RegistryObject<Item> CLONE_VIAL = simple("clone_serum", 16);

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
            boolean golden = name.equals("golden_portal_gun");
            GUNS.put(name, ITEMS.register(name,
                    () -> new PortalGunItem(new Item.Properties().setId(ITEMS.key(name)).stacksTo(1), golden)));
        }
    }

    public static final RegistryObject<Item> PORTAL_GUN = GUNS.get("portal_gun");

    public static final RegistryObject<Item> PORTAL_VIAL = simple("portal_fluid_vial", 16);
    public static final RegistryObject<Item> GOLD_VIAL = simple("golden_portal_fluid", 16);

    public static final RegistryObject<Item> SERUM_VIAL = ITEMS.register("antigrav_serum",
            () -> new SerumVialItem(new Item.Properties().setId(ITEMS.key("antigrav_serum")).stacksTo(16)));

    public static final RegistryObject<Item> FLURBO = simple("flurbo_crystal", 64);
    public static final RegistryObject<Item> MEGA_SEED = simple("mega_seed_extract", 64);
    public static final RegistryObject<Item> GOO = simple("interdimensional_goo", 64);
    public static final RegistryObject<Item> MIXING_ROD = simple("mixing_rod", 1);

    public static final RegistryObject<Item> NOTEBOOK = ITEMS.register("lab_notebook",
            () -> new NotebookItem(new Item.Properties().setId(ITEMS.key("lab_notebook")).stacksTo(1)));

    public static final RegistryObject<Item> EVIL_MORTY_EGG = ITEMS.register("evil_morty_spawn_egg",
            () -> new Item(new Item.Properties().setId(ITEMS.key("evil_morty_spawn_egg")).spawnEgg(EVIL_MORTY.get())));
    public static final RegistryObject<Item> EYEPATCH = simple("evil_morty_eyepatch", 1);

    public static final RegistryObject<Item> CYBER_ARM = ITEMS.register("cyber_arm",
            () -> new Implants.ImplantItem(new Item.Properties().setId(ITEMS.key("cyber_arm")).stacksTo(1), Implants.ARM));
    public static final RegistryObject<Item> CYBER_LEG = ITEMS.register("cyber_leg",
            () -> new Implants.ImplantItem(new Item.Properties().setId(ITEMS.key("cyber_leg")).stacksTo(1), Implants.LEG));
    public static final RegistryObject<Item> CYBER_EYE = ITEMS.register("cyber_eye",
            () -> new Implants.ImplantItem(new Item.Properties().setId(ITEMS.key("cyber_eye")).stacksTo(1), Implants.EYE));

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
                        out.accept(GOLD_VIAL.get());
                        out.accept(SERUM_VIAL.get());
                        out.accept(EVIL_MORTY_EGG.get());
                        out.accept(EYEPATCH.get());
                        out.accept(CYBER_ARM.get());
                        out.accept(CYBER_LEG.get());
                        out.accept(CYBER_EYE.get());
                    })
                    .build());

    public static void register(BusGroup bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
        SOUNDS.register(bus);
        ENTITIES.register(bus);
    }
}
