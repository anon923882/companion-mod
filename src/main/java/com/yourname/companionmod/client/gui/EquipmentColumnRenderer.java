package com.yourname.companionmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class EquipmentColumnRenderer {
    private static final int COLUMN_WIDTH = 26;
    private static final ItemStack HELMET_ICON = new ItemStack(Items.IRON_HELMET);
    private static final ItemStack CHEST_ICON = new ItemStack(Items.IRON_CHESTPLATE);
    private static final ItemStack LEGS_ICON = new ItemStack(Items.IRON_LEGGINGS);
    private static final ItemStack BOOTS_ICON = new ItemStack(Items.IRON_BOOTS);
    private static final ItemStack MAIN_HAND_ICON = new ItemStack(Items.IRON_SWORD);
    private static final ItemStack OFF_HAND_ICON = new ItemStack(Items.SHIELD);

    private EquipmentColumnRenderer() {}

    static void render(GuiGraphics guiGraphics, int leftPos, int topPos, int slots) {
        int heightWithoutBottom = getHeightWithoutBottom(slots);
        int columnX = leftPos - CompanionGuiTextures.UPGRADE_INVENTORY_OFFSET;

        guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, columnX, topPos, 0, 0,
            COLUMN_WIDTH, 4, 256, 256);
        guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, columnX, topPos + 4, 0, 4,
            COLUMN_WIDTH - 1, Math.max(0, heightWithoutBottom - 4), 256, 256);
        guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, columnX, topPos + heightWithoutBottom, 0, 198,
            COLUMN_WIDTH - 1, CompanionGuiTextures.UPGRADE_BOTTOM_HEIGHT, 256, 256);
    }

    private static int getHeightWithoutBottom(int slots) {
        return CompanionGuiTextures.UPGRADE_BOTTOM_HEIGHT + slots * CompanionGuiTextures.UPGRADE_SLOT_HEIGHT;
    }

    static void renderIcons(GuiGraphics guiGraphics, Player player, int slotX, int slotY, int slotSpacing, int slots) {
        ItemStack[] fallbackIcons = new ItemStack[] {
            HELMET_ICON,
            CHEST_ICON,
            LEGS_ICON,
            BOOTS_ICON,
            MAIN_HAND_ICON,
            OFF_HAND_ICON
        };
        EquipmentSlot[] equipmentOrder = new EquipmentSlot[] {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND
        };
        int renderSlots = Math.min(slots, equipmentOrder.length);
        for (int i = 0; i < renderSlots; i++) {
            ItemStack icon = ItemStack.EMPTY;
            if (player != null) {
                icon = player.getItemBySlot(equipmentOrder[i]);
            }
            if (icon.isEmpty()) {
                icon = fallbackIcons[i];
            }
            if (!icon.isEmpty()) {
                guiGraphics.renderItem(icon, slotX + 1, slotY + 1 + i * slotSpacing);
            }
        }
    }
}
