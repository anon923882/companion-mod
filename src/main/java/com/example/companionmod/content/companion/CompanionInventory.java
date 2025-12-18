package com.example.companionmod.content.companion;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.RegistryAccess;

public class CompanionInventory extends SimpleContainer {
    public static final int SIZE = 41;
    public static final int HEAD_SLOT = 0;
    public static final int CHEST_SLOT = 1;
    public static final int LEGS_SLOT = 2;
    public static final int FEET_SLOT = 3;
    public static final int OFFHAND_SLOT = 4;
    public static final int HOTBAR_START = 5;
    public static final int HOTBAR_END = 13;

    public CompanionInventory() {
        super(SIZE);
    }

    public void saveToTag(CompoundTag tag, RegistryAccess registryAccess) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < getContainerSize(); i++) {
            stacks.set(i, getItem(i));
        }
        ContainerHelper.saveAllItems(tag, stacks, registryAccess);
    }

    public void loadFromTag(CompoundTag tag, RegistryAccess registryAccess) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, stacks, registryAccess);
        for (int i = 0; i < stacks.size() && i < getContainerSize(); i++) {
            setItem(i, stacks.get(i));
        }
    }
}
