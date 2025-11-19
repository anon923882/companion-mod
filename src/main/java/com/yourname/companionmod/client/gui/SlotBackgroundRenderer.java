package com.yourname.companionmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Ports Sophisticated Core's stitched slot tiling so empty slot plates look identical
 * to the backpack UI and can scale to arbitrary grid sizes without stretching.
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
        // Match Sophisticated Core's approach: draw at most 12 rows per pass to preserve the
        // stitched edges from the texture without scaling artifacts.
        for (int currentY = y, remainingRows = rows; remainingRows > 0; currentY += 12 * CompanionGuiTextures.SLOT_SIZE) {
            int renderRows = Math.min(remainingRows, 12);
            int width = slotsInRow * CompanionGuiTextures.SLOT_SIZE;
            int height = renderRows * CompanionGuiTextures.SLOT_SIZE;
            guiGraphics.blit(CompanionGuiTextures.SLOTS_BACKGROUND, x, currentY, 0, 0, width, height, 256, 256);
            remainingRows -= renderRows;
        }
    }
}
