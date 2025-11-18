package com.yourname.companionmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Reuses Sophisticated Core's slot background tiling logic so stitched slot borders
 * can be rendered for arbitrary grid sizes without stretching artifacts.
 */
final class SlotBackgroundRenderer {
    private SlotBackgroundRenderer() {}

    static void render(GuiGraphics guiGraphics, int x, int y, int slotsInRow, int fullRows) {
        render(guiGraphics, x, y, slotsInRow, fullRows, 0);
    }

    static void render(GuiGraphics guiGraphics, int x, int y, int slotsInRow, int fullRows, int extraRowSlots) {
        renderRows(guiGraphics, x, y, slotsInRow, fullRows);
        if (extraRowSlots > 0) {
            renderRows(guiGraphics, x, y + fullRows * CompanionGuiTextures.SLOT_SIZE, extraRowSlots, 1);
        }
    }

    private static void renderRows(GuiGraphics guiGraphics, int x, int y, int slotsInRow, int rows) {
        if (slotsInRow <= 0 || rows <= 0) {
            return;
        }
        int width = slotsInRow * CompanionGuiTextures.SLOT_SIZE;
        int height = rows * CompanionGuiTextures.SLOT_SIZE;
        guiGraphics.blit(CompanionGuiTextures.SLOTS_BACKGROUND, x, y, 0, 0, width, height, 256, 256);
    }
}
