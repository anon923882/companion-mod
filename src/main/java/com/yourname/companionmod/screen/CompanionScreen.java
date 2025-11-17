package com.yourname.companionmod.screen;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation GENERIC_CHEST_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private static final int CHEST_TEXTURE_WIDTH = 176;
    private static final int PLAYER_SECTION_HEIGHT = 95;
    private static final int EQUIPMENT_COLUMN_WIDTH = 30;
    private static final int SLOT_BACKGROUND_U = 7;
    private static final int SLOT_BACKGROUND_V = 17;
    private static final int SLOT_BACKGROUND_SIZE = 18;

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        int storageSectionHeight = getStorageSectionHeight();
        this.imageWidth = CHEST_TEXTURE_WIDTH + EQUIPMENT_COLUMN_WIDTH;
        this.imageHeight = storageSectionHeight + PLAYER_SECTION_HEIGHT;
        this.titleLabelX = CompanionMenu.STORAGE_START_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = CompanionMenu.STORAGE_START_X;
        this.inventoryLabelY = CompanionMenu.PLAYER_INVENTORY_START_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 90;
        int buttonX = this.leftPos + this.imageWidth - buttonWidth - 6;
        int buttonY = this.topPos + 6;

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

        guiGraphics.blit(GENERIC_CHEST_TEXTURE, x, y, 0, 0, CHEST_TEXTURE_WIDTH, this.imageHeight);
        this.renderEquipmentColumnBackground(guiGraphics, x, y);
        this.renderEquipmentSlotFrames(guiGraphics);
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

    private static int getStorageSectionHeight() {
        return 17 + CompanionMenu.STORAGE_ROWS * CompanionMenu.SLOT_SPACING;
    }

    private void renderEquipmentColumnBackground(GuiGraphics guiGraphics, int x, int y) {
        int extensionX = x + CHEST_TEXTURE_WIDTH;
        int availableWidth = this.imageWidth - CHEST_TEXTURE_WIDTH;
        if (availableWidth <= 0) {
            return;
        }

        int copyU = CHEST_TEXTURE_WIDTH - availableWidth;
        guiGraphics.blit(GENERIC_CHEST_TEXTURE, extensionX, y, copyU, 0, availableWidth, this.imageHeight);
    }

    private void renderEquipmentSlotFrames(GuiGraphics guiGraphics) {
        int slotX = this.leftPos + CompanionMenu.EQUIPMENT_COLUMN_X - 1;
        int armorStartY = this.topPos + CompanionMenu.EQUIPMENT_START_Y - 1;
        for (int i = 0; i < 4; i++) {
            int y = armorStartY + i * CompanionMenu.SLOT_SPACING;
            guiGraphics.blit(GENERIC_CHEST_TEXTURE, slotX, y, SLOT_BACKGROUND_U, SLOT_BACKGROUND_V,
                SLOT_BACKGROUND_SIZE, SLOT_BACKGROUND_SIZE);
        }

        int handStartY = this.topPos + CompanionMenu.HAND_SLOT_START_Y - 1;
        for (int i = 0; i < 2; i++) {
            int y = handStartY + i * CompanionMenu.SLOT_SPACING;
            guiGraphics.blit(GENERIC_CHEST_TEXTURE, slotX, y, SLOT_BACKGROUND_U, SLOT_BACKGROUND_V,
                SLOT_BACKGROUND_SIZE, SLOT_BACKGROUND_SIZE);
        }
    }
}
