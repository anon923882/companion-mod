package com.yourname.companionmod.menu;

import com.yourname.companionmod.entity.custom.CompanionEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CompanionMenu extends AbstractContainerMenu {
    private final Container companionInventory;
    private final Container equipmentInventory;
    private final CompanionEntity companion;
    private static final int COMPANION_INVENTORY_SIZE = 54;
    private static final int EQUIPMENT_SIZE = 6;
    private static final int TOTAL_COMPANION_SLOTS = COMPANION_INVENTORY_SIZE + EQUIPMENT_SIZE;
    private final DataSlot attackHostiles = DataSlot.standalone();
    private final DataSlot attackPassives = DataSlot.standalone();
    private final DataSlot pickupXp = DataSlot.standalone();

    // Client constructor
    public CompanionMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, resolveCompanion(playerInventory, buf));
    }

    // Server constructor
    public CompanionMenu(int containerId, Inventory playerInventory, CompanionEntity companion) {
        super(ModMenuTypes.COMPANION_MENU.get(), containerId);
        this.companion = companion;
        this.companionInventory = companion != null ? companion.getInventory() : new SimpleContainer(COMPANION_INVENTORY_SIZE);
        this.equipmentInventory = companion != null ? companion.getEquipmentContainer() : new SimpleContainer(EQUIPMENT_SIZE);
        this.addDataSlot(this.attackHostiles);
        this.addDataSlot(this.attackPassives);
        this.addDataSlot(this.pickupXp);
        this.refreshTrackedFlags();

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

        // Companion inventory slots (6x9 grid)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(companionInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory (3x9) aligned to the container texture's lower grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Player hotbar (1x9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
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
                EquipmentSlot targetSlot = this.inferSlot(slotStack);
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
        return this.companion != null
            ? !this.companion.isRemoved() && player.distanceToSqr(this.companion) <= 64.0D
            : this.companionInventory.stillValid(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.companion == null || this.companion.level().isClientSide) {
            return false;
        }

        switch (id) {
            case 0 -> this.companion.setAttackHostiles(!this.companion.isAttackingHostiles());
            case 1 -> this.companion.setAttackPassives(!this.companion.isAttackingPassives());
            case 2 -> this.companion.setPickupXp(!this.companion.isPickingUpXp());
            default -> {
                return false;
            }
        }

        this.refreshTrackedFlags();
        return true;
    }

    public boolean isAttackingHostiles() {
        return this.attackHostiles.get() == 1;
    }

    public boolean isAttackingPassives() {
        return this.attackPassives.get() == 1;
    }

    public boolean isPickingUpXp() {
        return this.pickupXp.get() == 1;
    }

    public int getCompanionRows() {
        return COMPANION_INVENTORY_SIZE / 9;
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

    private EquipmentSlot inferSlot(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getEquipmentSlot();
        }
        if (stack.is(Items.SHIELD)) {
            return EquipmentSlot.OFFHAND;
        }
        return EquipmentSlot.MAINHAND;
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
            EquipmentSlot itemSlot = this.inferSlot(stack);
            return itemSlot == this.slotType;
        }

        private EquipmentSlot inferSlot(ItemStack stack) {
            if (stack.getItem() instanceof ArmorItem armor) {
                return armor.getEquipmentSlot();
            }
            if (stack.is(Items.SHIELD)) {
                return EquipmentSlot.OFFHAND;
            }
            return EquipmentSlot.MAINHAND;
        }
    }

    private void refreshTrackedFlags() {
        if (this.companion != null) {
            this.attackHostiles.set(this.companion.isAttackingHostiles() ? 1 : 0);
            this.attackPassives.set(this.companion.isAttackingPassives() ? 1 : 0);
            this.pickupXp.set(this.companion.isPickingUpXp() ? 1 : 0);
        }
    }

    private static CompanionEntity resolveCompanion(Inventory playerInventory, FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        return playerInventory.player.level().getEntity(entityId) instanceof CompanionEntity companion
            ? companion
            : null;
    }
}