package com.yourname.companionmod.screen;

import com.yourname.companionmod.client.CompanionTextures;
import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation TEXTURE = CompanionTextures.GUI;

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
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Position button below companion storage, above player inventory, right-aligned
        int buttonX = x + this.imageWidth - 90;
        int buttonY = y + CompanionMenu.STORAGE_START_Y + CompanionMenu.STORAGE_ROWS * CompanionMenu.SLOT_SPACING + 8;
        
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.companionmod.equip_best"),
            button -> this.sendEquipBestRequest())
            .bounds(buttonX, buttonY, 80, 20)
            .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
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

    private static void renderSlot(GuiGraphics guiGraphics, int slotX, int slotY) {
        guiGraphics.fill(slotX, slotY, slotX + CompanionMenu.SLOT_SPACING, slotY + CompanionMenu.SLOT_SPACING, 0xFF2B2B2B);
        guiGraphics.fill(slotX + 1, slotY + 1, slotX + CompanionMenu.SLOT_SPACING - 1,
            slotY + CompanionMenu.SLOT_SPACING - 1, 0xFF8B8B8B);
    }
}
