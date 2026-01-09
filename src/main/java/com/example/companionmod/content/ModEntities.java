package com.example.companionmod.content;

import com.example.companionmod.CompanionMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, CompanionMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CompanionEntity>> COMPANION = ENTITIES.register("companion",
            () -> EntityType.Builder.<CompanionEntity>of(CompanionEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(ResourceLocation.fromNamespaceAndPath(CompanionMod.MOD_ID, "companion").toString()));
}
