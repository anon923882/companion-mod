package com.yourname.companionmod;

import com.yourname.companionmod.entity.ModEntities;
import com.yourname.companionmod.entity.client.CompanionRenderer;
import com.yourname.companionmod.menu.ModMenuTypes;
import com.yourname.companionmod.screen.CompanionScreen;
import com.yourname.companionmod.entity.custom.CompanionEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(CompanionMod.MOD_ID)
public class CompanionMod {
    public static final String MOD_ID = "companionmod";

    public CompanionMod(IEventBus modEventBus, Dist dist) {
        modEventBus.addListener(this::commonSetup);
        
        ModEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        modEventBus.addListener(this::onEntityAttributeCreate);
        
        if (dist.isClient()) {
            modEventBus.addListener(this::registerRenderers);
            modEventBus.addListener(this::registerScreens);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup code
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.COMPANION.get(), CompanionRenderer::new);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COMPANION_MENU.get(), CompanionScreen::new);
    }

    private void onEntityAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COMPANION.get(), CompanionEntity.createAttributes().build());
    }
}
