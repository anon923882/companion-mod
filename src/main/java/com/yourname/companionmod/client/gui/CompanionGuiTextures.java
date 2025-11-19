package com.yourname.companionmod.client.gui;

import net.minecraft.resources.ResourceLocation;

/**
 * Minimal subset of the Sophisticated Backpacks GUI helpers that the companion screen can reuse.
 */
public final class CompanionGuiTextures {
    public static final int SLOT_SIZE = 18;
    public static final int STORAGE_X_OFFSET = 7;
    public static final int STORAGE_Y_OFFSET = 17;
    public static final int UPGRADE_INVENTORY_OFFSET = 21;
    public static final int UPGRADE_SLOT_HEIGHT = 16;
    public static final int UPGRADE_TOP_HEIGHT = 7;
    public static final int UPGRADE_BOTTOM_HEIGHT = 6;

    private static final String COMPANION_MOD = "companionmod";

    public static final ResourceLocation STORAGE_BACKGROUND_9 = ResourceLocation.fromNamespaceAndPath(
        COMPANION_MOD,
        "textures/gui/storage_background_9.png");
    public static final ResourceLocation GUI_CONTROLS = ResourceLocation.fromNamespaceAndPath(
        COMPANION_MOD,
        "textures/gui/gui_controls.png");
    public static final ResourceLocation SLOTS_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
        COMPANION_MOD,
        "textures/gui/slots_background.png");

    private CompanionGuiTextures() {}
}
