package com.yourname.companionmod.screen;

import com.yourname.companionmod.client.gui.CompanionGuiTextures;
import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final int BORDER_X = 8;
    private static final int BORDER_Y = 18;
    private static final int SETTINGS_BUTTON_SIZE = 20;
    private static final int SETTINGS_BUTTON_U = 0; // update based on icon pixel location in icons.png
    private static final int SETTINGS_BUTTON_V = 64;

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176; // should exactly match your backpack_27.png width
        this.imageHeight = 222; // should match your backpack_27.png height
        this.titleLabelX = BORDER_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = BORDER_X;
        this.inventoryLabelY = /* adjust Y for your player inventory text as needed */ BORDER_Y + 3 * 18 + 14;
    }

    private Button settingsButton;

    @Override
    protected void init() {
        super.init();
        int buttonX = this.leftPos + imageWidth - SETTINGS_BUTTON_SIZE - BORDER_X;
        int buttonY = this.topPos + BORDER_Y / 2;
        this.settingsButton = new Button(buttonX, buttonY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE, Component.literal(""), btn -> {}, b -> Component.empty()) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(CompanionGuiTextures.ICONS, buttonX, buttonY, SETTINGS_BUTTON_U, SETTINGS_BUTTON_V, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE, 256, 256);
            }
        };
        this.addRenderableWidget(this.settingsButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(CompanionGuiTextures.GUI_BACKGROUND, x, y, 0, 0, imageWidth, imageHeight);
        // No slot-by-slot background blit: all grid backgrounds should be baked into PNG
        renderEquipmentSlotBackgrounds(guiGraphics);
    }

    private void renderEquipmentSlotBackgrounds(GuiGraphics guiGraphics) {
        // Add overlays as needed. Otherwise, let PNG handle all backgrounds.
        // This method should be customized only for highlight/equip overlays, not base slot grid.
    }
}
