package com.example.companionmod.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
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
        this.companion.onMenuOpened(playerInventory.player);

        addCompanionSlots();
        addPlayerInventory(playerInventory);
    }

    public CompanionEntity getCompanion() {
        return companion;
    }

    public net.minecraft.network.chat.Component getOwnerLabel() {
        net.minecraft.network.chat.Component ownerName = companion.getOwnerNameComponent();
        if (ownerName != null) {
            return net.minecraft.network.chat.Component.translatable("menu." + com.example.companionmod.CompanionMod.MOD_ID + ".owner_label", ownerName);
        }
        return null;
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
        EquipmentSlot[] equipmentOrder = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET,
                EquipmentSlot.MAINHAND
        };

        for (int i = 0; i < equipmentOrder.length; i++) {
            EquipmentSlot equipmentSlot = equipmentOrder[i];
            int x = firstRowXs[i];
            int inventoryIndex = slotIndex++;
            this.addSlot(new Slot(inventory, inventoryIndex, x, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (equipmentSlot == EquipmentSlot.MAINHAND) {
                        return true;
                    }
                    return stack.canEquip(equipmentSlot, companion);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    companion.syncEquipmentSlot(equipmentSlot);
                }
            }.setBackground(InventoryMenu.BLOCK_ATLAS, getEquipmentSlotTexture(equipmentSlot)));
        }

        int[] rows = {40, 58, 76, 98};
        for (int rowY : rows) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, slotIndex++, left + column * 18, rowY));
            }
        }
    }

    private static net.minecraft.resources.ResourceLocation getEquipmentSlotTexture(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
            case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
            case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
            case FEET -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
            case MAINHAND -> InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
            default -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
        };
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
        return this.companion != null && this.companion.isWithinUsableDistance(player) && this.companion.isOwnedBy(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
        this.companion.onMenuClosed(player);
    }
}
