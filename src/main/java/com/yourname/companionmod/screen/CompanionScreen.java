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
    private static final int STORAGE_ROWS_PIXEL_HEIGHT = CompanionMenu.STORAGE_ROWS * CompanionGuiTextures.SLOT_SIZE;
    private static final int BASE_PANEL_HEIGHT = 114;
    private static final int SETTINGS_PANEL_SLOT_COLUMNS = 3;
    private static final int SETTINGS_PANEL_WIDTH = CompanionGuiTextures.SLOT_SIZE * SETTINGS_PANEL_SLOT_COLUMNS + 14;
    private static final int SETTINGS_PANEL_HEIGHT = 104;
    private static final Component FOLLOW_SETTING_LABEL = Component.translatable("gui.companionmod.settings.follow");
    private static final Component AUTO_HEAL_SETTING_LABEL = Component.translatable("gui.companionmod.settings.auto_heal");
    private static final Component AUTO_EQUIP_SETTING_LABEL = Component.translatable("gui.companionmod.settings.auto_equip");
    private static final Component SETTINGS_TITLE = Component.translatable("gui.companionmod.settings.title");
    private static final ItemStack FOLLOW_ICON = new ItemStack(Items.LEAD);
    private static final ItemStack HEAL_ICON = new ItemStack(Items.GOLDEN_APPLE);
    private static final ItemStack AUTO_EQUIP_ICON = new ItemStack(Items.DIAMOND_SWORD);

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
    private SlotToggleButton followToggle;
    private SlotToggleButton autoHealToggle;
    private SlotToggleButton autoEquipToggle;

    @Override
    protected void init() {
        super.init();
        int buttonX = this.leftPos + this.imageWidth - CompanionGuiTextures.SLOT_SIZE - 6;
        int buttonY = this.topPos + 4;

        this.settingsButton = new SettingsButton(buttonX, buttonY, button -> this.toggleSettingsPanel());
        this.addRenderableWidget(this.settingsButton);

        this.followToggle = new SlotToggleButton(Component.literal(""), FOLLOW_ICON,
            button -> this.sendMenuButtonRequest(CompanionMenu.BUTTON_TOGGLE_FOLLOW));
        this.autoHealToggle = new SlotToggleButton(Component.literal(""), HEAL_ICON,
            button -> this.sendMenuButtonRequest(CompanionMenu.BUTTON_TOGGLE_AUTO_HEAL));
        this.autoEquipToggle = new SlotToggleButton(Component.literal(""), AUTO_EQUIP_ICON,
            button -> this.sendMenuButtonRequest(CompanionMenu.BUTTON_TOGGLE_AUTO_EQUIP));
        this.addRenderableWidget(this.followToggle);
        this.addRenderableWidget(this.autoHealToggle);
        this.addRenderableWidget(this.autoEquipToggle);
        this.updateSettingsWidgets();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        int backgroundX = x - CompanionGuiTextures.UPGRADE_INVENTORY_OFFSET;
        int backgroundWidth = this.imageWidth + CompanionGuiTextures.UPGRADE_INVENTORY_OFFSET;

        CompanionGuiHelper.renderStorageBackground(guiGraphics, backgroundX, y, backgroundWidth, STORAGE_ROWS_PIXEL_HEIGHT);

        CompanionGuiHelper.renderSlotArea(guiGraphics, x + CompanionGuiTextures.STORAGE_X_OFFSET,
            y + CompanionGuiTextures.STORAGE_Y_OFFSET, CompanionMenu.STORAGE_COLUMNS, CompanionMenu.STORAGE_ROWS);

        CompanionGuiHelper.renderEquipmentColumn(guiGraphics, this.leftPos, this.topPos,
            CompanionMenu.EQUIPMENT_SLOT_COUNT);
        this.renderEquipmentSlotBackgrounds(guiGraphics);
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
        this.renderSettingsTooltips(guiGraphics, mouseX, mouseY);
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
        if (this.followToggle == null || this.autoHealToggle == null || this.autoEquipToggle == null) {
            return;
        }
        boolean visible = this.settingsOpen;
        this.followToggle.visible = visible;
        this.followToggle.active = visible;
        this.autoHealToggle.visible = visible;
        this.autoHealToggle.active = visible;
        if (this.autoEquipToggle != null) {
            this.autoEquipToggle.visible = visible;
            this.autoEquipToggle.active = visible;
        }
        this.placeSettingsButtons();
        this.updateSettingLabels();
    }

    private void updateSettingLabels() {
        if (this.followToggle == null || this.autoHealToggle == null || this.autoEquipToggle == null) {
            return;
        }
        this.updateToggleLabel(this.followToggle, this.menu.isFollowingEnabled());
        this.updateToggleLabel(this.autoHealToggle, this.menu.isAutoHealEnabled());
        if (this.autoEquipToggle != null) {
            this.updateToggleLabel(this.autoEquipToggle, this.menu.isAutoEquipEnabled());
        }
    }

    private void updateToggleLabel(SlotToggleButton button, boolean value) {
        Component state = Component.translatable(value ? "gui.companionmod.toggle.on"
            : "gui.companionmod.toggle.off");
        button.setMessage(state);
        button.isOn = value;
    }

    private void placeSettingsButtons() {
        if (this.followToggle == null || this.autoHealToggle == null || this.autoEquipToggle == null) {
            return;
        }
        int toggleX = this.getSettingsPanelX() + CompanionGuiTextures.STORAGE_X_OFFSET;
        int followY = this.getSettingsPanelY() + 26;
        this.followToggle.setX(toggleX);
        this.followToggle.setY(followY);
        this.followToggle.setWidth(CompanionGuiTextures.SLOT_SIZE);
        this.followToggle.setHeight(CompanionGuiTextures.SLOT_SIZE);
        this.autoHealToggle.setX(toggleX);
        this.autoHealToggle.setY(followY + CompanionGuiTextures.SLOT_SIZE + 4);
        this.autoHealToggle.setWidth(CompanionGuiTextures.SLOT_SIZE);
        this.autoHealToggle.setHeight(CompanionGuiTextures.SLOT_SIZE);
        if (this.autoEquipToggle != null) {
            this.autoEquipToggle.setX(toggleX);
            this.autoEquipToggle.setY(this.autoHealToggle.getY() + CompanionGuiTextures.SLOT_SIZE + 4);
            this.autoEquipToggle.setWidth(CompanionGuiTextures.SLOT_SIZE);
            this.autoEquipToggle.setHeight(CompanionGuiTextures.SLOT_SIZE);
        }
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
        if (this.followToggle != null && this.autoHealToggle != null) {
            int labelX = this.followToggle.getX() + CompanionGuiTextures.SLOT_SIZE + 6;
            guiGraphics.drawString(this.font, FOLLOW_SETTING_LABEL, labelX, this.followToggle.getY() + 5, 0x3F3F3F, false);
            guiGraphics.drawString(this.font, AUTO_HEAL_SETTING_LABEL, labelX, this.autoHealToggle.getY() + 5, 0x3F3F3F, false);
            if (this.autoEquipToggle != null) {
                guiGraphics.drawString(this.font, AUTO_EQUIP_SETTING_LABEL, labelX, this.autoEquipToggle.getY() + 5, 0x3F3F3F, false);
            }
        }
    }

    private void renderSettingsToggleSlots(GuiGraphics guiGraphics) {
        if (this.followToggle == null || this.autoHealToggle == null) {
            return;
        }
        CompanionGuiHelper.renderSlotArea(guiGraphics, this.followToggle.getX(), this.followToggle.getY(),
            SETTINGS_PANEL_SLOT_COLUMNS, 3);
        CompanionGuiHelper.renderSingleSlot(guiGraphics, this.followToggle.getX(), this.followToggle.getY());
        CompanionGuiHelper.renderSingleSlot(guiGraphics, this.autoHealToggle.getX(), this.autoHealToggle.getY());
        if (this.autoEquipToggle != null) {
            CompanionGuiHelper.renderSingleSlot(guiGraphics, this.autoEquipToggle.getX(), this.autoEquipToggle.getY());
        }
    }

    private void renderEquipmentSlotBackgrounds(GuiGraphics guiGraphics) {
        for (int slotOffset = 0; slotOffset < CompanionMenu.EQUIPMENT_SLOT_COUNT; slotOffset++) {
            Slot slot = this.menu.getSlot(CompanionMenu.getEquipmentSlotIndex(slotOffset));
            int slotX = this.leftPos + slot.x - 1;
            int slotY = this.topPos + slot.y - 1;
            CompanionGuiHelper.renderSingleSlot(guiGraphics, slotX, slotY);
        }
    }

    private void renderSettingsTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!this.settingsOpen) {
            return;
        }
        if (this.followToggle != null && this.followToggle.isHoveredOrFocused()) {
            guiGraphics.renderTooltip(this.font, this.getSettingTooltip(FOLLOW_SETTING_LABEL, this.followToggle), mouseX, mouseY);
            return;
        }
        if (this.autoHealToggle != null && this.autoHealToggle.isHoveredOrFocused()) {
            guiGraphics.renderTooltip(this.font, this.getSettingTooltip(AUTO_HEAL_SETTING_LABEL, this.autoHealToggle), mouseX, mouseY);
        }
        if (this.autoEquipToggle != null && this.autoEquipToggle.isHoveredOrFocused()) {
            guiGraphics.renderTooltip(this.font, this.getSettingTooltip(AUTO_EQUIP_SETTING_LABEL, this.autoEquipToggle), mouseX, mouseY);
        }
    }

    private Component getSettingTooltip(Component label, SlotToggleButton button) {
        return label.copy().append(": ").append(button.getMessage());
    }

    private static class SlotToggleButton extends Button {
        private final ItemStack icon;
        private boolean isOn;

        protected SlotToggleButton(Component message, ItemStack icon, OnPress onPress) {
            super(0, 0, CompanionGuiTextures.SLOT_SIZE, CompanionGuiTextures.SLOT_SIZE, message, onPress,
                DEFAULT_NARRATION);
            this.icon = icon;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int u = this.isHoveredOrFocused() ? 47 : 29;
            guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, this.getX(), this.getY(), u, 0,
                CompanionGuiTextures.SLOT_SIZE, CompanionGuiTextures.SLOT_SIZE, 256, 256);
            guiGraphics.renderItem(this.icon, this.getX() + 1, this.getY() + 1);
            if (this.isOn) {
                guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, this.getX() + 1, this.getY() + 1, 77, 0,
                    16, 16, 256, 256);
            }
        }
    }



    private static class SettingsButton extends Button {
        private static final int ICON_U = 16;
        private static final int ICON_V = 96;

        protected SettingsButton(int x, int y, OnPress onPress) {
            super(x, y, CompanionGuiTextures.SLOT_SIZE, CompanionGuiTextures.SLOT_SIZE, Component.literal(""), onPress,
                DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int u = this.isHoveredOrFocused() ? 47 : 29;
            guiGraphics.blit(CompanionGuiTextures.GUI_CONTROLS, this.getX(), this.getY(), u, 0,
                CompanionGuiTextures.SLOT_SIZE, CompanionGuiTextures.SLOT_SIZE, 256, 256);
            guiGraphics.blit(CompanionGuiTextures.ICONS, this.getX() + 1, this.getY() + 1, ICON_U, ICON_V,
                16, 16, 256, 256);
        }
    }
}
