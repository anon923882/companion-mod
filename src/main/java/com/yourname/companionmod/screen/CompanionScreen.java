package com.yourname.companionmod.screen;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final int PANEL_OUTLINE_COLOR = 0xFF000000;
    private static final int PANEL_BACKGROUND_COLOR = 0xFF151515;

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 222;
        this.imageHeight = 222;
        this.titleLabelX = CompanionMenu.STORAGE_START_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = CompanionMenu.STORAGE_START_X;
        this.inventoryLabelY = CompanionMenu.PLAYER_INVENTORY_START_Y - 12;
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

        renderPanelBackground(guiGraphics, x, y, this.imageWidth, this.imageHeight);

        renderSlotPanel(guiGraphics, x + CompanionMenu.STORAGE_START_X,
            y + CompanionMenu.STORAGE_START_Y, CompanionMenu.STORAGE_COLUMNS, CompanionMenu.STORAGE_ROWS);

        renderSlotPanel(guiGraphics, x + CompanionMenu.STORAGE_START_X,
            y + CompanionMenu.PLAYER_INVENTORY_START_Y, 9, 3);

        renderSlotPanel(guiGraphics, x + CompanionMenu.STORAGE_START_X,
            y + CompanionMenu.HOTBAR_Y, 9, 1);

        renderSlotPanel(guiGraphics, x + CompanionMenu.EQUIPMENT_COLUMN_X,
            y + CompanionMenu.EQUIPMENT_START_Y, 1, 4);

        renderSlotPanel(guiGraphics, x + CompanionMenu.EQUIPMENT_COLUMN_X,
            y + CompanionMenu.HAND_SLOT_START_Y, 1, 2);

        this.renderEquipmentSlotBackdrops(guiGraphics, x, y);
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

    private void renderEquipmentSlotBackdrops(GuiGraphics guiGraphics, int originX, int originY) {
        int columnX = originX + CompanionMenu.EQUIPMENT_COLUMN_X - 1;
        int slotSize = CompanionMenu.SLOT_SPACING;
        
        // Render armor slot backdrops
        for (int i = 0; i < 4; i++) {
            int slotY = originY + CompanionMenu.EQUIPMENT_START_Y - 1 + i * slotSize;
            renderSlot(guiGraphics, columnX, slotY);
        }

        // Render hand slot backdrops
        int mainHandY = originY + CompanionMenu.HAND_SLOT_START_Y - 1;
        renderSlot(guiGraphics, columnX, mainHandY);
        renderSlot(guiGraphics, columnX, mainHandY + slotSize);
    }

    private static void renderPanelBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_OUTLINE_COLOR);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_BACKGROUND_COLOR);
    }

    private static void renderSlotPanel(GuiGraphics guiGraphics, int startX, int startY, int columns, int rows) {
        int totalWidth = columns * CompanionMenu.SLOT_SPACING;
        int totalHeight = rows * CompanionMenu.SLOT_SPACING;

        guiGraphics.fill(startX - 3, startY - 3, startX + totalWidth + 3, startY + totalHeight + 3, PANEL_OUTLINE_COLOR);
        guiGraphics.fill(startX - 2, startY - 2, startX + totalWidth + 2, startY + totalHeight + 2, PANEL_BACKGROUND_COLOR);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotX = startX + col * CompanionMenu.SLOT_SPACING - 1;
                int slotY = startY + row * CompanionMenu.SLOT_SPACING - 1;
                renderSlot(guiGraphics, slotX, slotY);
            }
        }
    }

    private static void renderSlot(GuiGraphics guiGraphics, int slotX, int slotY) {
        guiGraphics.fill(slotX, slotY, slotX + CompanionMenu.SLOT_SPACING, slotY + CompanionMenu.SLOT_SPACING, 0xFF2B2B2B);
        guiGraphics.fill(slotX + 1, slotY + 1, slotX + CompanionMenu.SLOT_SPACING - 1,
            slotY + CompanionMenu.SLOT_SPACING - 1, 0xFF8B8B8B);
    }
}
