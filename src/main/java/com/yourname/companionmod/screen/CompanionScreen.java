package com.yourname.companionmod.screen;

import com.yourname.companionmod.client.gui.CompanionGuiHelper;
import com.yourname.companionmod.client.gui.CompanionGuiTextures;
import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final int STORAGE_ROWS_PIXEL_HEIGHT = CompanionMenu.STORAGE_ROWS * CompanionGuiTextures.SLOT_SIZE;
    private static final int BASE_PANEL_HEIGHT = 114;
    private static final int SETTINGS_PANEL_SLOT_COLUMNS = 5;
    private static final int SETTINGS_PANEL_WIDTH = CompanionGuiTextures.SLOT_SIZE * SETTINGS_PANEL_SLOT_COLUMNS + 14;
    private static final int SETTINGS_PANEL_HEIGHT = 92;
    private static final Component FOLLOW_SETTING_LABEL = Component.translatable("gui.companionmod.settings.follow");
    private static final Component AUTO_HEAL_SETTING_LABEL = Component.translatable("gui.companionmod.settings.auto_heal");
    private static final Component SETTINGS_TITLE = Component.translatable("gui.companionmod.settings.title");

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = CompanionMenu.STORAGE_COLUMNS * CompanionGuiTextures.SLOT_SIZE + 14;
        this.imageHeight = BASE_PANEL_HEIGHT + STORAGE_ROWS_PIXEL_HEIGHT;
        this.titleLabelX = CompanionMenu.STORAGE_START_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = CompanionMenu.STORAGE_START_X;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private boolean settingsOpen;
    private Button settingsButton;
    private Button followToggle;
    private Button autoHealToggle;

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 90;
        int buttonX = this.leftPos + this.imageWidth - buttonWidth - 10;
        int buttonY = this.topPos + 4;

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.companionmod.equip_best"),
            button -> this.sendMenuButtonRequest(CompanionMenu.BUTTON_EQUIP_BEST))
            .bounds(buttonX, buttonY, buttonWidth, 20)
            .build());

        int settingsWidth = 70;
        this.settingsButton = Button.builder(
            Component.translatable("gui.companionmod.settings"),
            button -> this.toggleSettingsPanel())
            .bounds(buttonX - settingsWidth - 4, buttonY, settingsWidth, 20)
            .build();
        this.addRenderableWidget(this.settingsButton);

        this.followToggle = Button.builder(Component.literal(""),
            button -> this.sendMenuButtonRequest(CompanionMenu.BUTTON_TOGGLE_FOLLOW))
            .bounds(this.leftPos, this.topPos, 80, CompanionGuiTextures.SLOT_SIZE)
            .build();
        this.autoHealToggle = Button.builder(Component.literal(""),
            button -> this.sendMenuButtonRequest(CompanionMenu.BUTTON_TOGGLE_AUTO_HEAL))
            .bounds(this.leftPos, this.topPos, 80, CompanionGuiTextures.SLOT_SIZE)
            .build();
        this.addRenderableWidget(this.followToggle);
        this.addRenderableWidget(this.autoHealToggle);
        this.updateSettingsWidgets();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        CompanionGuiHelper.renderStorageBackground(guiGraphics, x, y, this.imageWidth, STORAGE_ROWS_PIXEL_HEIGHT);

        CompanionGuiHelper.renderSlotArea(guiGraphics, x + CompanionGuiTextures.STORAGE_X_OFFSET,
            y + CompanionGuiTextures.STORAGE_Y_OFFSET, CompanionMenu.STORAGE_COLUMNS, CompanionMenu.STORAGE_ROWS);

        CompanionGuiHelper.renderEquipmentColumn(guiGraphics, this.leftPos, this.topPos,
            CompanionMenu.EQUIPMENT_SLOT_COUNT);
        int equipmentSlotX = this.leftPos + CompanionMenu.EQUIPMENT_COLUMN_X - 1;
        int equipmentSlotY = this.topPos + CompanionMenu.EQUIPMENT_START_Y - 1;
        CompanionGuiHelper.renderSlotArea(guiGraphics, equipmentSlotX, equipmentSlotY, 1,
            CompanionMenu.EQUIPMENT_SLOT_COUNT);
        CompanionGuiHelper.renderEquipmentIcons(guiGraphics, this.menu,
            this.leftPos + CompanionMenu.EQUIPMENT_COLUMN_X,
            this.topPos + CompanionMenu.EQUIPMENT_START_Y,
            CompanionMenu.EQUIPMENT_SLOT_SPACING, CompanionMenu.EQUIPMENT_SLOT_COUNT);

        if (this.settingsOpen) {
            this.renderSettingsPanel(guiGraphics);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateSettingsWidgets();
    }

    private void sendMenuButtonRequest(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    private void toggleSettingsPanel() {
        this.settingsOpen = !this.settingsOpen;
        this.updateSettingsWidgets();
    }

    private void updateSettingsWidgets() {
        if (this.followToggle == null || this.autoHealToggle == null) {
            return;
        }
        boolean visible = this.settingsOpen;
        this.followToggle.visible = visible;
        this.followToggle.active = visible;
        this.autoHealToggle.visible = visible;
        this.autoHealToggle.active = visible;
        this.placeSettingsButtons();
        this.updateSettingLabels();
    }

    private void updateSettingLabels() {
        if (this.followToggle == null || this.autoHealToggle == null) {
            return;
        }
        this.updateToggleLabel(this.followToggle, FOLLOW_SETTING_LABEL, this.menu.isFollowingEnabled());
        this.updateToggleLabel(this.autoHealToggle, AUTO_HEAL_SETTING_LABEL, this.menu.isAutoHealEnabled());
    }

    private void updateToggleLabel(Button button, Component label, boolean value) {
        Component state = Component.translatable(value ? "gui.companionmod.toggle.on"
            : "gui.companionmod.toggle.off");
        button.setMessage(label.copy().append(": ").append(state));
    }

    private void placeSettingsButtons() {
        if (this.followToggle == null || this.autoHealToggle == null) {
            return;
        }
        int toggleX = this.getSettingsPanelX() + CompanionGuiTextures.STORAGE_X_OFFSET;
        int toggleWidth = CompanionGuiTextures.SLOT_SIZE * SETTINGS_PANEL_SLOT_COLUMNS;
        int followY = this.getSettingsPanelY() + 26;
        this.followToggle.setX(toggleX);
        this.followToggle.setY(followY);
        this.followToggle.setWidth(toggleWidth);
        this.followToggle.setHeight(CompanionGuiTextures.SLOT_SIZE);
        this.autoHealToggle.setX(toggleX);
        this.autoHealToggle.setY(followY + CompanionGuiTextures.SLOT_SIZE + 8);
        this.autoHealToggle.setWidth(toggleWidth);
        this.autoHealToggle.setHeight(CompanionGuiTextures.SLOT_SIZE);
    }

    private int getSettingsPanelX() {
        return this.leftPos + this.imageWidth + 6;
    }

    private int getSettingsPanelY() {
        return this.topPos + 12;
    }

    private void renderSettingsPanel(GuiGraphics guiGraphics) {
        int x = this.getSettingsPanelX();
        int y = this.getSettingsPanelY();
        CompanionGuiHelper.renderSettingsPanel(guiGraphics, x, y, SETTINGS_PANEL_WIDTH, SETTINGS_PANEL_HEIGHT);
        guiGraphics.drawString(this.font, SETTINGS_TITLE, x + 6, y + 8, 0x3F3F3F, false);
        this.renderSettingsToggleSlots(guiGraphics);
    }

    private void renderSettingsToggleSlots(GuiGraphics guiGraphics) {
        if (this.followToggle == null || this.autoHealToggle == null) {
            return;
        }
        CompanionGuiHelper.renderSlotArea(guiGraphics, this.followToggle.getX(), this.followToggle.getY(),
            SETTINGS_PANEL_SLOT_COLUMNS, 1);
        CompanionGuiHelper.renderSlotArea(guiGraphics, this.autoHealToggle.getX(), this.autoHealToggle.getY(),
            SETTINGS_PANEL_SLOT_COLUMNS, 1);
    }

}
