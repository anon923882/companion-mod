package com.yourname.companionmod.screen;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;
import org.joml.Quaternionf;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation SURVIVAL_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/inventory.png");

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176; // vanilla
        this.imageHeight = 166; // vanilla minus hotbar
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
        // Fill crafting grid + result
        int bgColor = 0xFF373737;
        guiGraphics.fill(x + 98, y + 18, x + 98 + 55, y + 18 + 55, bgColor);
        // Render entity
        LivingEntity companion = this.menu.getCompanion();
        if (companion != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                (float)(x + 51), (float)(y + 75), 30f,
                Vector3f.ZERO,
                new Quaternionf().rotationXYZ(0, 0, 0),
                new Quaternionf().rotationY((float)Math.PI),
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
