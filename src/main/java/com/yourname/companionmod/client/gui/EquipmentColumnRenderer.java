package com.yourname.companionmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;

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
}
