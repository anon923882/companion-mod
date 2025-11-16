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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class CompanionEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> OWNER_UUID = 
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.STRING);
    
    private final SimpleContainer inventory = new SimpleContainer(27);

    public CompanionEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, "");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUUID() != null) {
            tag.putString("OwnerUUID", this.getOwnerUUID().toString());
        }
        tag.put("Inventory", this.inventory.createTag(this.registryAccess()));
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
            return new CompanionMenu(containerId, playerInventory, companion.getInventory());
        }
    }
}