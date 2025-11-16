package com.yourname.companionmod.screen;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse(
        "minecraft:textures/gui/container/generic_54.png");
    private Button hostilesButton;
    private Button passivesButton;
    private Button pickupXpButton;

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        int rows = menu.getCompanionRows();
        this.imageHeight = 114 + rows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.hostilesButton = Button.builder(toggleLabel("Hostiles", this.menu.isAttackingHostiles()), button -> {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }).pos(x + 8, y + 4).size(90, 20).build();

        this.passivesButton = Button.builder(toggleLabel("Passives", this.menu.isAttackingPassives()), button -> {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
        }).pos(x + 8 + 94, y + 4).size(90, 20).build();

        this.pickupXpButton = Button.builder(toggleLabel("Pickup XP", this.menu.isPickingUpXp()), button -> {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
        }).pos(x + 8 + 188, y + 4).size(90, 20).build();

        this.addRenderableWidget(this.hostilesButton);
        this.addRenderableWidget(this.passivesButton);
        this.addRenderableWidget(this.pickupXpButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.hostilesButton != null) {
            this.hostilesButton.setMessage(toggleLabel("Hostiles", this.menu.isAttackingHostiles()));
        }
        if (this.passivesButton != null) {
            this.passivesButton.setMessage(toggleLabel("Passives", this.menu.isAttackingPassives()));
        }
        if (this.pickupXpButton != null) {
            this.pickupXpButton.setMessage(toggleLabel("Pickup XP", this.menu.isPickingUpXp()));
        }
    }

    private Component toggleLabel(String name, boolean enabled) {
        return Component.literal(name + ": " + (enabled ? "ON" : "OFF"));
    }
}