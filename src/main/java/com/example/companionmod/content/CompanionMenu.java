package com.example.companionmod.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

public class CompanionMenu extends AbstractContainerMenu {
    private final CompanionEntity companion;
    private final SimpleContainer inventory;

    public CompanionMenu(int id, Inventory playerInventory, CompanionEntity companion) {
        super(ModMenus.COMPANION_MENU.get(), id);
        this.companion = companion;
        this.inventory = companion.getInventory();
        this.inventory.startOpen(playerInventory.player);

        addCompanionSlots();
        addPlayerInventory(playerInventory);
    }

    public static CompanionMenu forServer(int id, Inventory playerInventory, CompanionEntity companion) {
        return new CompanionMenu(id, playerInventory, companion);
    }

    public static CompanionMenu fromNetwork(int id, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        Entity entity = playerInventory.player.level().getEntity(entityId);
        if (entity instanceof CompanionEntity companion) {
            return new CompanionMenu(id, playerInventory, companion);
        }
        throw new IllegalStateException("No companion entity for menu");
    }

    private void addCompanionSlots() {
        int slotIndex = 0;
        int left = 8;

        int[] firstRowXs = {left, left + 18, left + 36, left + 54, left + 90};
        for (int x : firstRowXs) {
            this.addSlot(new Slot(inventory, slotIndex++, x, 18));
        }

        int[] rows = {40, 58, 76, 98};
        for (int rowY : rows) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, slotIndex++, left + column * 18, rowY));
            }
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int left = 8;
        int[] inventoryRows = {163, 181, 199};
        for (int row = 0; row < inventoryRows.length; ++row) {
            int rowY = inventoryRows[row];
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, left + column * 18, rowY));
            }
        }

        int hotbarY = 221;
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, left + column * 18, hotbarY));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();
            int companionSlots = inventory.getContainerSize();
            if (slotIndex < companionSlots) {
                if (!this.moveItemStackTo(stackInSlot, companionSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, companionSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.companion != null && this.companion.isWithinUsableDistance(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }
}
