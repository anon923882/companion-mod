package com.example.companionmod.client;

import com.example.companionmod.CompanionMod;
import com.example.companionmod.content.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CompanionMod.MOD_ID, "textures/gui/companion_inventory.png");

    public CompanionScreen(CompanionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 245;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        var ownerLabel = this.menu.getOwnerLabel();
        if (ownerLabel != null) {
            int labelWidth = this.font.width(ownerLabel);
            int x = this.leftPos + 8;
            int y = this.topPos + 134;
            guiGraphics.drawString(this.font, ownerLabel, x, y, 0x404040, false);
        }
    }
}
