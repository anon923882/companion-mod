package com.yourname.companionmod.menu;

import com.yourname.companionmod.entity.custom.CompanionEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;

public class CompanionMenu extends AbstractContainerMenu {
    private final Container companionInventory;
    private final CompanionEntity companion;

    // Client constructor
    public CompanionMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CompanionEntity.TOTAL_SLOTS), null);
    }

    // Server constructor
    public CompanionMenu(int containerId, Inventory playerInventory, Container companionInventory,
            CompanionEntity companion) {
        super(ModMenuTypes.COMPANION_MENU.get(), containerId);
        checkContainerSize(companionInventory, CompanionEntity.TOTAL_SLOTS);
        this.companionInventory = companionInventory;
        this.companion = companion;

        companionInventory.startOpen(playerInventory.player);

        int storageStartX = 70;
        int storageStartY = 38;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                this.addSlot(new Slot(companionInventory, index, storageStartX + col * 18,
                    storageStartY + row * 18));
            }
        }

        // Main-hand and off-hand slots
        int armorX = 26;
        int armorY = 28;
        this.addSlot(new Slot(companionInventory, CompanionEntity.MAIN_HAND_SLOT, armorX, armorY + 78));
        this.addSlot(new Slot(companionInventory, CompanionEntity.OFF_HAND_SLOT, armorX, armorY + 96));

        // Armor slots (bottom to top for consistent indices)
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.BOOTS_SLOT, armorX, armorY + 54,
            ArmorItem.Type.BOOTS));
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.LEGS_SLOT, armorX, armorY + 36,
            ArmorItem.Type.LEGGINGS));
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.CHEST_SLOT, armorX, armorY + 18,
            ArmorItem.Type.CHESTPLATE));
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.HELMET_SLOT, armorX, armorY,
            ArmorItem.Type.HELMET));

        // Player inventory (3x9)
        int playerInvStartY = storageStartY + 72;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    storageStartX + col * 18, playerInvStartY + row * 18));
            }
        }

        // Player hotbar
        int hotbarY = playerInvStartY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, storageStartX + col * 18, hotbarY));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < CompanionEntity.TOTAL_SLOTS) {
                if (!this.moveItemStackTo(slotStack, CompanionEntity.TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.movePlayerItemToCompanion(slotStack)) {
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.companionInventory.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0 && this.companion != null && this.companion.isOwnedBy(player)) {
            this.companion.equipBestGear();
            return true;
        }
        return super.clickMenuButton(player, buttonId);
    }

    private boolean movePlayerItemToCompanion(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            int slotIndex = getSlotIndexForArmor(armorItem.getType());
            if (this.moveItemStackTo(stack, slotIndex, slotIndex + 1, false)) {
                return true;
            }
        }

        if (isValidMainHandItem(stack)) {
            if (this.moveItemStackTo(stack, CompanionEntity.MAIN_HAND_SLOT, CompanionEntity.MAIN_HAND_SLOT + 1, false)) {
                return true;
            }
        }

        if (stack.getItem() instanceof ShieldItem) {
            if (this.moveItemStackTo(stack, CompanionEntity.OFF_HAND_SLOT, CompanionEntity.OFF_HAND_SLOT + 1, false)) {
                return true;
            }
        }

        return this.moveItemStackTo(stack, 0, CompanionEntity.STORAGE_SIZE, false);
    }

    private static boolean isValidMainHandItem(ItemStack stack) {
        return stack.isEdible() || stack.getItem() instanceof SwordItem || stack.getItem() instanceof TieredItem;
    }

    private static int getSlotIndexForArmor(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> CompanionEntity.HELMET_SLOT;
            case CHESTPLATE -> CompanionEntity.CHEST_SLOT;
            case LEGGINGS -> CompanionEntity.LEGS_SLOT;
            case BOOTS -> CompanionEntity.BOOTS_SLOT;
        };
    }

    private static class ArmorSlot extends Slot {
        private final ArmorItem.Type type;

        public ArmorSlot(Container container, int index, int x, int y, ArmorItem.Type type) {
            super(container, index, x, y);
            this.type = type;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ArmorItem armor && armor.getType() == this.type;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
