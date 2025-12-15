package com.example.companionmod.client;

import com.example.companionmod.CompanionMod;
import com.example.companionmod.content.CompanionEntity;
import com.example.companionmod.content.ModEntities;
import com.example.companionmod.content.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class CompanionClient {
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COMPANION_MENU.get(), CompanionScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.COMPANION.get(), CompanionRenderer::new);
    }

    private static class CompanionRenderer extends HumanoidMobRenderer<CompanionEntity, PlayerModel<CompanionEntity>> {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CompanionMod.MOD_ID, "textures/entity/companion.png");

        public CompanionRenderer(EntityRendererProvider.Context context) {
            super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
            this.addLayer(new HumanoidArmorLayer<>(this,
                    new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                    new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)),
                    context.getModelManager()));
            this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        }

        @Override
        public ResourceLocation getTextureLocation(CompanionEntity entity) {
            return TEXTURE;
        }

        @Override
        protected void renderNameTag(CompanionEntity entity, Component name, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTick) {
            double distance = this.entityRenderDispatcher.distanceToSqr(entity);
            if (distance > 4096.0D || !this.shouldShowName(entity)) {
                return;
            }

            super.renderNameTag(entity, name, poseStack, buffer, packedLight, partialTick);

            Component statusLine = buildStatusLine(entity);
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.26F, 0.0D);
            super.renderNameTag(entity, statusLine, poseStack, buffer, packedLight, partialTick);
            poseStack.popPose();
        }

        private static Component buildStatusLine(CompanionEntity entity) {
            float health = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            int armor = Mth.floor(entity.getArmorValue());
            Component heartText = Component.literal(String.format("❤ %.1f/%.1f", health, maxHealth)).withStyle(ChatFormatting.RED);
            if (armor > 0) {
                Component armorText = Component.literal("  ⛨ " + armor).withStyle(ChatFormatting.BLUE);
                return Component.empty().append(heartText).append(armorText);
            }
            return heartText;
        }
    }
}
