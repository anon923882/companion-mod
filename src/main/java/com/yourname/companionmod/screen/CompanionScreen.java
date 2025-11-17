package com.yourname.companionmod.screen;

import com.yourname.companionmod.client.gui.CompanionGuiHelper;
import com.yourname.companionmod.client.gui.CompanionGuiTextures;
import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final int STORAGE_ROWS_PIXEL_HEIGHT = CompanionMenu.STORAGE_ROWS * CompanionGuiTextures.SLOT_SIZE;
    private static final int BASE_PANEL_HEIGHT = 114;

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = CompanionMenu.STORAGE_COLUMNS * CompanionGuiTextures.SLOT_SIZE + 14;
        this.imageHeight = BASE_PANEL_HEIGHT + STORAGE_ROWS_PIXEL_HEIGHT;
        this.titleLabelX = CompanionMenu.STORAGE_START_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = CompanionMenu.STORAGE_START_X;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 90;
        int buttonX = this.leftPos + this.imageWidth - buttonWidth - 10;
        int buttonY = this.topPos + 4;

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.companionmod.equip_best"),
            button -> this.sendEquipBestRequest())
            .bounds(buttonX, buttonY, buttonWidth, 20)
            .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        CompanionGuiHelper.renderStorageBackground(guiGraphics, x, y, this.imageWidth, STORAGE_ROWS_PIXEL_HEIGHT);

        CompanionGuiHelper.renderSlotArea(guiGraphics, x + CompanionMenu.STORAGE_START_X,
            y + CompanionMenu.STORAGE_START_Y, CompanionMenu.STORAGE_COLUMNS, CompanionMenu.STORAGE_ROWS);

        CompanionGuiHelper.renderSlotArea(guiGraphics, x + CompanionMenu.STORAGE_START_X,
            y + CompanionMenu.PLAYER_INVENTORY_START_Y, 9, 3);

        CompanionGuiHelper.renderSlotArea(guiGraphics, x + CompanionMenu.STORAGE_START_X,
            y + CompanionMenu.HOTBAR_Y, 9, 1);

        CompanionGuiHelper.renderEquipmentColumn(guiGraphics, this.leftPos, this.topPos,
            CompanionMenu.EQUIPMENT_SLOT_COUNT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void sendEquipBestRequest() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }

}
