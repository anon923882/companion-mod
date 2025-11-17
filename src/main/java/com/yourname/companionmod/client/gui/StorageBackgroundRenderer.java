package com.yourname.companionmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;

final class StorageBackgroundRenderer {
    private StorageBackgroundRenderer() {}

    static void render(GuiGraphics guiGraphics, int x, int y, int imageWidth, int storageHeight) {
        int slotsTopBottomHeight = Math.min(storageHeight / 2, 150);
        int yOffset = 0;

        guiGraphics.blit(CompanionGuiTextures.STORAGE_BACKGROUND_9, x, y, 0, 0,
            imageWidth, CompanionGuiTextures.STORAGE_Y_OFFSET + slotsTopBottomHeight, 256, 256);

        if (storageHeight / 2 > 150) {
            int middleHeight = (storageHeight / 2 - 150) * 2;
            guiGraphics.blit(CompanionGuiTextures.STORAGE_BACKGROUND_9, x,
                y + CompanionGuiTextures.STORAGE_Y_OFFSET + slotsTopBottomHeight, 0,
                CompanionGuiTextures.STORAGE_Y_OFFSET, imageWidth, middleHeight, 256, 256);
            yOffset = middleHeight;
        }

        int playerInventoryHeight = 97;
        guiGraphics.blit(CompanionGuiTextures.STORAGE_BACKGROUND_9, x,
            y + yOffset + CompanionGuiTextures.STORAGE_Y_OFFSET + slotsTopBottomHeight, 0,
            256 - (playerInventoryHeight + slotsTopBottomHeight),
            imageWidth, playerInventoryHeight + slotsTopBottomHeight, 256, 256);
    }
}
