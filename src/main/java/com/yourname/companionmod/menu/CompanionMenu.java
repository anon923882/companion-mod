// Only relevant excerpt - for full fix, these key lines go in CompanionMenu:
// --- Change all usages of CompanionEntity.TOTAL_SLOTS for display to CompanionEntity.STORAGE_SIZE only ---

// for visual slots:
for (int row = 0; row < STORAGE_ROWS; row++) {
  for (int col = 0; col < STORAGE_COLUMNS; col++) {
    int index = col + row * STORAGE_COLUMNS;
    this.addSlot(new Slot(companionInventory, index,
      STORAGE_START_X + col * SLOT_SPACING,
      STORAGE_START_Y + row * SLOT_SPACING)); // Only 0..26
  }
}
// ... Armour and hand slots added separately as before
// Remove any visual slot looping or layout logic past STORAGE_SIZE.

// In CompanionEntity, update equipBestGear to only swap in a better item:
private void equipBestArmor(ArmorItem.Type armorType, int equipmentSlotIndex, boolean allowReplacement) {
    ItemStack currentlyEquipped = this.inventory.getItem(equipmentSlotIndex);
    double currentScore = getArmorScore(currentlyEquipped, armorType);
    int bestIndex = -1;
    double bestScore = currentScore;
    for (int i = 0; i < STORAGE_SIZE; i++) {
        ItemStack candidate = this.inventory.getItem(i);
        double score = getArmorScore(candidate, armorType);
        if (score > bestScore) {
            bestScore = score;
            bestIndex = i;
        }
    }
    // Only swap if improvement
    if (bestIndex >= 0 && bestScore > currentScore) {
        this.swapSlots(bestIndex, equipmentSlotIndex);
    }
}

// Same for weapon logic: only swap if better main hand
private void equipBestWeapon(boolean allowReplacement) {
    ItemStack currentWeapon = this.inventory.getItem(MAIN_HAND_SLOT);
    double currentScore = getWeaponScore(currentWeapon);
    int bestIndex = -1;
    double bestScore = currentScore;
    for (int i = 0; i < STORAGE_SIZE; i++) {
        ItemStack candidate = this.inventory.getItem(i);
        double score = getWeaponScore(candidate);
        if (score > bestScore) {
            bestScore = score;
            bestIndex = i;
        }
    }
    if (bestIndex >= 0 && bestScore > currentScore) {
        this.swapSlots(bestIndex, MAIN_HAND_SLOT);
    }
}
