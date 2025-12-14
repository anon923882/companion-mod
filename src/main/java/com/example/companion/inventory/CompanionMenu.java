package com.example.companion.inventory;

import com.example.companion.CompanionMod;
import com.example.companion.entity.CompanionEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CompanionMenu extends AbstractContainerMenu {
    private static final int COMPANION_SLOTS = 27;
    private final CompanionEntity companion;
    private final SimpleContainer inventory;
    private final ContainerLevelAccess access;

    public CompanionMenu(int containerId, Inventory playerInventory, CompanionEntity companion) {
        super(CompanionMod.COMPANION_MENU.get(), containerId);
        this.companion = companion;
        this.inventory = companion.getInventory();
        this.access = ContainerLevelAccess.create(companion.level(), companion.blockPosition());

        this.inventory.startOpen(playerInventory.player);

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(inventory, column + row * 9, 8 + column * 18, 17 + row * 18));
            }
        }

        int playerInventoryY = 84;
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, playerInventoryY + row * 18));
            }
        }

        int hotbarY = playerInventoryY + 58;
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, hotbarY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, pos) -> !companion.isRemoved() && companion.distanceTo(player) < 8.0F, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (index < COMPANION_SLOTS) {
                if (!this.moveItemStackTo(slotStack, COMPANION_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, COMPANION_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }
}
