package com.yourname.companionmod.screen;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation SURVIVAL_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/inventory.png");

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(SURVIVAL_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int bgColor = 0xFF373737;
        guiGraphics.fill(x + 98, y + 18, x + 98 + 55, y + 18 + 55, bgColor);
        LivingEntity companion = this.menu.getCompanion();
        if (companion != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                x + 51, y + 75, 30,
                0, 0, 180f, 180f, 0f,
                companion
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
