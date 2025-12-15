package com.example.companionmod.content;

import com.example.companionmod.CompanionMod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.minecraft.world.Containers;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

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

    public static void addHostileTargets(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Monster monster) {
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(monster, CompanionEntity.class, true));
        }
    }
}
