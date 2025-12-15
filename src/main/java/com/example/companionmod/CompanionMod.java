package com.example.companionmod;

import com.example.companionmod.client.CompanionClient;
import com.example.companionmod.content.CompanionEntity;
import com.example.companionmod.content.CompanionEvents;
import com.example.companionmod.content.ModMenus;
import com.example.companionmod.content.ModEntities;
import com.example.companionmod.content.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(CompanionMod.MOD_ID)
public class CompanionMod {
    public static final String MOD_ID = "companionmod";

    public CompanionMod() {
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        IEventBus modBus = container.getEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModMenus.MENUS.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(ModItems::buildCreativeTabs);
        modBus.addListener(CompanionEvents::registerAttributes);
        NeoForge.EVENT_BUS.addListener(CompanionEvents::handleDrops);
        NeoForge.EVENT_BUS.addListener(CompanionEvents::addHostileTargets);

        try {
            Class.forName("net.neoforged.neoforge.client.event.RegisterMenuScreensEvent");
            modBus.addListener(CompanionClient::registerScreens);
            modBus.addListener(CompanionClient::registerRenderers);
        } catch (ClassNotFoundException ignored) {
            // Client events are not available on a dedicated server runtime.
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }
}
