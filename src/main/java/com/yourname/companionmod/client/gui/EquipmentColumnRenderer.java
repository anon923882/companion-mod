package com.yourname.companionmod.client.gui;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class EquipmentColumnRenderer {
    private static final int COLUMN_WIDTH = 26;

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
