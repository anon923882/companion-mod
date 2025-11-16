package com.yourname.companionmod.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;

public class CompanionMenu extends AbstractContainerMenu {
    private final Container companionInventory;
    private final Container equipmentInventory;
    private static final int COMPANION_INVENTORY_SIZE = 27;
    private static final int EQUIPMENT_SIZE = 6;
    private static final int TOTAL_COMPANION_SLOTS = COMPANION_INVENTORY_SIZE + EQUIPMENT_SIZE;

    // Client constructor
    public CompanionMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(COMPANION_INVENTORY_SIZE), new SimpleContainer(EQUIPMENT_SIZE));
    }

    // Server constructor
    public CompanionMenu(int containerId, Inventory playerInventory, Container companionInventory, Container equipmentInventory) {
        super(ModMenuTypes.COMPANION_MENU.get(), containerId);
        this.companionInventory = companionInventory;
        this.equipmentInventory = equipmentInventory;

        // Armor slots (head, chest, legs, feet)
        int baseX = 8;
        int baseY = 18;
        EquipmentSlot[] armorOrder = new EquipmentSlot[] {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
        };

        for (int i = 0; i < armorOrder.length; i++) {
            EquipmentSlot slot = armorOrder[i];
            int slotY = baseY + i * 18;
            this.addSlot(new EquipmentRestrictedSlot(equipmentInventory, i, baseX - 26, slotY, slot));
        }

        // Weapon/offhand slots
        this.addSlot(new EquipmentRestrictedSlot(equipmentInventory, 4, baseX - 26, baseY + 4 * 18 + 4,
            EquipmentSlot.MAINHAND));
        this.addSlot(new EquipmentRestrictedSlot(equipmentInventory, 5, baseX - 26, baseY + 5 * 18 + 4,
            EquipmentSlot.OFFHAND));

        // Companion inventory slots (3x9 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(companionInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar (1x9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < TOTAL_COMPANION_SLOTS) {
                // Moving from companion inventory to player inventory
                if (!this.moveItemStackTo(slotStack, TOTAL_COMPANION_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to companion inventory
                EquipmentSlot targetSlot = Mob.getEquipmentSlotForItem(slotStack);
                int equipmentIndex = this.getEquipmentIndex(targetSlot);

                if (equipmentIndex >= 0 && !this.slots.get(equipmentIndex).hasItem()) {
                    if (!this.moveItemStackTo(slotStack, equipmentIndex, equipmentIndex + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, 0, COMPANION_INVENTORY_SIZE, false)) {
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

    private int getEquipmentIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> COMPANION_INVENTORY_SIZE + 0;
            case CHEST -> COMPANION_INVENTORY_SIZE + 1;
            case LEGS -> COMPANION_INVENTORY_SIZE + 2;
            case FEET -> COMPANION_INVENTORY_SIZE + 3;
            case MAINHAND -> COMPANION_INVENTORY_SIZE + 4;
            case OFFHAND -> COMPANION_INVENTORY_SIZE + 5;
            default -> -1;
        };
    }

    private static class EquipmentRestrictedSlot extends Slot {
        private final EquipmentSlot slotType;

        public EquipmentRestrictedSlot(Container container, int index, int x, int y, EquipmentSlot slotType) {
            super(container, index, x, y);
            this.slotType = slotType;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (this.slotType.isArmor()) {
                return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == this.slotType;
            }
            if (this.slotType == EquipmentSlot.MAINHAND || this.slotType == EquipmentSlot.OFFHAND) {
                return true;
            }
            EquipmentSlot itemSlot = Mob.getEquipmentSlotForItem(stack);
            return itemSlot == this.slotType;
        }
    }
}