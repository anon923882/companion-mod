package com.yourname.companionmod.entity;

import com.yourname.companionmod.CompanionMod;
import com.yourname.companionmod.entity.custom.CompanionEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
        DeferredRegister.create(Registries.ENTITY_TYPE, CompanionMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CompanionEntity>> COMPANION = 
        ENTITY_TYPES.register("companion", () -> EntityType.Builder.of(CompanionEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.8f)
            .clientTrackingRange(10)
            .build("companion"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}