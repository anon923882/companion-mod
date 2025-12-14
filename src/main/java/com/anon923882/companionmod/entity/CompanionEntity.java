package com.anon923882.companionmod.entity;

import com.anon923882.companionmod.inventory.CompanionMenu;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.UUID;

public class CompanionEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> OWNER_UUID = 
        SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.STRING);
    
    private final ItemStackHandler inventory = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            CompanionEntity.this.setChanged();
        }
    };

    public CompanionEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowMobGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, "");
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            // Check distance (similar to chest - 8 blocks)
            if (player.distanceToSqr(this) <= 64.0) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, playerInventory, p) -> new CompanionMenu(id, playerInventory, this.getInventoryContainer()),
                        this.getDisplayName()
                    ));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public Container getInventoryContainer() {
        SimpleContainer container = new SimpleContainer(27);
        for (int i = 0; i < 27; i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        return container;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("OwnerUUID", this.entityData.get(OWNER_UUID));
        tag.put("Inventory", inventory.serializeNBT(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("OwnerUUID")) {
            this.entityData.set(OWNER_UUID, tag.getString("OwnerUUID"));
        }
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(this.registryAccess(), tag.getCompound("Inventory"));
        }
    }

    public void setOwner(@Nullable Player player) {
        if (player != null) {
            this.entityData.set(OWNER_UUID, player.getStringUUID());
        }
    }

    @Nullable
    public UUID getOwnerUUID() {
        String uuidString = this.entityData.get(OWNER_UUID);
        if (uuidString.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void setChanged() {
        // Mark entity as changed for saving
    }

    private static class SimpleMenuProvider implements net.minecraft.world.MenuProvider {
        private final net.minecraft.world.inventory.MenuConstructor constructor;
        private final net.minecraft.network.chat.Component displayName;

        public SimpleMenuProvider(net.minecraft.world.inventory.MenuConstructor constructor, 
                                 net.minecraft.network.chat.Component displayName) {
            this.constructor = constructor;
            this.displayName = displayName;
        }

        @Override
        public net.minecraft.network.chat.Component getDisplayName() {
            return displayName;
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, 
                                                                               net.minecraft.world.entity.player.Inventory inventory, 
                                                                               Player player) {
            return constructor.createMenu(id, inventory, player);
        }
    }
}
