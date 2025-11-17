package com.yourname.companionmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Utility routines that closely follow Sophisticated Backpacks' storage rendering pipeline.
 */
public final class CompanionGuiHelper {
    private CompanionGuiHelper() {}

    public static void renderStorageBackground(GuiGraphics guiGraphics, int leftPos, int topPos,
                                               int imageWidth, int storageHeight) {
        StorageBackgroundRenderer.render(guiGraphics, leftPos, topPos, imageWidth, storageHeight);
    }

    public static void renderSlotArea(GuiGraphics guiGraphics, int x, int y, int columns, int rows) {
        if (columns <= 0 || rows <= 0) {
            return;
        }
        int width = columns * CompanionGuiTextures.SLOT_SIZE;
        int height = rows * CompanionGuiTextures.SLOT_SIZE;
        guiGraphics.blit(CompanionGuiTextures.SLOTS_BACKGROUND, x, y, 0, 0, width, height, 256, 256);
    }

    public static void renderEquipmentColumn(GuiGraphics guiGraphics, int leftPos, int topPos, int slots) {
        if (slots <= 0) {
            return;
        }
        EquipmentColumnRenderer.render(guiGraphics, leftPos, topPos, slots);
    }
}
