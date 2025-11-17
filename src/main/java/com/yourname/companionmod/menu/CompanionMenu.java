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
    public static final int SLOT_SPACING = 18;
    public static final int STORAGE_COLUMNS = 9;
    public static final int STORAGE_ROWS = 3;
    public static final int STORAGE_START_X = 8;
    public static final int STORAGE_START_Y = 18;
    public static final int PLAYER_INVENTORY_START_Y = 84;
    public static final int HOTBAR_Y = 142;
    public static final int EQUIPMENT_COLUMN_X = STORAGE_START_X + SLOT_SPACING * STORAGE_COLUMNS + 12;
    public static final int EQUIPMENT_START_Y = STORAGE_START_Y;
    public static final int HAND_SLOT_START_Y = EQUIPMENT_START_Y + SLOT_SPACING * 4 + 10;

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

        // Companion storage inventory (27 slots only - 3 rows x 9 columns)
        for (int row = 0; row < STORAGE_ROWS; row++) {
            for (int col = 0; col < STORAGE_COLUMNS; col++) {
                int index = col + row * STORAGE_COLUMNS;
                this.addSlot(new Slot(companionInventory, index,
                    STORAGE_START_X + col * SLOT_SPACING,
                    STORAGE_START_Y + row * SLOT_SPACING));
            }
        }

        // Equipment slots (separate from storage grid)
        int equipmentColumnX = EQUIPMENT_COLUMN_X;
        int equipmentStartY = EQUIPMENT_START_Y;
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.HELMET_SLOT,
            equipmentColumnX, equipmentStartY, ArmorItem.Type.HELMET));
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.CHEST_SLOT,
            equipmentColumnX, equipmentStartY + SLOT_SPACING, ArmorItem.Type.CHESTPLATE));
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.LEGS_SLOT,
            equipmentColumnX, equipmentStartY + SLOT_SPACING * 2, ArmorItem.Type.LEGGINGS));
        this.addSlot(new ArmorSlot(companionInventory, CompanionEntity.BOOTS_SLOT,
            equipmentColumnX, equipmentStartY + SLOT_SPACING * 3, ArmorItem.Type.BOOTS));

        // Hand slots
        int handsStartY = HAND_SLOT_START_Y;
        this.addSlot(new MainHandSlot(companionInventory, CompanionEntity.MAIN_HAND_SLOT, equipmentColumnX, handsStartY));
        this.addSlot(new OffHandSlot(companionInventory, CompanionEntity.OFF_HAND_SLOT,
            equipmentColumnX, handsStartY + SLOT_SPACING));

        // Player inventory (3 rows x 9 columns)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    STORAGE_START_X + col * SLOT_SPACING,
                    PLAYER_INVENTORY_START_Y + row * SLOT_SPACING));
            }
        }

        // Player hotbar (9 columns)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                STORAGE_START_X + col * SLOT_SPACING, HOTBAR_Y));
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
        return stack.has(DataComponents.FOOD) || stack.getItem() instanceof SwordItem
            || stack.getItem() instanceof TieredItem;
    }

    private static int getSlotIndexForArmor(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> CompanionEntity.HELMET_SLOT;
            case CHESTPLATE -> CompanionEntity.CHEST_SLOT;
            case LEGGINGS -> CompanionEntity.LEGS_SLOT;
            case BOOTS -> CompanionEntity.BOOTS_SLOT;
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
