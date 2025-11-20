package com.yourname.companionmod.menu;

import com.yourname.companionmod.entity.custom.CompanionEntity;
import net.minecraft.core.component.DataComponents;
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
    // Vanilla survival inventory positions (matching pixel-perfect)
    public static final int ARMOR_SLOT_X = 8;
    public static final int ARMOR_START_Y = 8;
    public static final int SLOT_SPACING = 18;
    public static final int OFF_HAND_X = 77;
    public static final int OFF_HAND_Y = 62;
    public static final int MAIN_HAND_X = 98;
    public static final int MAIN_HAND_Y = 18;
    
    // Companion storage (3x9 grid, no hotbar)
    public static final int STORAGE_COLUMNS = 9;
    public static final int STORAGE_ROWS = 3;
    public static final int STORAGE_START_X = 8;
    public static final int STORAGE_START_Y = 84;
    
    // Player inventory (matching chest-style layout)
    public static final int PLAYER_INV_START_X = 8;
    public static final int PLAYER_INV_START_Y = 84;
    
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

        // Companion armor slots (left side, matching vanilla player armor)
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.HELMET_SLOT,
            ARMOR_SLOT_X, ARMOR_START_Y, ArmorItem.Type.HELMET));
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.CHEST_SLOT,
            ARMOR_SLOT_X, ARMOR_START_Y + SLOT_SPACING, ArmorItem.Type.CHESTPLATE));
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.LEGS_SLOT,
            ARMOR_SLOT_X, ARMOR_START_Y + SLOT_SPACING * 2, ArmorItem.Type.LEGGINGS));
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.BOOTS_SLOT,
            ARMOR_SLOT_X, ARMOR_START_Y + SLOT_SPACING * 3, ArmorItem.Type.BOOTS));

        // Companion hand slots
        this.addSlot(new MainHandSlot(companionInventory, CompanionEntity.MAIN_HAND_SLOT,
            MAIN_HAND_X, MAIN_HAND_Y));
        this.addSlot(new OffHandSlot(companionInventory, CompanionEntity.OFF_HAND_SLOT,
            OFF_HAND_X, OFF_HAND_Y));

        // Companion storage inventory (3 rows x 9 columns, NO hotbar)
        for (int row = 0; row < STORAGE_ROWS; row++) {
            for (int col = 0; col < STORAGE_COLUMNS; col++) {
                int index = col + row * STORAGE_COLUMNS;
                this.addSlot(new Slot(companionInventory, index,
                    STORAGE_START_X + col * SLOT_SPACING,
                    STORAGE_START_Y + row * SLOT_SPACING));
            }
        }

        // Player inventory (3 rows x 9 columns, matching chest layout)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    PLAYER_INV_START_X + col * SLOT_SPACING,
                    PLAYER_INV_START_Y + row * SLOT_SPACING));
            }
        }

        // NO PLAYER HOTBAR - removed as requested
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // Companion slots are first
            int companionSlotCount = 6 + (STORAGE_ROWS * STORAGE_COLUMNS);
            
            if (index < companionSlotCount) {
                // Moving from companion to player inventory
                if (!this.moveItemStackTo(slotStack, companionSlotCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to companion
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

    public CompanionEntity getCompanion() {
        return this.companion;
    }

    private boolean movePlayerItemToCompanion(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            int slotIndex = getSlotIndexForArmor(armorItem.getType());
            if (this.moveItemStackTo(stack, slotIndex, slotIndex + 1, false)) {
                return true;
            }
        }

        if (isValidMainHandItem(stack)) {
            int mainHandIndex = 5; // Main hand is 6th slot (0-indexed)
            if (this.moveItemStackTo(stack, mainHandIndex, mainHandIndex + 1, false)) {
                return true;
            }
        }

        if (stack.getItem() instanceof ShieldItem) {
            int offHandIndex = 4; // Off hand is 5th slot (0-indexed)
            if (this.moveItemStackTo(stack, offHandIndex, offHandIndex + 1, false)) {
                return true;
            }
        }

        // Try to move to companion storage
        return this.moveItemStackTo(stack, 6, 6 + (STORAGE_ROWS * STORAGE_COLUMNS), false);
    }

    private static boolean isValidMainHandItem(ItemStack stack) {
        return stack.has(DataComponents.FOOD) || stack.getItem() instanceof SwordItem
            || stack.getItem() instanceof TieredItem;
    }

    private static int getSlotIndexForArmor(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 0;
            case CHESTPLATE -> 1;
            case LEGGINGS -> 2;
            case BOOTS -> 3;
            default -> throw new IllegalStateException("Unexpected armor slot: " + type);
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

    private static class MainHandSlot extends Slot {
        public MainHandSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isValidMainHandItem(stack);
        }
    }

    private static class OffHandSlot extends Slot {
        public OffHandSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ShieldItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
