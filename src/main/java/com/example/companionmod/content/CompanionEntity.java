package com.example.companionmod.content;

import com.example.companionmod.CompanionMod;
import com.example.companionmod.content.companion.CompanionEquipmentHandler;
import com.example.companionmod.content.companion.CompanionInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public class CompanionEntity extends PathfinderMob implements MenuProvider {
    private final CompanionInventory inventory = new CompanionInventory();
    private final CompanionEquipmentHandler equipmentHandler = new CompanionEquipmentHandler(this, inventory);

    public CompanionEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(net.minecraft.world.item.Items.NAME_TAG)) {
            InteractionResult result = held.interactLivingEntity(player, this, hand);
            if (result.consumesAction()) {
                return result;
            }
        }

        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer && this.isWithinUsableDistance(player)) {
            serverPlayer.openMenu(this, buf -> buf.writeVarInt(this.getId()));
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }


    public CompanionInventory getInventory() {
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
        return CompanionMenu.forServer(id, playerInventory, this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        inventory.saveToTag(tag, this.registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        inventory.loadFromTag(tag, this.registryAccess());
        syncAllEquipmentSlots();
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        equipmentHandler.pullSlotToInventory(slot);
    }

    public void syncAllEquipmentSlots() {
        equipmentHandler.syncAllToEntity();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            equipmentHandler.pullAllFromEntity();
        }
    }

    public void syncEquipmentSlot(EquipmentSlot equipmentSlot) {
        equipmentHandler.syncEquipmentSlot(equipmentSlot);
    }

    @Override
    protected void hurtArmor(net.minecraft.world.damagesource.DamageSource source, float amount) {
        equipmentHandler.hurtArmor(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide) {
            equipmentHandler.pullAllFromEntity();
        }
        return result;
    }

    public boolean isWithinUsableDistance(Player player) {
        return !this.isRemoved() && this.distanceToSqr(player) <= 9.0D;
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
}
