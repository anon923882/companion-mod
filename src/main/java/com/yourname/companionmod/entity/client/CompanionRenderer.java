package com.yourname.companionmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yourname.companionmod.entity.custom.CompanionEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.yourname.companionmod.CompanionMod;

public class CompanionRenderer extends MobRenderer<CompanionEntity, HumanoidModel<CompanionEntity>> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
        ResourceLocation.fromNamespaceAndPath(CompanionMod.MOD_ID, "textures/entity/companion.png"),
        ResourceLocation.fromNamespaceAndPath(CompanionMod.MOD_ID, "textures/entity/companion_female1.png"),
        ResourceLocation.fromNamespaceAndPath(CompanionMod.MOD_ID, "textures/entity/companion_female2.png")
    };

    public CompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionEntity entity) {
        int idx = Math.max(0, Math.min(TEXTURES.length - 1, entity.getTextureVariant()));
        return TEXTURES[idx];
    }

    @Override
    protected void scale(CompanionEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}