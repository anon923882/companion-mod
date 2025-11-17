package com.yourname.companionmod.entity.custom;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.UUID;

public class CompanionEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> OWNER_UUID =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> LEVEL =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXPERIENCE =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> XP_PICKUP_ENABLED =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HUNT_PASSIVES =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HUNT_HOSTILES =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AUTO_EQUIP =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    
    public static final int STORAGE_SIZE = 27;
    public static final int MAIN_HAND_SLOT = STORAGE_SIZE;
    public static final int OFF_HAND_SLOT = STORAGE_SIZE + 1;
    public static final int BOOTS_SLOT = STORAGE_SIZE + 2;
    public static final int LEGS_SLOT = STORAGE_SIZE + 3;
    public static final int CHEST_SLOT = STORAGE_SIZE + 4;
    public static final int HELMET_SLOT = STORAGE_SIZE + 5;
    public static final int TOTAL_SLOTS = STORAGE_SIZE + 6;
    private static final float SELF_HEAL_THRESHOLD = 0.6f;
    private static final int SELF_HEAL_COOLDOWN_TICKS = 100;
    private static final double TELEPORT_DISTANCE_SQR = 20 * 20;
    private static final int TELEPORT_ATTEMPTS = 10;
    private static final double FOLLOW_AFTER_TELEPORT_SPEED = 1.1D;
    private static final int FORCED_FOLLOW_AFTER_TELEPORT_TICKS = 60;
    private static final double FORCED_FOLLOW_DESIRED_DISTANCE_SQR = 4.0D;

    private static final int MAX_LEVEL = 20;
    private static final double BASE_MAX_HEALTH = 20.0D;
    private static final double HEALTH_PER_LEVEL = 1.5D;
    private static final double ATTACK_PER_LEVEL = 0.2D;
    private static final double XP_ORB_SEARCH_RADIUS = 4.0D;
    private static final double PASSIVE_HUNT_RANGE = 16.0D;
    private static final double HOSTILE_HUNT_RANGE = 24.0D;

    private final CompanionInventory inventory = new CompanionInventory(this);
    private int healCooldown = 0;
    private boolean suppressInventoryUpdates = false;
    private int forcedFollowTicks = 0;

    public CompanionEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.FOLLOW_RANGE, 32.0D)
            .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, "");
        builder.define(LEVEL, 1);
        builder.define(EXPERIENCE, 0);
        builder.define(XP_PICKUP_ENABLED, true);
        builder.define(HUNT_PASSIVES, false);
        builder.define(HUNT_HOSTILES, true);
        builder.define(AUTO_EQUIP, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CompanionMeleeAttackGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new HuntGoal(this, HOSTILE_HUNT_RANGE,
            living -> living instanceof Enemy, this::isHostileHuntEnabled));
        this.targetSelector.addGoal(3, new HuntGoal(this, PASSIVE_HUNT_RANGE,
            living -> living instanceof Animal && !(living instanceof Enemy), this::isPassiveHuntEnabled));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (this.isOwnedBy(player) || this.getOwnerUUID() == null) {
                if (this.getOwnerUUID() == null) {
                    this.setOwnerUUID(player.getUUID());
                }
                serverPlayer.openMenu(new CompanionMenuProvider(this));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(OWNER_UUID, uuid.toString());
    }

    public UUID getOwnerUUID() {
        String uuidString = this.entityData.get(OWNER_UUID);
        return uuidString.isEmpty() ? null : UUID.fromString(uuidString);
    }

    public boolean isOwnedBy(Player player) {
        UUID ownerUUID = this.getOwnerUUID();
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    public Player getOwner() {
        UUID uuid = this.getOwnerUUID();
        return uuid == null ? null : this.level().getPlayerByUUID(uuid);
    }

    public Container getInventory() {
        return this.inventory;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.healCooldown > 0) {
                this.healCooldown--;
            }
            this.tryConsumeHeldFood();
            this.tryTeleportToOwner();
            this.updateForcedFollow();
            this.collectNearbyExperience();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUUID() != null) {
            tag.putString("OwnerUUID", this.getOwnerUUID().toString());
        }
        tag.put("Inventory", this.inventory.createTag(this.registryAccess()));
        tag.putInt("CompanionLevel", this.getCompanionLevel());
        tag.putInt("CompanionExperience", this.getExperience());
        tag.putBoolean("PickupXp", this.isExperiencePickupEnabled());
        tag.putBoolean("HuntPassives", this.isPassiveHuntEnabled());
        tag.putBoolean("HuntHostiles", this.isHostileHuntEnabled());
        tag.putBoolean("AutoEquip", this.isAutoEquipEnabled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("OwnerUUID")) {
            this.setOwnerUUID(UUID.fromString(tag.getString("OwnerUUID")));
        }
        if (tag.contains("Inventory")) {
            this.inventory.fromTag(tag.getList("Inventory", 10), this.registryAccess());
            this.onInventoryChanged();
        }
        if (tag.contains("CompanionLevel")) {
            this.entityData.set(LEVEL, Math.max(1, tag.getInt("CompanionLevel")));
            this.applyLevelBonuses();
        }
        if (tag.contains("CompanionExperience")) {
            this.entityData.set(EXPERIENCE, Math.max(0, tag.getInt("CompanionExperience")));
        }
        this.entityData.set(XP_PICKUP_ENABLED, tag.getBoolean("PickupXp"));
        this.entityData.set(HUNT_PASSIVES, tag.getBoolean("HuntPassives"));
        this.entityData.set(HUNT_HOSTILES, tag.contains("HuntHostiles") ? tag.getBoolean("HuntHostiles") : true);
        this.entityData.set(AUTO_EQUIP, !tag.contains("AutoEquip") || tag.getBoolean("AutoEquip"));
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack);
            }
        }
    }

    private static class FollowOwnerGoal extends Goal {
        private final CompanionEntity companion;
        private Player owner;
        private final double speedModifier = 1.0D;
        private final float stopDistance = 3.0F;
        private final float startDistance = 10.0F;

        public FollowOwnerGoal(CompanionEntity companion) {
            this.companion = companion;
        }

        @Override
        public boolean canUse() {
            Player player = this.companion.getOwner();
            if (player == null || player.isSpectator()) {
                return false;
            }
            if (this.companion.distanceToSqr(player) < (double)(this.stopDistance * this.stopDistance)) {
                return false;
            }
            this.owner = player;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.companion.getNavigation().isDone() 
                && this.companion.distanceToSqr(this.owner) > (double)(this.stopDistance * this.stopDistance);
        }

        @Override
        public void start() {
            this.companion.getNavigation().moveTo(this.owner, this.speedModifier);
        }

        @Override
        public void stop() {
            this.owner = null;
            this.companion.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.companion.getLookControl().setLookAt(this.owner, 10.0F, 
                (float)this.companion.getMaxHeadXRot());
            if (this.companion.distanceToSqr(this.owner) > (double)(this.startDistance * this.startDistance)) {
                this.companion.getNavigation().moveTo(this.owner, this.speedModifier);
            }
        }
    }

    private void onInventoryChanged() {
        if (this.level().isClientSide || this.suppressInventoryUpdates) {
            return;
        }
        this.suppressInventoryUpdates = true;
        try {
            if (this.isAutoEquipEnabled()) {
                this.evaluateBestEquipment(true);
            } else {
                this.fillEmptyEquipmentSlots();
                this.syncEquipmentFromInventory();
            }
        } finally {
            this.suppressInventoryUpdates = false;
        }
    }

    public void equipBestGear() {
        if (this.level().isClientSide) {
            return;
        }
        this.runWithInventorySilenced(() -> {
            this.evaluateBestEquipment(true);
            this.syncEquipmentFromInventory();
        });
        this.onInventoryChanged();
    }

    private void evaluateBestEquipment(boolean allowReplacement) {
        this.equipBestWeapon(allowReplacement);
        this.equipShield(allowReplacement);
        this.equipBestArmor(ArmorItem.Type.BOOTS, BOOTS_SLOT, allowReplacement);
        this.equipBestArmor(ArmorItem.Type.LEGGINGS, LEGS_SLOT, allowReplacement);
        this.equipBestArmor(ArmorItem.Type.CHESTPLATE, CHEST_SLOT, allowReplacement);
        this.equipBestArmor(ArmorItem.Type.HELMET, HELMET_SLOT, allowReplacement);
    }

    private void fillEmptyEquipmentSlots() {
        this.equipBestWeapon(false);
        this.equipShield(false);
        this.equipBestArmor(ArmorItem.Type.BOOTS, BOOTS_SLOT, false);
        this.equipBestArmor(ArmorItem.Type.LEGGINGS, LEGS_SLOT, false);
        this.equipBestArmor(ArmorItem.Type.CHESTPLATE, CHEST_SLOT, false);
        this.equipBestArmor(ArmorItem.Type.HELMET, HELMET_SLOT, false);
    }

    private void equipBestWeapon(boolean allowReplacement) {
        ItemStack currentWeapon = this.inventory.getItem(MAIN_HAND_SLOT);
        double currentScore = getWeaponScore(currentWeapon);
        
        if (!allowReplacement && !currentWeapon.isEmpty()) {
            return;
        }
        
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

    private void equipShield(boolean allowReplacement) {
        if (!allowReplacement && !this.inventory.getItem(OFF_HAND_SLOT).isEmpty()) {
            return;
        }
        for (int i = 0; i < STORAGE_SIZE; i++) {
            ItemStack candidate = this.inventory.getItem(i);
            if (!candidate.isEmpty() && candidate.getItem() instanceof ShieldItem) {
                this.swapSlots(i, OFF_HAND_SLOT);
                break;
            }
        }
    }

    private void equipBestArmor(ArmorItem.Type armorType, int equipmentSlotIndex, boolean allowReplacement) {
        ItemStack currentArmor = this.inventory.getItem(equipmentSlotIndex);
        double currentScore = getArmorScore(currentArmor, armorType);
        
        if (!allowReplacement && !currentArmor.isEmpty()) {
            return;
        }
        
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
        
        if (bestIndex >= 0 && bestScore > currentScore) {
            this.swapSlots(bestIndex, equipmentSlotIndex);
        }
    }

    private void swapSlots(int from, int to) {
        ItemStack fromStack = this.inventory.getItem(from);
        ItemStack toStack = this.inventory.getItem(to);
        this.inventory.setItem(to, fromStack);
        this.inventory.setItem(from, toStack);
    }

    private static double getWeaponScore(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        double attack = 0;
        if (stack.getItem() instanceof SwordItem sword) {
            attack = 4.0 + sword.getTier().getAttackDamageBonus();
        } else if (stack.getItem() instanceof AxeItem axe) {
            attack = 3.0 + axe.getTier().getAttackDamageBonus();
        } else if (stack.getItem() instanceof TieredItem tiered) {
            attack = 1.0 + tiered.getTier().getAttackDamageBonus();
        }
        return attack;
    }

    private static double getArmorScore(ItemStack stack, ArmorItem.Type armorType) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor) || armor.getType() != armorType) {
            return 0;
        }
        return armor.getDefense() * 10.0 + armor.getToughness();
    }

    private void syncEquipmentFromInventory() {
        this.setItemSlot(EquipmentSlot.MAINHAND, this.inventory.getItem(MAIN_HAND_SLOT));
        this.setItemSlot(EquipmentSlot.OFFHAND, this.inventory.getItem(OFF_HAND_SLOT));
        this.setItemSlot(EquipmentSlot.FEET, this.inventory.getItem(BOOTS_SLOT));
        this.setItemSlot(EquipmentSlot.LEGS, this.inventory.getItem(LEGS_SLOT));
        this.setItemSlot(EquipmentSlot.CHEST, this.inventory.getItem(CHEST_SLOT));
        this.setItemSlot(EquipmentSlot.HEAD, this.inventory.getItem(HELMET_SLOT));
    }

    private void runWithInventorySilenced(Runnable action) {
        boolean previous = this.suppressInventoryUpdates;
        this.suppressInventoryUpdates = true;
        try {
            action.run();
        } finally {
            this.suppressInventoryUpdates = previous;
        }
    }

    private void tryConsumeHeldFood() {
        if (this.healCooldown > 0) {
            return;
        }
        if (this.getHealth() / this.getMaxHealth() > SELF_HEAL_THRESHOLD) {
            return;
        }
        ItemStack held = this.inventory.getItem(MAIN_HAND_SLOT);
        if (held.isEmpty() || !held.has(DataComponents.FOOD)) {
            return;
        }
        this.level().gameEvent(this, GameEvent.EAT, this.position());
        ItemStack result = held.getItem().finishUsingItem(held, this.level(), this);
        this.inventory.setItem(MAIN_HAND_SLOT, result);
        this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
        this.healCooldown = SELF_HEAL_COOLDOWN_TICKS;
    }

    private void tryTeleportToOwner() {
        Player owner = this.getOwner();
        if (owner == null || owner.level() != this.level() || owner.isSpectator()) {
            return;
        }
        if (this.distanceToSqr(owner) < TELEPORT_DISTANCE_SQR) {
            return;
        }

        BlockPos ownerPos = owner.blockPosition();
        for (int i = 0; i < TELEPORT_ATTEMPTS; i++) {
            BlockPos target = ownerPos.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1,
                this.random.nextInt(3) - 1);
            if (this.canTeleportTo(target)) {
                this.teleportTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);
                this.getNavigation().stop();
                this.resumeFollowingOwner(owner);
                return;
            }
        }

        this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        this.getNavigation().stop();
        this.resumeFollowingOwner(owner);
    }

    private boolean canTeleportTo(BlockPos target) {
        if (!this.level().isLoaded(target) || !this.level().getWorldBorder().isWithinBounds(target)) {
            return false;
        }
        BlockPos below = target.below();
        if (!this.level().getBlockState(below).isSolidRender(this.level(), below)) {
            return false;
        }
        if (!this.level().isEmptyBlock(target) || !this.level().isEmptyBlock(target.above())) {
            return false;
        }
        return this.level().noCollision(this, this.getBoundingBox().move(
            target.getX() + 0.5D - this.getX(),
            target.getY() - this.getY(),
            target.getZ() + 0.5D - this.getZ()));
    }

    private void resumeFollowingOwner(Player owner) {
        this.forcedFollowTicks = FORCED_FOLLOW_AFTER_TELEPORT_TICKS;
        this.getNavigation().moveTo(owner, FOLLOW_AFTER_TELEPORT_SPEED);
        this.getLookControl().setLookAt(owner, 10.0F, this.getMaxHeadXRot());
    }

    private void updateForcedFollow() {
        if (this.forcedFollowTicks <= 0) {
            return;
        }

        Player owner = this.getOwner();
        if (owner == null || owner.isSpectator() || owner.level() != this.level()) {
            this.forcedFollowTicks = 0;
            return;
        }

        this.forcedFollowTicks--;
        if (this.distanceToSqr(owner) > FORCED_FOLLOW_DESIRED_DISTANCE_SQR) {
            this.getNavigation().moveTo(owner, FOLLOW_AFTER_TELEPORT_SPEED);
            this.getLookControl().setLookAt(owner, 10.0F, this.getMaxHeadXRot());
        } else if (this.forcedFollowTicks < FORCED_FOLLOW_AFTER_TELEPORT_TICKS / 2) {
            this.forcedFollowTicks = 0;
        }
    }

    public int getCompanionLevel() {
        return this.entityData.get(LEVEL);
    }

    public int getExperience() {
        return this.entityData.get(EXPERIENCE);
    }

    public int getExperienceToNextLevel() {
        int level = this.getCompanionLevel();
        return level >= MAX_LEVEL ? 0 : this.getExperienceRequirementForLevel(level);
    }

    private int getExperienceRequirementForLevel(int level) {
        return (int)(25 + Math.pow(level, 1.4D) * 12);
    }

    public boolean isExperiencePickupEnabled() {
        return this.entityData.get(XP_PICKUP_ENABLED);
    }

    public boolean isPassiveHuntEnabled() {
        return this.entityData.get(HUNT_PASSIVES);
    }

    public boolean isHostileHuntEnabled() {
        return this.entityData.get(HUNT_HOSTILES);
    }

    public boolean isAutoEquipEnabled() {
        return this.entityData.get(AUTO_EQUIP);
    }

    public void toggleExperiencePickup() {
        this.entityData.set(XP_PICKUP_ENABLED, !this.isExperiencePickupEnabled());
    }

    public void togglePassiveHunting() {
        this.entityData.set(HUNT_PASSIVES, !this.isPassiveHuntEnabled());
    }

    public void toggleHostileHunting() {
        this.entityData.set(HUNT_HOSTILES, !this.isHostileHuntEnabled());
    }

    public void toggleAutoEquip() {
        this.entityData.set(AUTO_EQUIP, !this.isAutoEquipEnabled());
        this.onInventoryChanged();
    }

    private void addExperience(int value) {
        if (value <= 0 || this.level().isClientSide) {
            return;
        }
        int level = this.getCompanionLevel();
        int experience = this.getExperience();
        int remaining = value;

        while (remaining > 0 && level < MAX_LEVEL) {
            int needed = this.getExperienceRequirementForLevel(level);
            if (remaining + experience >= needed) {
                remaining -= Math.max(0, needed - experience);
                level++;
                experience = 0;
                this.applyLevelBonuses();
            } else {
                experience += remaining;
                remaining = 0;
            }
        }

        if (level >= MAX_LEVEL) {
            experience = 0;
        }

        this.entityData.set(EXPERIENCE, experience);
        this.entityData.set(LEVEL, level);
    }

    private void applyLevelBonuses() {
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (health != null) {
            double newMax = Math.min(BASE_MAX_HEALTH + HEALTH_PER_LEVEL * (this.getCompanionLevel() - 1),
                BASE_MAX_HEALTH + HEALTH_PER_LEVEL * (MAX_LEVEL - 1));
            health.setBaseValue(newMax);
            if (this.getHealth() > newMax) {
                this.setHealth((float)newMax);
            }
        }
        if (attack != null) {
            double newAttack = 4.0D + ATTACK_PER_LEVEL * (this.getCompanionLevel() - 1);
            attack.setBaseValue(newAttack);
        }
    }

    private void collectNearbyExperience() {
        if (!this.isExperiencePickupEnabled()) {
            return;
        }
        AABB searchArea = this.getBoundingBox().inflate(XP_ORB_SEARCH_RADIUS);
        List<ExperienceOrb> orbs = this.level().getEntitiesOfClass(ExperienceOrb.class, searchArea,
            orb -> !orb.isRemoved() && orb.isAlive());
        for (ExperienceOrb orb : orbs) {
            this.addExperience(orb.value);
            orb.discard();
            this.level().playSound(null, this, SoundEvents.EXPERIENCE_ORB_PICKUP, this.getSoundSource(), 0.2F,
                0.95F + this.level().random.nextFloat() * 0.1F);
        }
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity target) {
        boolean result = super.killedEntity(level, target);
        int reward = Math.max(1, target.getExperienceReward(level, this) / 2);
        this.addExperience(reward);
        return result;
    }

    private static class CompanionMeleeAttackGoal extends MeleeAttackGoal {
        public CompanionMeleeAttackGoal(CompanionEntity companion) {
            super(companion, 1.2D, true);
        }

        protected double getAttackReachSqr(LivingEntity enemy) {
            return 2.0F + enemy.getBbWidth();
        }
    }

    private static class HuntGoal extends NearestAttackableTargetGoal<LivingEntity> {
        private final CompanionEntity companion;
        private final java.util.function.BooleanSupplier enabledCheck;

        public HuntGoal(CompanionEntity companion, double range,
                java.util.function.Predicate<LivingEntity> matcher, java.util.function.BooleanSupplier enabledCheck) {
            super(companion, LivingEntity.class, 10, true, true, matcher);
            this.companion = companion;
            this.enabledCheck = enabledCheck;
            this.targetConditions.range((float)range);
        }

        @Override
        public boolean canUse() {
            if (!this.enabledCheck.getAsBoolean()) {
                return false;
            }
            if (this.companion.getOwner() == null) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.enabledCheck.getAsBoolean() && super.canContinueToUse();
        }
    }

    private static class CompanionInventory extends SimpleContainer {
        private final CompanionEntity companion;

        public CompanionInventory(CompanionEntity companion) {
            super(TOTAL_SLOTS);
            this.companion = companion;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            this.companion.onInventoryChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            if (!this.companion.isAlive()) {
                return false;
            }
            if (!this.companion.isOwnedBy(player) && !player.isCreative()) {
                return false;
            }
            return player.distanceToSqr(this.companion) <= 64.0D;
        }
    }

    private static class CompanionMenuProvider implements net.minecraft.world.MenuProvider {
        private final CompanionEntity companion;

        public CompanionMenuProvider(CompanionEntity companion) {
            this.companion = companion;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("container.companionmod.companion_inventory");
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId,
                net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
            return new CompanionMenu(containerId, playerInventory, companion.getInventory(), this.companion);
        }
    }
}
