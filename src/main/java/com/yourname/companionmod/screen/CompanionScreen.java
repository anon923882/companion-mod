package com.yourname.companionmod.screen;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation GENERIC_CONTAINER_TEXTURE =
        ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int CHEST_TEXTURE_WIDTH = 176;
    private static final int PLAYER_SECTION_HEIGHT = 96;
    private static final int EQUIPMENT_PANEL_WIDTH = 28;
    private static final int EQUIPMENT_PANEL_BORDER = 0xFF37291B;
    private static final int EQUIPMENT_PANEL_BACKGROUND = 0xFF27190F;
    private static final int SLOT_OUTLINE = 0xFF000000;
    private static final int SLOT_FILL = 0xFFC6C6C6;

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        int storageSectionHeight = getStorageSectionHeight();
        this.imageWidth = CHEST_TEXTURE_WIDTH + EQUIPMENT_PANEL_WIDTH;
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

        renderChestBackground(guiGraphics, x, y);
        renderEquipmentColumn(guiGraphics, x, y);
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

        for (int i = 0; i < 4; i++) {
            int slotY = originY + CompanionMenu.EQUIPMENT_START_Y - 1 + i * slotSize;
            renderSlot(guiGraphics, columnX, slotY);
        }

        int mainHandY = originY + CompanionMenu.HAND_SLOT_START_Y - 1;
        renderSlot(guiGraphics, columnX, mainHandY);
        renderSlot(guiGraphics, columnX, mainHandY + slotSize);
    }

    private void renderChestBackground(GuiGraphics guiGraphics, int x, int y) {
        int storageSectionHeight = getStorageSectionHeight();
        guiGraphics.blit(GENERIC_CONTAINER_TEXTURE, x, y, 0, 0, CHEST_TEXTURE_WIDTH, storageSectionHeight);
        guiGraphics.blit(GENERIC_CONTAINER_TEXTURE, x, y + storageSectionHeight, 0, 126, CHEST_TEXTURE_WIDTH,
            PLAYER_SECTION_HEIGHT);
    }

    private void renderEquipmentColumn(GuiGraphics guiGraphics, int originX, int originY) {
        int panelX = originX + CHEST_TEXTURE_WIDTH;
        int panelRight = panelX + (this.imageWidth - CHEST_TEXTURE_WIDTH);

        guiGraphics.fill(panelX, originY, panelRight, originY + this.imageHeight, EQUIPMENT_PANEL_BORDER);
        guiGraphics.fill(panelX + 1, originY + 1, panelRight - 1, originY + this.imageHeight - 1,
            EQUIPMENT_PANEL_BACKGROUND);

        renderEquipmentSlotBackdrops(guiGraphics, originX, originY);
    }

    private static void renderSlot(GuiGraphics guiGraphics, int slotX, int slotY) {
        guiGraphics.fill(slotX, slotY, slotX + CompanionMenu.SLOT_SPACING, slotY + CompanionMenu.SLOT_SPACING,
            SLOT_OUTLINE);
        guiGraphics.fill(slotX + 1, slotY + 1, slotX + CompanionMenu.SLOT_SPACING - 1,
            slotY + CompanionMenu.SLOT_SPACING - 1, SLOT_FILL);
    }

    private static int getStorageSectionHeight() {
        return 17 + CompanionMenu.STORAGE_ROWS * CompanionMenu.SLOT_SPACING;
    }
}
