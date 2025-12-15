package com.example.companionmod.content;

import com.example.companionmod.CompanionMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, CompanionMod.MOD_ID);

    public static final DeferredHolder<Item, SpawnEggItem> COMPANION_SPAWN_EGG = ITEMS.register("companion_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.COMPANION, 0x6d8be6, 0x3b4460, new Item.Properties()));

    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.accept(COMPANION_SPAWN_EGG.get());
        }
    }
}
