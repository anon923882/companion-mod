package com.yourname.companionmod.entity.custom;

import com.yourname.companionmod.menu.CompanionMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;

public class CompanionEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> OWNER_UUID =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> ATTACK_HOSTILES =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACK_PASSIVES =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STAYING =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LEVEL =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXPERIENCE =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT =
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    
    private final SimpleContainer inventory = new SimpleContainer(27);
    private final Container equipmentContainer = new CompanionEquipmentContainer(this);
    private static final int XP_PER_LEVEL = 25;
    private int healCooldown = 0;

    public CompanionEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.FOLLOW_RANGE, 32.0D)
            .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, "");
        builder.define(ATTACK_HOSTILES, true);
        builder.define(ATTACK_PASSIVES, false);
        builder.define(STAYING, false);
        builder.define(LEVEL, 1);
        builder.define(EXPERIENCE, 0);
        builder.define(TEXTURE_VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new ProtectOwnerFromAttackerGoal(this));
        this.targetSelector.addGoal(3, new RetaliateForOwnerGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 10,
            true, false, this::shouldAttackMob));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        if (this.healCooldown > 0) {
            this.healCooldown--;
        } else {
            this.trySelfHeal();
            this.healCooldown = 40;
        }

        this.setCustomName(net.minecraft.network.chat.Component.literal(
            "Companion Lv." + this.entityData.get(LEVEL) + " (" + (int)this.getHealth() + "/" + (int)this.getMaxHealth() + " HP)"));
        this.setCustomNameVisible(true);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (this.isOwnedBy(player) || this.getOwnerUUID() == null) {
                if (player.isCrouching()) {
                    if (this.getOwnerUUID() == null) {
                        this.setOwnerUUID(player.getUUID());
                    }
                    if (player.getItemInHand(hand).isEmpty()) {
                        this.toggleStaying(player);
                    } else {
                        this.cycleAggression(player);
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    if (this.getOwnerUUID() == null) {
                        this.setOwnerUUID(player.getUUID());
                    }
                    serverPlayer.openMenu(new CompanionMenuProvider(this));
                    return InteractionResult.SUCCESS;
                }
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

    public boolean isStaying() {
        return this.entityData.get(STAYING);
    }

    public void toggleStaying(Player owner) {
        boolean newValue = !this.isStaying();
        this.entityData.set(STAYING, newValue);
        this.navigation.stop();
        if (owner != null && !this.level().isClientSide) {
            owner.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Companion is now " + (newValue ? "staying" : "following")), true);
        }
    }

    public boolean shouldAttackMob(LivingEntity mob) {
        if (mob == null || mob == this.getOwner() || this.isStaying()) {
            return false;
        }
        MobCategory category = mob.getType().getCategory();
        boolean hostile = category == MobCategory.MONSTER || category == MobCategory.MISC;
        boolean passive = !hostile;

        if (this.entityData.get(ATTACK_PASSIVES) && passive) {
            return true;
        }
        if (this.entityData.get(ATTACK_HOSTILES) && hostile) {
            return true;
        }

        Player owner = this.getOwner();
        return owner != null && mob instanceof Mob attacker && attacker.getTarget() == owner;
    }

    public int getTextureVariant() {
        return this.entityData.get(TEXTURE_VARIANT);
    }

    private void addExperience(int amount) {
        int newXp = this.entityData.get(EXPERIENCE) + amount;
        int currentLevel = this.entityData.get(LEVEL);
        while (newXp >= XP_PER_LEVEL) {
            newXp -= XP_PER_LEVEL;
            currentLevel++;
            this.levelUp(currentLevel);
        }
        this.entityData.set(EXPERIENCE, newXp);
        this.entityData.set(LEVEL, currentLevel);
    }

    private void levelUp(int newLevel) {
        double baseHealth = 20.0D + (newLevel - 1) * 2.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth);
        this.setHealth((float)this.getMaxHealth());
        this.setCustomName(net.minecraft.network.chat.Component.literal(
            "Companion Lv." + newLevel + " (" + (int)this.getHealth() + "/" + (int)this.getMaxHealth() + " HP)"));
        this.setCustomNameVisible(true);
    }

    private void trySelfHeal() {
        if (this.getHealth() >= this.getMaxHealth() * 0.9F) {
            return;
        }

        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.getItem().isEdible()) {
                FoodProperties props = stack.getItem().getFoodProperties(stack, this);
                if (props != null) {
                    this.heal(props.nutrition());
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        this.inventory.setItem(i, ItemStack.EMPTY);
                    }
                    break;
                }
            }
        }
    }

    private void cycleAggression(Player owner) {
        boolean attackHostiles = this.entityData.get(ATTACK_HOSTILES);
        boolean attackPassives = this.entityData.get(ATTACK_PASSIVES);

        if (!attackHostiles && !attackPassives) {
            attackHostiles = true;
        } else if (attackHostiles && !attackPassives) {
            attackPassives = true;
        } else {
            attackHostiles = false;
            attackPassives = false;
        }

        this.entityData.set(ATTACK_HOSTILES, attackHostiles);
        this.entityData.set(ATTACK_PASSIVES, attackPassives);

        if (owner != null && !owner.level().isClientSide()) {
            String mode = attackHostiles ? (attackPassives ? "aggressive" : "hostile-only") : "defensive";
            owner.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Companion combat mode: " + mode), true);
        }
    }

    public Container getInventory() {
        return this.inventory;
    }

    public Container getEquipmentContainer() {
        return this.equipmentContainer;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficulty,
            MobSpawnType spawnType, SpawnGroupData spawnData) {
        SpawnGroupData data = super.finalizeSpawn(levelAccessor, difficulty, spawnType, spawnData);
        if (!levelAccessor.isClientSide()) {
            this.entityData.set(TEXTURE_VARIANT, this.getRandom().nextInt(3));
        }
        return data;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && target instanceof LivingEntity living && living.isDeadOrDying()) {
            this.addExperience(5);
        }
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUUID() != null) {
            tag.putString("OwnerUUID", this.getOwnerUUID().toString());
        }
        tag.put("Inventory", this.inventory.createTag(this.registryAccess()));
        tag.putBoolean("AttackHostiles", this.entityData.get(ATTACK_HOSTILES));
        tag.putBoolean("AttackPassives", this.entityData.get(ATTACK_PASSIVES));
        tag.putBoolean("Staying", this.entityData.get(STAYING));
        tag.putInt("Level", this.entityData.get(LEVEL));
        tag.putInt("Experience", this.entityData.get(EXPERIENCE));
        tag.putInt("TextureVariant", this.entityData.get(TEXTURE_VARIANT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("OwnerUUID")) {
            this.setOwnerUUID(UUID.fromString(tag.getString("OwnerUUID")));
        }
        if (tag.contains("Inventory")) {
            this.inventory.fromTag(tag.getList("Inventory", 10), this.registryAccess());
        }
        this.entityData.set(ATTACK_HOSTILES, tag.getBoolean("AttackHostiles"));
        this.entityData.set(ATTACK_PASSIVES, tag.getBoolean("AttackPassives"));
        this.entityData.set(STAYING, tag.getBoolean("Staying"));
        this.entityData.set(LEVEL, Math.max(1, tag.getInt("Level")));
        this.entityData.set(EXPERIENCE, tag.getInt("Experience"));
        this.entityData.set(TEXTURE_VARIANT, tag.getInt("TextureVariant"));
        this.levelUp(this.entityData.get(LEVEL));
    }

    private static class CompanionEquipmentContainer implements Container {
        private static final EquipmentSlot[] EQUIPMENT_ORDER = new EquipmentSlot[] {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND
        };

        private final CompanionEntity companion;

        public CompanionEquipmentContainer(CompanionEntity companion) {
            this.companion = companion;
        }

        @Override
        public int getContainerSize() {
            return EQUIPMENT_ORDER.length;
        }

        @Override
        public boolean isEmpty() {
            for (EquipmentSlot slot : EQUIPMENT_ORDER) {
                if (!this.companion.getItemBySlot(slot).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return index >= 0 && index < EQUIPMENT_ORDER.length
                ? this.companion.getItemBySlot(EQUIPMENT_ORDER[index])
                : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            ItemStack stack = this.getItem(index);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack splitStack = stack.split(count);
            if (!splitStack.isEmpty()) {
                this.setChanged();
            }
            return splitStack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack stack = this.getItem(index);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            this.setItem(index, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            if (index >= 0 && index < EQUIPMENT_ORDER.length) {
                this.companion.setItemSlot(EQUIPMENT_ORDER[index], stack);
                this.setChanged();
            }
        }

        @Override
        public void setChanged() {
            this.companion.setPersistenceRequired();
        }

        @Override
        public boolean stillValid(Player player) {
            return !this.companion.isRemoved()
                && player.distanceToSqr(this.companion) <= 64.0D;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < EQUIPMENT_ORDER.length; i++) {
                this.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    // Custom follow goal
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
            if (player == null || player.isSpectator() || this.companion.isStaying()) {
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
                && !this.companion.isStaying()
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

    private static class ProtectOwnerFromAttackerGoal extends TargetGoal {
        private final CompanionEntity companion;
        private LivingEntity attacker;
        private int timestamp;

        public ProtectOwnerFromAttackerGoal(CompanionEntity companion) {
            super(companion, false);
            this.companion = companion;
        }

        @Override
        public boolean canUse() {
            Player owner = this.companion.getOwner();
            if (owner == null) {
                return false;
            }

            this.attacker = owner.getLastHurtByMob();
            int attackerTime = owner.getLastHurtByMobTimestamp();

            if (attackerTime == this.timestamp || this.attacker == null) {
                return false;
            }

            return this.canAttack(this.attacker, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            Player owner = this.companion.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastHurtByMobTimestamp();
            }
            super.start();
        }
    }

    private static class RetaliateForOwnerGoal extends TargetGoal {
        private final CompanionEntity companion;
        private LivingEntity target;
        private int timestamp;

        public RetaliateForOwnerGoal(CompanionEntity companion) {
            super(companion, false);
            this.companion = companion;
        }

        @Override
        public boolean canUse() {
            Player owner = this.companion.getOwner();
            if (owner == null) {
                return false;
            }

            this.target = owner.getLastHurtMob();
            int targetTime = owner.getLastHurtMobTimestamp();

            if (targetTime == this.timestamp || this.target == null) {
                return false;
            }

            return this.canAttack(this.target, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.target);
            Player owner = this.companion.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastHurtMobTimestamp();
            }
            super.start();
        }
    }

    private static class CompanionMenuProvider implements net.minecraft.world.MenuProvider {
        private final CompanionEntity companion;

        public CompanionMenuProvider(CompanionEntity companion) {
            this.companion = companion;
        }

        @Override
        public net.minecraft.network.chat.Component getDisplayName() {
            return net.minecraft.network.chat.Component.literal("Companion Inventory");
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId,
                net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
            return new CompanionMenu(containerId, playerInventory,
                companion.getInventory(), companion.getEquipmentContainer());
        }
    }
}
