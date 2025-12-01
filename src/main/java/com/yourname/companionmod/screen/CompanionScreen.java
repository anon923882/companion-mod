package com.yourname.companionmod.screen;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Companion inventory screen implementing Sophisticated-style rendering.
 * 
 * Architecture based on SophisticatedCore/StorageScreenBase.java:
 * - Equipment slots in left column (like upgrade slots)
 * - Main 3x9 storage grid with proper background rendering
 * - Player inventory below
 * 
 * Key references:
 * - StorageScreenBase.java#L517-L524: renderBg implementation
 * - StorageScreenBase.java#L925-L932: drawSlotBg pattern
 * - GuiHelper.java#L33-L47: slot background rendering
 */
public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    // Texture references matching Sophisticated pattern
    private static final ResourceLocation STORAGE_BACKGROUND = 
        ResourceLocation.fromNamespaceAndPath("companionmod", "textures/gui/storage_background_9.png");
    private static final ResourceLocation SLOTS_BACKGROUND = 
        ResourceLocation.fromNamespaceAndPath("companionmod", "textures/gui/slots_background.png");
    
    // Slot positioning constants from StorageScreenBase.java#L82-L83
    private static final int SLOTS_Y_OFFSET = 17;
    private static final int SLOTS_X_OFFSET = 7;
    
    // GUI dimensions
    // Base width for 9-column inventory (9*18 + margins)
    private static final int BASE_GUI_WIDTH = 176;
    // Height = storage rows + player inventory + margins
    private static final int GUI_HEIGHT = 168; // 3*18 (storage) + 76 (player inv) + margins

    public CompanionScreen(CompanionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        
        // Set GUI dimensions
        this.imageWidth = BASE_GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        
        // Position title label at storage grid location
        // References StorageScreenBase.java slot positioning
        this.titleLabelX = SLOTS_X_OFFSET + 1;
        this.titleLabelY = 6;
        
        // Position inventory label below storage area
        this.inventoryLabelX = SLOTS_X_OFFSET + 1;
        this.inventoryLabelY = 76; // Below 3 rows of storage
    }

    @Override
    protected void init() {
        super.init();
        // Future: Add buttons, tabs, etc. here following Sophisticated pattern
    }

    /**
     * Renders background elements.
     * Based on StorageScreenBase.java#L517-L524
     */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // 1. Render main storage frame/background
        // Reference: StorageScreenBase.drawInventoryBg() L926-929
        renderStorageFrame(guiGraphics, x, y);
        
        // 2. Render slot grid backgrounds
        // Reference: StorageScreenBase.drawSlotBg() L925-932
        renderSlotBackgrounds(guiGraphics, x, y);
        
        // 3. Render equipment slot column backgrounds
        renderEquipmentSlotBackgrounds(guiGraphics, x, y);
    }
    
    /**
     * Renders the storage inventory frame/border.
     * Follows StorageScreenBase.drawInventoryBg() implementation.
     */
    private void renderStorageFrame(GuiGraphics guiGraphics, int x, int y) {
        // Storage background texture should be a 9-patch or tiled texture
        // For now, we render it as the main background panel
        // This matches how Sophisticated renders storage_background_9.png
        
        // Render main panel for storage area (3 rows)
        int storageHeight = 3 * 18 + 14; // 3 rows + margins
        guiGraphics.blit(STORAGE_BACKGROUND, 
            x + SLOTS_X_OFFSET - 4, // Left margin
            y + SLOTS_Y_OFFSET - 4, // Top margin  
            0, 0, // UV start
            9 * 18 + 8, storageHeight, // Width x Height (9 slots + margins)
            256, 256); // Texture dimensions
        
        // Render panel for player inventory (4 rows: 3 + hotbar)
        int playerInvY = y + SLOTS_Y_OFFSET + 3 * 18 + 4; // Below storage
        guiGraphics.blit(STORAGE_BACKGROUND,
            x + SLOTS_X_OFFSET - 4,
            playerInvY,
            0, 0,
            9 * 18 + 8, 4 * 18 + 8, // 4 rows of inventory
            256, 256);
    }
    
    /**
     * Renders slot backgrounds for the 3x9 storage grid.
     * Based on GuiHelper.renderSlotsBackground() L33-47 pattern.
     * 
     * In Sophisticated, this uses a computed texture region from slots_background.png
     * that contains pre-rendered slot grids. For simplicity, we render the grid
     * as a single texture region matching our 3x9 dimensions.
     */
    private void renderSlotBackgrounds(GuiGraphics guiGraphics, int x, int y) {
        // Calculate slot grid position
        int slotX = x + SLOTS_X_OFFSET;
        int slotY = y + SLOTS_Y_OFFSET;
        
        // Storage grid: 9 columns x 3 rows
        // Reference: GuiHelper.renderSlotsBackground uses dynamic UV calculation
        // We render the appropriate section of slots_background.png
        int gridWidth = 9 * 18;  // 162 pixels
        int gridHeight = 3 * 18; // 54 pixels
        
        guiGraphics.blit(SLOTS_BACKGROUND,
            slotX, slotY,     // Position
            0, 0,              // UV start in texture
            gridWidth, gridHeight, // Size to render
            256, 256);         // Texture dimensions
        
        // Render player inventory slot backgrounds (3 rows + hotbar)
        int playerInvY = slotY + 3 * 18 + 4;
        
        // 3 rows of player inventory
        guiGraphics.blit(SLOTS_BACKGROUND,
            slotX, playerInvY,
            0, 0,
            gridWidth, 3 * 18,
            256, 256);
        
        // Hotbar
        int hotbarY = playerInvY + 3 * 18 + 4;
        guiGraphics.blit(SLOTS_BACKGROUND,
            slotX, hotbarY,
            0, 0,
            gridWidth, 18,
            256, 256);
    }
    
    /**
     * Renders equipment slot backgrounds (left column).
     * Follows Sophisticated's upgrade slot rendering pattern from
     * StorageScreenBase.drawUpgradeBackground() L934-957.
     */
    private void renderEquipmentSlotBackgrounds(GuiGraphics guiGraphics, int x, int y) {
        // Equipment slots are positioned in left column
        // Reference: StorageScreenBase.UPGRADE_INVENTORY_OFFSET = 21 (L81)
        int equipX = x - 21; // Left of main GUI
        int equipY = y + CompanionMenu.EQUIP_START_Y;
        
        // Render background panel for equipment column (like upgrade slots)
        // Top cap
        guiGraphics.blit(SLOTS_BACKGROUND,
            equipX, equipY - 4,
            0, 0,
            26, 4,
            256, 256);
        
        // Middle section (6 slots high)
        int middleHeight = 6 * 18;
        guiGraphics.blit(SLOTS_BACKGROUND,
            equipX, equipY,
            0, 4,
            25, middleHeight,
            256, 256);
        
        // Bottom cap
        guiGraphics.blit(SLOTS_BACKGROUND,
            equipX, equipY + middleHeight,
            0, 198,
            25, 6,
            256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render background first
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render tooltips on top
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
