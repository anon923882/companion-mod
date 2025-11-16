package com.yourname.companionmod.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CompanionMenu extends AbstractContainerMenu {
    private final Container companionInventory;
    private static final int COMPANION_INVENTORY_SIZE = 27;

    // Client constructor
    public CompanionMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(COMPANION_INVENTORY_SIZE));
    }

    // Server constructor
    public CompanionMenu(int containerId, Inventory playerInventory, Container companionInventory) {
        super(ModMenuTypes.COMPANION_MENU.get(), containerId);
        this.companionInventory = companionInventory;

        // Companion inventory slots (3x9 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(companionInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }

        // Player hotbar (1x9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 144));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < COMPANION_INVENTORY_SIZE) {
                // Moving from companion inventory to player inventory
                if (!this.moveItemStackTo(slotStack, COMPANION_INVENTORY_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to companion inventory
                if (!this.moveItemStackTo(slotStack, 0, COMPANION_INVENTORY_SIZE, false)) {
                    return ItemStack.EMPTY;
                }
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
    public boolean stillValid(Player player) {
        return this.companionInventory.stillValid(player);
    }
}