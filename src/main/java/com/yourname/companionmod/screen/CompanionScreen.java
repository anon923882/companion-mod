package com.yourname.companionmod.screen;

import com.yourname.companionmod.client.gui.CompanionGuiHelper;
import com.yourname.companionmod.client.gui.CompanionGuiTextures;
import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;
    private static final int SLOT_SIZE = CompanionGuiTextures.SLOT_SIZE;
    private static final int BORDER_X = 8;
    private static final int BORDER_Y = 18;
    private static final int PLAYER_INVENTORY_OFFSET = 14; // vertical separation
    
    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = BORDER_X * 2 + INVENTORY_COLUMNS * SLOT_SIZE;
        this.imageHeight = BORDER_Y * 2 + INVENTORY_ROWS * SLOT_SIZE + PLAYER_INVENTORY_OFFSET + 3 * SLOT_SIZE;
        this.titleLabelX = BORDER_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = BORDER_X;
        this.inventoryLabelY = BORDER_Y * 2 + INVENTORY_ROWS * SLOT_SIZE + 2;
    }

    private Button settingsButton;

    @Override
    protected void init() {
        super.init();
        int buttonX = this.leftPos + this.imageWidth - SLOT_SIZE - BORDER_X;
        int buttonY = this.topPos + BORDER_Y / 2;
        this.settingsButton = new SettingsButton(buttonX, buttonY, btn -> {});
        this.addRenderableWidget(this.settingsButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(CompanionGuiTextures.GUI_BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight);
        CompanionGuiHelper.renderSlotArea(guiGraphics, x + BORDER_X, y + BORDER_Y, INVENTORY_COLUMNS, INVENTORY_ROWS);
        CompanionGuiHelper.renderSlotArea(guiGraphics, x + BORDER_X, y + BORDER_Y + INVENTORY_ROWS * SLOT_SIZE + PLAYER_INVENTORY_OFFSET, INVENTORY_COLUMNS, 3); // player inventory
        CompanionGuiHelper.renderEquipmentColumn(guiGraphics, x, y, CompanionMenu.EQUIPMENT_SLOT_COUNT);
        this.renderEquipmentSlotBackgrounds(guiGraphics);
    }

    private void renderEquipmentSlotBackgrounds(GuiGraphics guiGraphics) {
        for (int slotOffset = 0; slotOffset < CompanionMenu.EQUIPMENT_SLOT_COUNT; slotOffset++) {
            Slot slot = this.menu.getSlot(CompanionMenu.getEquipmentSlotIndex(slotOffset));
            int slotX = this.leftPos + slot.x - 1;
            int slotY = this.topPos + slot.y - 1;
            CompanionGuiHelper.renderSingleSlot(guiGraphics, slotX, slotY);
        }
    }

    private static class SettingsButton extends Button {
        protected SettingsButton(int x, int y, OnPress onPress) {
            super(x, y, SLOT_SIZE, SLOT_SIZE, Component.literal(""), onPress, DEFAULT_NARRATION);
        }
        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, this.getX(), this.getY(), 16, 96,
                SLOT_SIZE, SLOT_SIZE, 256, 256);
        }
    }
}
