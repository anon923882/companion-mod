package com.example.companion.client;

import com.example.companion.entity.CompanionEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CompanionRenderer extends MobRenderer<CompanionEntity, VillagerModel<CompanionEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("minecraft:textures/entity/villager/villager.png");

    public CompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionEntity entity) {
        return TEXTURE;
    }
}
