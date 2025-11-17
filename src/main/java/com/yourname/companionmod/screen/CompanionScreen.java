package com.yourname.companionmod.screen;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation GENERIC_CHEST_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private static final int CHEST_WIDTH = 176;
    private static final int SLOT_BACKGROUND_U = 7;
    private static final int SLOT_BACKGROUND_V = 17;
    private static final int SLOT_BACKGROUND_SIZE = 18;
    private static final int PLAYER_SECTION_HEIGHT = 96;
    private static final int SETTINGS_PANEL_WIDTH = 92;
    private static final int SETTINGS_PANEL_PADDING = 6;
    private static final int GEAR_BUTTON_SIZE = 18;
    private static final int PANEL_BACKGROUND = 0xFFB48A4A;
    private static final int PANEL_OUTLINE = 0xFF6E4D23;
    private static final int XP_BAR_BG = 0xFF3B2A15;
    private static final int XP_BAR_FILL = 0xFF58C754;

    private final int containerRows = CompanionMenu.STORAGE_ROWS;
    private GearButton settingsButton;
    private boolean settingsVisible = false;
    private final List<SettingEntry> settingEntries = new ArrayList<>();

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = CHEST_WIDTH + CompanionMenu.EQUIPMENT_PANEL_WIDTH;
        this.imageHeight = 114 + this.containerRows * SLOT_BACKGROUND_SIZE;
        this.titleLabelX = CompanionMenu.STORAGE_START_X;
        this.titleLabelY = 6;
        this.inventoryLabelX = CompanionMenu.STORAGE_START_X;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int buttonX = this.leftPos + this.imageWidth - GEAR_BUTTON_SIZE - 6;
        int buttonY = this.topPos + 6;
        this.settingsButton = this.addRenderableWidget(new GearButton(buttonX, buttonY,
            this::toggleSettingsPanel));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.refreshSettingStates();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int equipmentX = this.leftPos;
        int chestX = equipmentX + CompanionMenu.EQUIPMENT_PANEL_WIDTH;
        int y = this.topPos;
        int topHeight = this.containerRows * SLOT_BACKGROUND_SIZE + 17;

        guiGraphics.blit(GENERIC_CHEST_TEXTURE, equipmentX, y, 0, 0,
            CompanionMenu.EQUIPMENT_PANEL_WIDTH, topHeight + PLAYER_SECTION_HEIGHT);
        guiGraphics.blit(GENERIC_CHEST_TEXTURE, chestX, y, 0, 0, CHEST_WIDTH, topHeight);
        guiGraphics.blit(GENERIC_CHEST_TEXTURE, chestX, y + topHeight, 0, 126,
            CHEST_WIDTH, PLAYER_SECTION_HEIGHT);
        this.renderManualSlotFrames(guiGraphics);
        this.renderExperienceOverlay(guiGraphics);
        if (this.settingsVisible) {
            this.renderSettingsPanel(guiGraphics);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderManualSlotFrames(GuiGraphics guiGraphics) {
        this.blitSlot(guiGraphics, this.leftPos + CompanionMenu.MAIN_HAND_SLOT_X - 1,
            this.topPos + CompanionMenu.MAIN_HAND_SLOT_Y - 1);
        this.blitSlot(guiGraphics, this.leftPos + CompanionMenu.OFF_HAND_SLOT_X - 1,
            this.topPos + CompanionMenu.OFF_HAND_SLOT_Y - 1);
        this.blitSlot(guiGraphics, this.leftPos + CompanionMenu.ARMOR_COLUMN_X - 1,
            this.topPos + CompanionMenu.ARMOR_START_Y - 1);
        this.blitSlot(guiGraphics, this.leftPos + CompanionMenu.ARMOR_COLUMN_X - 1,
            this.topPos + CompanionMenu.ARMOR_START_Y + SLOT_BACKGROUND_SIZE - 1);
        this.blitSlot(guiGraphics, this.leftPos + CompanionMenu.ARMOR_COLUMN_X - 1,
            this.topPos + CompanionMenu.ARMOR_START_Y + SLOT_BACKGROUND_SIZE * 2 - 1);
        this.blitSlot(guiGraphics, this.leftPos + CompanionMenu.ARMOR_COLUMN_X - 1,
            this.topPos + CompanionMenu.ARMOR_START_Y + SLOT_BACKGROUND_SIZE * 3 - 1);
    }

    private void renderExperienceOverlay(GuiGraphics guiGraphics) {
        int level = this.menu.getDisplayedLevel();
        int experience = this.menu.getDisplayedExperience();
        int needed = this.menu.getDisplayedExperienceToNext();
        Component levelText = Component.translatable("gui.companionmod.level", level);
        Component xpText = needed == 0
            ? Component.translatable("gui.companionmod.max_level")
            : Component.translatable("gui.companionmod.xp", experience, needed);
        int textX = this.leftPos + CompanionMenu.STORAGE_START_X;
        int textY = this.topPos + 6;
        guiGraphics.drawString(this.font, levelText, textX, textY, 0x3F2F0A, false);
        guiGraphics.drawString(this.font, xpText, textX, textY + 9, 0x3F2F0A, false);

        if (needed > 0) {
            int barWidth = 120;
            int barX = this.leftPos + CompanionMenu.STORAGE_START_X;
            int barY = this.topPos + 22;
            guiGraphics.fill(barX, barY, barX + barWidth, barY + 4, XP_BAR_BG);
            float progress = Math.min(1.0F, experience / (float)Math.max(1, needed));
            int fillWidth = (int)(barWidth * progress);
            guiGraphics.fill(barX + 1, barY + 1, barX + 1 + fillWidth, barY + 3, XP_BAR_FILL);
        }
    }

    private void blitSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(GENERIC_CHEST_TEXTURE, x, y, SLOT_BACKGROUND_U, SLOT_BACKGROUND_V,
            SLOT_BACKGROUND_SIZE, SLOT_BACKGROUND_SIZE);
    }

    private void toggleSettingsPanel() {
        this.settingsVisible = !this.settingsVisible;
        this.rebuildSettingsButtons();
    }

    private void renderSettingsPanel(GuiGraphics guiGraphics) {
        int panelX = this.getSettingsPanelX();
        int panelY = this.getSettingsPanelY();
        int entryCount = Math.max(this.settingEntries.size(), 4);
        int panelHeight = SETTINGS_PANEL_PADDING * 2 + entryCount * 18 + 12;
        int bottom = panelY + panelHeight;
        guiGraphics.fill(panelX, panelY, panelX + SETTINGS_PANEL_WIDTH, bottom, PANEL_BACKGROUND);
        guiGraphics.drawString(this.font,
            Component.translatable("gui.companionmod.settings"), panelX + SETTINGS_PANEL_PADDING,
            panelY + 4, 0x3F2F0A, false);
        guiGraphics.hLine(panelX, panelX + SETTINGS_PANEL_WIDTH, panelY, PANEL_OUTLINE);
        guiGraphics.hLine(panelX, panelX + SETTINGS_PANEL_WIDTH, bottom, PANEL_OUTLINE);
        guiGraphics.vLine(panelX, panelY, bottom, PANEL_OUTLINE);
        guiGraphics.vLine(panelX + SETTINGS_PANEL_WIDTH, panelY, bottom, PANEL_OUTLINE);
    }

    private void rebuildSettingsButtons() {
        this.settingEntries.forEach(entry -> this.removeWidget(entry.toggle));
        this.settingEntries.clear();
        if (!this.settingsVisible) {
            return;
        }
        int x = this.getSettingsPanelX() + SETTINGS_PANEL_PADDING;
        int y = this.getSettingsPanelY() + SETTINGS_PANEL_PADDING + 12;
        int width = SETTINGS_PANEL_WIDTH - SETTINGS_PANEL_PADDING * 2;
        this.addSettingToggle(Component.translatable("gui.companionmod.setting.pickup_xp"),
            this.menu::isExperiencePickupEnabled, CompanionMenu.BUTTON_TOGGLE_XP, x, y, width);
        y += 18;
        this.addSettingToggle(Component.translatable("gui.companionmod.setting.passive_hunt"),
            this.menu::isPassiveHuntEnabled, CompanionMenu.BUTTON_TOGGLE_PASSIVE, x, y, width);
        y += 18;
        this.addSettingToggle(Component.translatable("gui.companionmod.setting.hostile_hunt"),
            this.menu::isHostileHuntEnabled, CompanionMenu.BUTTON_TOGGLE_HOSTILE, x, y, width);
        y += 18;
        this.addSettingToggle(Component.translatable("gui.companionmod.setting.auto_equip"),
            this.menu::isAutoEquipEnabled, CompanionMenu.BUTTON_TOGGLE_AUTO_EQUIP, x, y, width);
    }

    private void addSettingToggle(Component label, BooleanSupplier supplier, int buttonId,
            int x, int y, int width) {
        SettingToggle toggle = new SettingToggle(x, y, width, 16, label, supplier.getAsBoolean(),
            () -> this.sendSettingsToggle(buttonId));
        this.addRenderableWidget(toggle);
        this.settingEntries.add(new SettingEntry(toggle, supplier));
    }

    private void refreshSettingStates() {
        for (SettingEntry entry : this.settingEntries) {
            boolean serverState = entry.stateSupplier.getAsBoolean();
            if (entry.toggle.isSelected() != serverState) {
                entry.toggle.setSelected(serverState);
            }
        }
    }

    private void sendSettingsToggle(int buttonId) {
        Minecraft minecraft = this.minecraft;
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    private int getSettingsPanelX() {
        return this.leftPos + this.imageWidth - SETTINGS_PANEL_WIDTH - 6;
    }

    private int getSettingsPanelY() {
        return this.topPos + 40;
    }

    private static class SettingEntry {
        private final SettingToggle toggle;
        private final BooleanSupplier stateSupplier;

        private SettingEntry(SettingToggle toggle, BooleanSupplier stateSupplier) {
            this.toggle = toggle;
            this.stateSupplier = stateSupplier;
        }
    }

    private class SettingToggle extends AbstractWidget {
        private final Runnable toggleAction;
        private boolean selected;

        protected SettingToggle(int x, int y, int width, int height, Component label, boolean selected,
                Runnable toggleAction) {
            super(x, y, width, height, label);
            this.toggleAction = toggleAction;
            this.selected = selected;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int boxSize = 10;
            int boxX = this.getX();
            int boxY = this.getY() + (this.height - boxSize) / 2;
            guiGraphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, PANEL_OUTLINE);
            int fillColor = this.selected ? XP_BAR_FILL : PANEL_BACKGROUND;
            guiGraphics.fill(boxX + 1, boxY + 1, boxX + boxSize - 1, boxY + boxSize - 1, fillColor);
            int textY = this.getY() + (this.height - 8) / 2;
            guiGraphics.drawString(CompanionScreen.this.font, this.getMessage(),
                boxX + boxSize + 4, textY, 0x3F2F0A, false);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.selected = !this.selected;
            this.toggleAction.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return this.selected;
        }
    }

    private class GearButton extends AbstractWidget {
        private final Runnable onPress;

        public GearButton(int x, int y, Runnable onPress) {
            super(x, y, GEAR_BUTTON_SIZE, GEAR_BUTTON_SIZE, Component.literal(""));
            this.onPress = onPress;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int y = this.getY();
            guiGraphics.blit(GENERIC_CHEST_TEXTURE, x, y, SLOT_BACKGROUND_U, SLOT_BACKGROUND_V,
                this.width, this.height);
            this.renderGearIcon(guiGraphics, x, y);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.onPress.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        private void renderGearIcon(GuiGraphics guiGraphics, int x, int y) {
            int centerX = x + this.width / 2;
            int centerY = y + this.height / 2;
            int radius = 5;
            guiGraphics.fill(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
                0xFF5F5F5F);
            guiGraphics.fill(centerX - radius + 1, centerY - radius + 1, centerX + radius - 1,
                centerY + radius - 1, 0xFFDDDDDD);
            guiGraphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFF5F5F5F);
            for (int i = 0; i < 8; i++) {
                double angle = Math.PI / 4 * i;
                int armX = centerX + (int)(Math.cos(angle) * (radius + 1));
                int armY = centerY + (int)(Math.sin(angle) * (radius + 1));
                guiGraphics.fill(armX - 1, armY - 1, armX + 1, armY + 1, 0xFF5F5F5F);
            }
        }
    }
}
