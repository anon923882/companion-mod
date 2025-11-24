package com.yourname.companionmod.menu;

import com.yourname.companionmod.CompanionMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
        DeferredRegister.create(Registries.MENU, CompanionMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CompanionMenu>> COMPANION_MENU = 
        MENU_TYPES.register("companion_menu", 
            () -> new MenuType<>(CompanionMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}