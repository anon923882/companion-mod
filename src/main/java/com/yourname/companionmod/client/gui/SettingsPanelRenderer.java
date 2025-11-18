package com.yourname.companionmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;

final class SettingsPanelRenderer {
    private static final int CORNER_SIZE = 8;

    private SettingsPanelRenderer() {}

    static void render(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.blitNineSliced(CompanionGuiTextures.STORAGE_BACKGROUND_9, x, y, width, height,
            CORNER_SIZE, CORNER_SIZE, 256, 256, 0, 0);
    }
}
