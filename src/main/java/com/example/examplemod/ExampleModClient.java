package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ExampleMod.MODID, dist = Dist.CLIENT)
public class ExampleModClient {
    public ExampleModClient(IEventBus modEventBus, ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // Register event listeners on the mod event bus manually (avoids annotation mismatch issues)
        modEventBus.addListener(ExampleModClient::onClientSetup);
        modEventBus.addListener(this::registerEntityRenderers);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ExampleMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        ExampleMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ExampleMod.FISHERMAN.get(), VillagerRenderer::new);
    }
}
