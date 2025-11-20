package com.yourname.companionmod.client.gui;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class EquipmentColumnRenderer {
    private static final int COLUMN_WIDTH = 26;

    private EquipmentColumnRenderer() {}

    static void render(GuiGraphics guiGraphics, int leftPos, int topPos, int slots) {
        int columnX = leftPos - CompanionGuiTextures.UPGRADE_INVENTORY_OFFSET;
        int columnBodyHeight = slots * CompanionGuiTextures.UPGRADE_SLOT_HEIGHT;
        int middleY = topPos + CompanionGuiTextures.UPGRADE_TOP_HEIGHT;
        int bottomY = middleY + columnBodyHeight;

        // Draw the stitched upgrade column exactly like Sophisticated Backpacks: a fixed-height
        // cap, a repeated middle section aligned to 16px slot spacing, and a fixed bottom cap.
        guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, columnX, topPos, 0, 0,
            COLUMN_WIDTH, CompanionGuiTextures.UPGRADE_TOP_HEIGHT, 256, 256);
        if (columnBodyHeight > 0) {
            guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, columnX, middleY, 0,
                CompanionGuiTextures.UPGRADE_TOP_HEIGHT, COLUMN_WIDTH,
                columnBodyHeight, 256, 256);
        }
        guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, columnX, bottomY, 0, 198,
            COLUMN_WIDTH, CompanionGuiTextures.UPGRADE_BOTTOM_HEIGHT, 256, 256);
    }

    static void renderIcons(GuiGraphics guiGraphics, CompanionMenu menu, int slotX, int slotY, int slotSpacing, int slots) {
        int renderSlots = Math.min(slots, CompanionMenu.EQUIPMENT_SLOT_COUNT);
        for (int i = 0; i < renderSlots; i++) {
            Slot menuSlot = menu.getSlot(CompanionMenu.getEquipmentSlotIndex(i));
            ItemStack stack = menuSlot != null ? menuSlot.getItem() : ItemStack.EMPTY;
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, slotX + 1, slotY + 1 + i * slotSpacing);
            }
        }
    }
}
