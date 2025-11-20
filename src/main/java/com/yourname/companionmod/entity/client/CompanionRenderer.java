package com.yourname.companionmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yourname.companionmod.CompanionMod;
import com.yourname.companionmod.entity.custom.CompanionEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CompanionRenderer extends MobRenderer<CompanionEntity, HumanoidModel<CompanionEntity>> {
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(CompanionMod.MOD_ID, "textures/entity/companion.png");

    public CompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(CompanionEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}