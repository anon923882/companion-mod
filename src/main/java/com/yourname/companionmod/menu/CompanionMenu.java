package com.yourname.companionmod.menu;

import com.yourname.companionmod.entity.custom.CompanionEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;

public class CompanionMenu extends AbstractContainerMenu {
    public static final int SLOT_SPACING = 18;
    public static final int EQUIP_COLUMN_X = 8;
    public static final int EQUIP_START_Y = 18;
    public static final int EQUIP_SLOT_SPACING = 18;
    public static final int EQUIP_SLOT_COUNT = 6;
    public static final int STORAGE_COLUMNS = 9;
    public static final int STORAGE_ROWS = 3;
    public static final int STORAGE_START_X = 44;
    public static final int STORAGE_START_Y = 18;
    public static final int PLAYER_INVENTORY_START_Y = 86;
    public static final int HOTBAR_Y = 144;
    public static final int BUTTON_EQUIP_BEST = 0;
    public static final int BUTTON_TOGGLE_FOLLOW = 1;
    public static final int BUTTON_TOGGLE_AUTO_HEAL = 2;
    public static final int BUTTON_TOGGLE_AUTO_EQUIP = 3;
    private static final int DATA_FOLLOW_ENABLED = 0;
    private static final int DATA_AUTO_HEAL_ENABLED = 1;
    private static final int DATA_AUTO_EQUIP_ENABLED = 2;
    private static final int SETTINGS_DATA_COUNT = 3;

    private final Container companionInventory;
    private final CompanionEntity companion;
    private final ContainerData settingsData;

    // ADDED: Helper method for standardized equip slot indexing, referenced by supplemental and UI code.
    public static int getEquipSlotIndex(int offset) {
        return CompanionEntity.STORAGE_SIZE + offset;
    }

    public CompanionMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CompanionEntity.TOTAL_SLOTS), null,
            new SimpleContainerData(SETTINGS_DATA_COUNT));
    }

    public CompanionMenu(int containerId, Inventory playerInventory, Container companionInventory,
            CompanionEntity companion) {
        this(containerId, playerInventory, companionInventory, companion,
            companion == null ? new SimpleContainerData(SETTINGS_DATA_COUNT)
                : new CompanionSettingsData(companion));
    }

    private CompanionMenu(int containerId, Inventory playerInventory, Container companionInventory,
            CompanionEntity companion, ContainerData settingsData) {
        super(ModMenuTypes.COMPANION_MENU.get(), containerId);
        checkContainerSize(companionInventory, CompanionEntity.TOTAL_SLOTS);
        this.companionInventory = companionInventory;
        this.companion = companion;
        this.settingsData = settingsData;

        companionInventory.startOpen(playerInventory.player);
        this.addDataSlots(settingsData);

        int equipX = EQUIP_COLUMN_X;
        int equipY = EQUIP_START_Y;
        this.addSlot(new EquipSlot(companionInventory, CompanionEntity.HELMET_SLOT,
            equipX, equipY, EquipSlotType.HELMET));
        equipY += EQUIP_SLOT_SPACING;
        this.addSlot(new EquipSlot(companionInventory, CompanionEntity.CHEST_SLOT,
            equipX, equipY, EquipSlotType.CHESTPLATE));
        equipY += EQUIP_SLOT_SPACING;
        this.addSlot(new EquipSlot(companionInventory, CompanionEntity.LEGS_SLOT,
            equipX, equipY, EquipSlotType.LEGGINGS));
        equipY += EQUIP_SLOT_SPACING;
        this.addSlot(new EquipSlot(companionInventory, CompanionEntity.BOOTS_SLOT,
            equipX, equipY, EquipSlotType.BOOTS));
        equipY += EQUIP_SLOT_SPACING;
        this.addSlot(new EquipSlot(companionInventory, CompanionEntity.MAIN_HAND_SLOT,
            equipX, equipY, EquipSlotType.MAIN_HAND));
        equipY += EQUIP_SLOT_SPACING;
        this.addSlot(new EquipSlot(companionInventory, CompanionEntity.OFF_HAND_SLOT,
            equipX, equipY, EquipSlotType.OFF_HAND));

        for (int row = 0; row < STORAGE_ROWS; row++) {
            for (int col = 0; col < STORAGE_COLUMNS; col++) {
                int index = col + row * STORAGE_COLUMNS;
                this.addSlot(new Slot(companionInventory, index,
                    STORAGE_START_X + col * SLOT_SPACING,
                    STORAGE_START_Y + row * SLOT_SPACING));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    STORAGE_START_X + col * SLOT_SPACING,
                    PLAYER_INVENTORY_START_Y + row * SLOT_SPACING));
            }
        }

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
        if (this.companion != null && this.companion.isOwnedBy(player)) {
            if (buttonId == BUTTON_EQUIP_BEST) {
                this.companion.equipBestGear();
                return true;
            }
            if (buttonId == BUTTON_TOGGLE_FOLLOW) {
                this.toggleFollowSetting();
                return true;
            }
            if (buttonId == BUTTON_TOGGLE_AUTO_HEAL) {
                this.toggleAutoHealSetting();
                return true;
            }
            if (buttonId == BUTTON_TOGGLE_AUTO_EQUIP) {
                this.toggleAutoEquipSetting();
                return true;
            }
        }
        return super.clickMenuButton(player, buttonId);
    }

    public boolean isFollowingEnabled() {
        return this.settingsData.get(DATA_FOLLOW_ENABLED) != 0;
    }

    public boolean isAutoHealEnabled() {
        return this.settingsData.get(DATA_AUTO_HEAL_ENABLED) != 0;
    }

    public boolean isAutoEquipEnabled() {
        return this.settingsData.get(DATA_AUTO_EQUIP_ENABLED) != 0;
    }

    private void toggleFollowSetting() {
        int newValue = this.isFollowingEnabled() ? 0 : 1;
        this.setData(DATA_FOLLOW_ENABLED, newValue);
    }

    private void toggleAutoHealSetting() {
        int newValue = this.isAutoHealEnabled() ? 0 : 1;
        this.setData(DATA_AUTO_HEAL_ENABLED, newValue);
    }

    private void toggleAutoEquipSetting() {
        int newValue = this.isAutoEquipEnabled() ? 0 : 1;
        this.setData(DATA_AUTO_EQUIP_ENABLED, newValue);
    }

    private enum EquipSlotType {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS, MAIN_HAND, OFF_HAND
    }

    private static class EquipSlot extends Slot {
        private static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = 
            ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
        private static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = 
            ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
        private static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = 
            ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
        private static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = 
            ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
        private static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = 
            ResourceLocation.withDefaultNamespace("item/empty_armor_slot_shield");
        
        private final EquipSlotType slotType;

        public EquipSlot(Container container, int index, int x, int y, EquipSlotType slotType) {
            super(container, index, x, y);
            this.slotType = slotType;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return switch (this.slotType) {
                case HELMET -> stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.HELMET;
                case CHESTPLATE -> stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.CHESTPLATE;
                case LEGGINGS -> stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.LEGGINGS;
                case BOOTS -> stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.BOOTS;
                case MAIN_HAND -> isValidMainHandItem(stack);
                case OFF_HAND -> stack.getItem() instanceof ShieldItem;
            };
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return switch (this.slotType) {
                case HELMET -> Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_SLOT_HELMET);
                case CHESTPLATE -> Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_SLOT_CHESTPLATE);
                case LEGGINGS -> Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_SLOT_LEGGINGS);
                case BOOTS -> Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_SLOT_BOOTS);
                case OFF_HAND -> Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_SLOT_SHIELD);
                default -> null;
            };
        }

        @Override
        public int getMaxStackSize() {
            return this.slotType == EquipSlotType.MAIN_HAND ? 64 : 1;
        }
    }

    private static class CompanionSettingsData implements ContainerData {
        private final CompanionEntity companion;

        private CompanionSettingsData(CompanionEntity companion) {
            this.companion = companion;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_FOLLOW_ENABLED -> this.companion.isFollowEnabled() ? 1 : 0;
                case DATA_AUTO_HEAL_ENABLED -> this.companion.isAutoHealEnabled() ? 1 : 0;
                case DATA_AUTO_EQUIP_ENABLED -> this.companion.isAutoEquipEnabled() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            boolean enabled = value != 0;
            switch (index) {
                case DATA_FOLLOW_ENABLED -> this.companion.setFollowEnabled(enabled);
                case DATA_AUTO_HEAL_ENABLED -> this.companion.setAutoHealEnabled(enabled);
                case DATA_AUTO_EQUIP_ENABLED -> this.companion.setAutoEquipEnabled(enabled);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return SETTINGS_DATA_COUNT;
        }
    }
}
