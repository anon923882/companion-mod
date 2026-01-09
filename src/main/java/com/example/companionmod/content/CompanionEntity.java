package com.example.companionmod.content;

import com.example.companionmod.CompanionMod;
import com.example.companionmod.content.companion.CompanionEquipmentHandler;
import com.example.companionmod.content.companion.CompanionInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import com.example.companionmod.content.companion.CombatHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.monster.RangedAttackMob;
import java.util.Optional;
import java.util.UUID;

public class CompanionEntity extends PathfinderMob implements MenuProvider, RangedAttackMob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private CompanionInventory inventory;
    private CompanionEquipmentHandler equipmentHandler;
    private CombatHandler combatHandler;
    private int openCount = 0;

    public CompanionEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        ensureHandlers();
    }

    private void ensureHandlers() {
        if (this.inventory == null) {
            this.inventory = new CompanionInventory();
        }
        if (this.equipmentHandler == null) {
            this.equipmentHandler = new CompanionEquipmentHandler(this, this.inventory);
        }
        if (this.combatHandler == null) {
            this.combatHandler = new CombatHandler(this, this.inventory);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ATTACK_SPEED, 4.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        ensureHandlers();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, combatHandler.createCombatGoal());
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ensureHandlers();
        ItemStack held = player.getItemInHand(hand);
        if (!this.level().isClientSide && getOwnerUUID().isEmpty()) {
            setOwner(player);
        }
        if (held.is(net.minecraft.world.item.Items.NAME_TAG)) {
            InteractionResult result = held.interactLivingEntity(player, this, hand);
            if (result.consumesAction()) {
                return result;
            }
        }

        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer && this.isWithinUsableDistance(player)) {
            if (!isOwnedBy(player)) {
                return InteractionResult.FAIL;
            }
            serverPlayer.openMenu(this, buf -> buf.writeVarInt(this.getId()));
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }


    public CompanionInventory getInventory() {
        ensureHandlers();
        return inventory;
    }

    @Override
    public Component getDisplayName() {
        Component nameComponent = this.hasCustomName() ? this.getCustomName() : Component.translatable("entity." + CompanionMod.MOD_ID + ".companion");
        return Component.translatable("menu." + CompanionMod.MOD_ID + ".companion_inventory", nameComponent);
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        ensureHandlers();
        return CompanionMenu.forServer(id, playerInventory, this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        ensureHandlers();
        super.addAdditionalSaveData(tag);
        inventory.saveToTag(tag, this.registryAccess());
        getOwnerUUID().ifPresent(uuid -> tag.putUUID("Owner", uuid));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        ensureHandlers();
        super.readAdditionalSaveData(tag);
        inventory.loadFromTag(tag, this.registryAccess());
        if (tag.hasUUID("Owner")) {
            setOwner(tag.getUUID("Owner"));
        }
        syncAllEquipmentSlots();
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        ensureHandlers();
        super.setItemSlot(slot, stack);
        equipmentHandler.pullSlotToInventory(slot);
    }

    public void syncAllEquipmentSlots() {
        ensureHandlers();
        equipmentHandler.syncAllToEntity();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            ensureHandlers();
            equipmentHandler.pullAllFromEntity();
            if (openCount > 0) {
                stopMovingAndFaceOwner();
            }
            combatHandler.tick();
        }
    }

    public void syncEquipmentSlot(EquipmentSlot equipmentSlot) {
        ensureHandlers();
        equipmentHandler.syncEquipmentSlot(equipmentSlot);
    }

    @Override
    protected void hurtArmor(net.minecraft.world.damagesource.DamageSource source, float amount) {
        ensureHandlers();
        equipmentHandler.hurtArmor(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        ensureHandlers();
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide) {
            equipmentHandler.pullAllFromEntity();
        }
        return result;
    }

    public boolean isWithinUsableDistance(Player player) {
        return !this.isRemoved() && this.distanceToSqr(player) <= 9.0D;
    }

    public void onMenuOpened(Player player) {
        ensureHandlers();
        if (openCount == 0) {
            getNavigation().stop();
        }
        openCount++;
    }

    public void onMenuClosed(Player player) {
        ensureHandlers();
        if (openCount > 0) {
            openCount--;
        }
    }

    private void stopMovingAndFaceOwner() {
        Player owner = getOwner();
        if (owner != null) {
            this.getNavigation().stop();
            LookControl lookControl = this.getLookControl();
            Vec3 lookPos = owner.position();
            lookControl.setLookAt(lookPos.x, lookPos.y + owner.getEyeHeight(), lookPos.z);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack projectileWeapon = this.getMainHandItem();
        if (!(projectileWeapon.getItem() instanceof ProjectileWeaponItem weapon)) {
            return;
        }
        combatHandler.shootProjectile(target, weapon, projectileWeapon, distanceFactor);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 0;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        super.playStepSound(pos, state);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.PLAYERS;
    }

    @Override
    public boolean isAlliedTo(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    public void setOwner(Player player) {
        setOwner(player.getUUID());
    }

    public void setOwner(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    public Player getOwner() {
        return this.getOwnerUUID().map(uuid -> this.level().getPlayerByUUID(uuid)).orElse(null);
    }

    public boolean isOwnedBy(Player player) {
        return player != null && this.getOwnerUUID().map(player.getUUID()::equals).orElse(false);
    }

    public Component getOwnerNameComponent() {
        Player owner = getOwner();
        if (owner != null) {
            return owner.getName();
        }
        return null;
    }
}
