package com.example.companionmod.content;

import com.example.companionmod.CompanionMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CompanionMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CompanionMenu>> COMPANION_MENU = MENUS.register("companion",
            () -> IMenuTypeExtension.create((windowId, inventory, buffer) -> CompanionMenu.fromNetwork(windowId, inventory, buffer)));
}
