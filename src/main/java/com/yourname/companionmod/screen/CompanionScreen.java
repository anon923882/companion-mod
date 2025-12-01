package com.yourname.companionmod.screen;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation STORAGE_BACKGROUND = 
        ResourceLocation.fromNamespaceAndPath("companionmod", "textures/gui/storage_background_9.png");
    private static final ResourceLocation SLOTS_BACKGROUND = 
        ResourceLocation.fromNamespaceAndPath("companionmod", "textures/gui/slots_background.png");
    private static final int GUI_WIDTH = 194;
    private static final int GUI_HEIGHT = 222;

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.titleLabelX = CompanionMenu.STORAGE_START_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = CompanionMenu.STORAGE_START_X;
        this.inventoryLabelY = CompanionMenu.PLAYER_INVENTORY_START_Y - 10;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        renderStorageBackground(guiGraphics, x, y);
        renderEquipSlotBackgrounds(guiGraphics, x, y);
    }
    
    private void renderStorageBackground(GuiGraphics guiGraphics, int x, int y) {
        int storageHeight = CompanionMenu.STORAGE_ROWS * 18 + 4;
        guiGraphics.blit(STORAGE_BACKGROUND, 
            x + CompanionMenu.STORAGE_START_X - 4, 
            y + CompanionMenu.STORAGE_START_Y - 4,
            0, 0, 
            CompanionMenu.STORAGE_COLUMNS * 18 + 8, 
            storageHeight, 
            256, 256);
        int playerInvY = y + CompanionMenu.PLAYER_INVENTORY_START_Y - 4;
        guiGraphics.blit(STORAGE_BACKGROUND,
            x + CompanionMenu.STORAGE_START_X - 4,
            playerInvY,
            0, 0,
            CompanionMenu.STORAGE_COLUMNS * 18 + 8,
            76,
            256, 256);
    }
    
    private void renderEquipSlotBackgrounds(GuiGraphics guiGraphics, int x, int y) {
        int slotX = x + CompanionMenu.EQUIP_COLUMN_X;
        int slotY = y + CompanionMenu.EQUIP_START_Y;
        for (int i = 0; i < CompanionMenu.EQUIP_SLOT_COUNT; i++) {
            guiGraphics.blit(SLOTS_BACKGROUND, 
                slotX - 1, slotY - 1, 
                0, 0, 
                18, 18, 
                256, 256);
            slotY += CompanionMenu.EQUIP_SLOT_SPACING;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
