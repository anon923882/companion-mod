package com.example.companionmod.content;

import com.example.companionmod.CompanionMod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.minecraft.world.Containers;

public class CompanionEvents {
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COMPANION.get(), CompanionEntity.createAttributes().build());
    }

    public static void handleDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof CompanionEntity companion) {
            Containers.dropContents(event.getEntity().level(), event.getEntity().blockPosition(), companion.getInventory());
            companion.getInventory().clearContent();
        }
    }
}
