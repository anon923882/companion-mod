package com.example.companionmod.content.companion;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.DamageTypeTags;

public class CompanionEquipmentHandler {
    private final LivingEntity owner;
    private final CompanionInventory inventory;

    public CompanionEquipmentHandler(LivingEntity owner, CompanionInventory inventory) {
        this.owner = owner;
        this.inventory = inventory;
    }

    public void syncAllToEntity() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            pushSlotToEquipment(slot);
        }
    }

    public void pushSlotToEquipment(EquipmentSlot equipmentSlot) {
        int index = slotIndexFor(equipmentSlot);
        if (index >= 0) {
            ItemStack stack = inventory.getItem(index);
            ItemStack previous = owner.getItemBySlot(equipmentSlot);
            if (stacksDiffer(previous, stack)) {
                owner.setItemSlot(equipmentSlot, stack.copy());
            }
        }
    }

    public void pullAllFromEntity() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            pullSlotToInventory(slot);
        }
    }

    public void pullSlotToInventory(EquipmentSlot equipmentSlot) {
        int index = slotIndexFor(equipmentSlot);
        if (index >= 0) {
            ItemStack equipped = owner.getItemBySlot(equipmentSlot);
            ItemStack stored = inventory.getItem(index);
            if (stacksDiffer(stored, equipped)) {
                inventory.setItem(index, equipped.copy());
            }
        }
    }

    public void hurtArmor(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) {
            return;
        }
        float durabilityDamage = amount / 4.0F;
        if (durabilityDamage < 1.0F) {
            durabilityDamage = 1.0F;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            ItemStack stack = owner.getItemBySlot(slot);
            if (stack.isDamageableItem()) {
                stack.hurtAndBreak((int) durabilityDamage, owner, slot);
            }
        }
        pullAllFromEntity();
    }

    public void syncEquipmentSlot(EquipmentSlot slot) {
        pushSlotToEquipment(slot);
    }

    private static boolean stacksDiffer(ItemStack first, ItemStack second) {
        if (first.isEmpty() && second.isEmpty()) {
            return false;
        }
        if (first.isEmpty() != second.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(first, second)) {
            return true;
        }
        if (first.getCount() != second.getCount()) {
            return true;
        }
        return first.getDamageValue() != second.getDamageValue();
    }

    private static int slotIndexFor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> CompanionInventory.HEAD_SLOT;
            case CHEST -> CompanionInventory.CHEST_SLOT;
            case LEGS -> CompanionInventory.LEGS_SLOT;
            case FEET -> CompanionInventory.FEET_SLOT;
            case MAINHAND -> CompanionInventory.MAINHAND_SLOT;
            default -> -1;
        };
    }
}
