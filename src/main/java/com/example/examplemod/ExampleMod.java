package com.example.examplemod;

import com.example.examplemod.blocks.BlockEventHandler;
import net.minecraft.world.item.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;


@Mod(ExampleMod.MODID)
public class ExampleMod {
    
    public static final String MODID = "examplemod";
    public static final Logger LOGGER = LogUtils.getLogger();


    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<com.example.examplemod.entity.Fisherman>> FISHERMAN = ENTITY_TYPES.register("fisherman",
            () -> EntityType.Builder.of(com.example.examplemod.entity.Fisherman::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "fisherman"))));
//동전 아이템들

    public static final DeferredItem<Item> RETURN_SCROLL = ITEMS.registerSimpleItem("return_scroll", props -> props.stacksTo(16));
    public static final DeferredItem<Item> COIN_1 = ITEMS.registerSimpleItem("coin_1", props -> props);
    public static final DeferredItem<Item> COIN_5 = ITEMS.registerSimpleItem("coin_5", props -> props);
    public static final DeferredItem<Item> COIN_10 = ITEMS.registerSimpleItem("coin_10", props -> props);
    public static final DeferredItem<Item> COIN_50 = ITEMS.registerSimpleItem("coin_50", props -> props);
    public static final DeferredItem<Item> COIN_100 = ITEMS.registerSimpleItem("coin_100", props -> props);
    //땅블럭


    public static final DeferredBlock<Block> LAND_BLOCK= BLOCKS.registerSimpleBlock("land_block",
            p -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT));
    public static final DeferredItem<BlockItem> LAND_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("land_block", LAND_BLOCK);

    public static final DeferredBlock<Block> FLAG_BLOCK= BLOCKS.registerSimpleBlock("flag_block");
    public static final DeferredItem<BlockItem> FLAG_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("flag_block", FLAG_BLOCK);

    

    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerAttributes);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.register(new BlockEventHandler());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }
        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());
        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(LAND_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(FISHERMAN.get(), Villager.createAttributes().build());
    }
}
