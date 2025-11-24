package com.yourname.companionmod.client.gui;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class EquipmentColumnRenderer {
    private static final int COLUMN_WIDTH = 26;
    private static final int MIDDLE_WIDTH = 25;
    private static final int COLUMN_TOP_HEIGHT = 4;

    private EquipmentColumnRenderer() {}

    static void render(GuiGraphics guiGraphics, int leftPos, int topPos, int slots) {
        int columnX = leftPos - CompanionGuiTextures.UPGRADE_INVENTORY_OFFSET;
        int heightWithoutBottom = slots * CompanionGuiTextures.UPGRADE_SLOT_HEIGHT
            + CompanionGuiTextures.UPGRADE_BOTTOM_HEIGHT;
        int bottomY = topPos + heightWithoutBottom;

        guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, columnX, topPos, 0, 0,
            COLUMN_WIDTH, COLUMN_TOP_HEIGHT, 256, 256);
        guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, columnX, topPos + COLUMN_TOP_HEIGHT, 0, COLUMN_TOP_HEIGHT,
            MIDDLE_WIDTH, heightWithoutBottom - COLUMN_TOP_HEIGHT, 256, 256);
        guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, columnX, bottomY, 0, 198,
            MIDDLE_WIDTH, CompanionGuiTextures.UPGRADE_BOTTOM_HEIGHT, 256, 256);
    }

    static void renderIcons(GuiGraphics guiGraphics, CompanionMenu menu, int slotX, int slotY, int slotSpacing, int slots) {
        int renderSlots = Math.min(slots, CompanionMenu.EQUIP_SLOT_COUNT);
        for (int i = 0; i < renderSlots; i++) {
            Slot menuSlot = menu.getSlot(CompanionMenu.getEquipSlotIndex(i));
            ItemStack stack = menuSlot != null ? menuSlot.getItem() : ItemStack.EMPTY;
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, slotX + 1, slotY + 1 + i * slotSpacing);
            }
        }
    }
}
