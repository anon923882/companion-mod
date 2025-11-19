package com.yourname.companionmod.client.gui;

import com.yourname.companionmod.menu.CompanionMenu;
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
        renderSlotArea(guiGraphics, x, y, columns, rows, 0);
    }

    public static void renderSlotArea(GuiGraphics guiGraphics, int x, int y, int columns, int rows, int extraSlots) {
        if (columns <= 0 || (rows <= 0 && extraSlots <= 0)) {
            return;
        }
        SlotBackgroundRenderer.render(guiGraphics, x, y, columns, rows, extraSlots);
    }

    public static void renderEquipmentColumn(GuiGraphics guiGraphics, int leftPos, int topPos, int slots) {
        if (slots <= 0) {
            return;
        }
        EquipmentColumnRenderer.render(guiGraphics, leftPos, topPos, slots);
    }

    public static void renderEquipmentIcons(GuiGraphics guiGraphics, CompanionMenu menu,
                                            int slotX, int slotY, int slotSpacing, int slots) {
        if (slots <= 0) {
            return;
        }
        EquipmentColumnRenderer.renderIcons(guiGraphics, menu, slotX, slotY, slotSpacing, slots);
    }

    public static void renderSettingsPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        SettingsPanelRenderer.render(guiGraphics, x, y, width, height);
    }
}
