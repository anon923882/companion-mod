package com.example.companionmod.client;

import com.example.companionmod.CompanionMod;
import com.example.companionmod.content.CompanionEntity;
import com.example.companionmod.content.ModEntities;
import com.example.companionmod.content.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class CompanionClient {
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COMPANION_MENU.get(), CompanionScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.COMPANION.get(), CompanionRenderer::new);
    }

    private static class CompanionRenderer extends HumanoidMobRenderer<CompanionEntity, HumanoidModel<CompanionEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CompanionMod.MOD_ID, "textures/entity/companion.png");

        public CompanionRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(CompanionEntity entity) {
            return TEXTURE;
        }
    }
}
