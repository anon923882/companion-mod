package com.yourname.companionmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class SettingsPanelRenderer {
    private static final int CORNER_SIZE = 8;

    private SettingsPanelRenderer() {}

    static void render(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        blitNineSliced(guiGraphics, CompanionGuiTextures.STORAGE_BACKGROUND_9, x, y, width, height,
            CORNER_SIZE, CORNER_SIZE, 256, 256, 0, 0);
    }

    private static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y,
                                       int width, int height, int cornerWidth, int cornerHeight,
                                       int textureWidth, int textureHeight, int uOffset, int vOffset) {
        int innerWidth = Math.max(0, width - 2 * cornerWidth);
        int innerHeight = Math.max(0, height - 2 * cornerHeight);
        int textureInnerWidth = textureWidth - 2 * cornerWidth;
        int textureInnerHeight = textureHeight - 2 * cornerHeight;

        int rightX = x + width - cornerWidth;
        int bottomY = y + height - cornerHeight;
        int uRight = uOffset + textureWidth - cornerWidth;
        int vBottom = vOffset + textureHeight - cornerHeight;

        guiGraphics.blit(texture, x, y, uOffset, vOffset, cornerWidth, cornerHeight, textureWidth, textureHeight);
        guiGraphics.blit(texture, rightX, y, uRight, vOffset, cornerWidth, cornerHeight, textureWidth, textureHeight);
        guiGraphics.blit(texture, x, bottomY, uOffset, vBottom, cornerWidth, cornerHeight, textureWidth, textureHeight);
        guiGraphics.blit(texture, rightX, bottomY, uRight, vBottom, cornerWidth, cornerHeight, textureWidth, textureHeight);

        repeatHorizontal(guiGraphics, texture, x + cornerWidth, y, innerWidth, cornerHeight,
            uOffset + cornerWidth, vOffset, textureInnerWidth, cornerHeight, textureWidth, textureHeight);
        repeatHorizontal(guiGraphics, texture, x + cornerWidth, bottomY, innerWidth, cornerHeight,
            uOffset + cornerWidth, vBottom, textureInnerWidth, cornerHeight, textureWidth, textureHeight);

        repeatVertical(guiGraphics, texture, x, y + cornerHeight, cornerWidth, innerHeight,
            uOffset, vOffset + cornerHeight, cornerWidth, textureInnerHeight, textureWidth, textureHeight);
        repeatVertical(guiGraphics, texture, rightX, y + cornerHeight, cornerWidth, innerHeight,
            uRight, vOffset + cornerHeight, cornerWidth, textureInnerHeight, textureWidth, textureHeight);

        int drawnY = 0;
        while (drawnY < innerHeight) {
            int drawHeight = Math.min(textureInnerHeight, innerHeight - drawnY);
            int drawnX = 0;
            while (drawnX < innerWidth) {
                int drawWidth = Math.min(textureInnerWidth, innerWidth - drawnX);
                guiGraphics.blit(texture, x + cornerWidth + drawnX, y + cornerHeight + drawnY,
                    uOffset + cornerWidth, vOffset + cornerHeight, drawWidth, drawHeight, textureWidth, textureHeight);
                drawnX += drawWidth;
            }
            drawnY += drawHeight;
        }
    }

    private static void repeatHorizontal(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y,
                                         int width, int height, int u, int v, int segmentWidth, int segmentHeight,
                                         int textureWidth, int textureHeight) {
        int drawn = 0;
        while (drawn < width) {
            int drawWidth = Math.min(segmentWidth, width - drawn);
            guiGraphics.blit(texture, x + drawn, y, u, v, drawWidth, height, textureWidth, textureHeight);
            drawn += drawWidth;
        }
    }

    private static void repeatVertical(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y,
                                       int width, int height, int u, int v, int segmentWidth, int segmentHeight,
                                       int textureWidth, int textureHeight) {
        int drawn = 0;
        while (drawn < height) {
            int drawHeight = Math.min(segmentHeight, height - drawn);
            guiGraphics.blit(texture, x, y + drawn, u, v, width, drawHeight, textureWidth, textureHeight);
            drawn += drawHeight;
        }
    }
}
