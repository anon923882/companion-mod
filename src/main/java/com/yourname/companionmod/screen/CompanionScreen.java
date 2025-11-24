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

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 194;
        this.imageHeight = 222;
        this.titleLabelX = CompanionMenu.STORAGE_START_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = CompanionMenu.STORAGE_START_X;
        this.inventoryLabelY = CompanionMenu.PLAYER_INVENTORY_START_Y - 10;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Reference: SophisticatedCore/StorageScreenBase.java#L517-L524
        guiGraphics.blit(STORAGE_BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        // Draw slot backgrounds for storage slots (reference: StorageScreenBase.java#L925)
        int slotX = x + CompanionMenu.STORAGE_START_X;
        int slotY = y + CompanionMenu.STORAGE_START_Y;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                guiGraphics.blit(SLOTS_BACKGROUND, slotX + col * 18, slotY + row * 18, 0, 0, 18, 18, 256, 256);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
