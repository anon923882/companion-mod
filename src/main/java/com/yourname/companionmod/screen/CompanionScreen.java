package com.yourname.companionmod.screen;

import com.yourname.companionmod.client.gui.CompanionGuiTextures;
import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;
    private static final int SLOT_SIZE = CompanionGuiTextures.SLOT_SIZE;
    private static final int BORDER_X = 8;
    private static final int BORDER_Y = 18;
    private static final int PLAYER_INVENTORY_OFFSET = 14;
    private static final int SETTINGS_BUTTON_SIZE = 20;
    private static final int SETTINGS_BUTTON_U = 0; // Update to your icons.png U
    private static final int SETTINGS_BUTTON_V = 64; // Update to your icons.png V

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.titleLabelX = BORDER_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = BORDER_X;
        this.inventoryLabelY = BORDER_Y + SLOT_SIZE * 3 + PLAYER_INVENTORY_OFFSET;
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
        int slotsOriginX = x + BORDER_X;
        int slotsOriginY = y + BORDER_Y;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = slotsOriginX + col * SLOT_SIZE;
                int slotY = slotsOriginY + row * SLOT_SIZE;
                guiGraphics.blit(CompanionGuiTextures.SLOTS_BACKGROUND, slotX, slotY, 0, 0, SLOT_SIZE, SLOT_SIZE, 256, 256);
            }
        }
        int playerInvY = slotsOriginY + SLOT_SIZE * 3 + PLAYER_INVENTORY_OFFSET;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = slotsOriginX + col * SLOT_SIZE;
                int slotY = playerInvY + row * SLOT_SIZE;
                guiGraphics.blit(CompanionGuiTextures.SLOTS_BACKGROUND, slotX, slotY, 0, 0, SLOT_SIZE, SLOT_SIZE, 256, 256);
            }
        }
        renderEquipmentSlotBackgrounds(guiGraphics);
    }

    private void renderEquipmentSlotBackgrounds(GuiGraphics guiGraphics) {
        for (int slotOffset = 0; slotOffset < CompanionMenu.EQUIPMENT_SLOT_COUNT; slotOffset++) {
            Slot slot = this.menu.getSlot(CompanionMenu.getEquipmentSlotIndex(slotOffset));
            int slotX = this.leftPos + slot.x - 1;
            int slotY = this.topPos + slot.y - 1;
            CompanionMenu.renderSingleSlot(guiGraphics, slotX, slotY);
        }
    }
}
