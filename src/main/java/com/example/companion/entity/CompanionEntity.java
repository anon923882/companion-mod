package com.example.companion.entity;

import com.example.companion.CompanionMod;
import com.example.companion.inventory.CompanionMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.NetworkHooks;

public class CompanionEntity extends TamableAnimal {
    private static final int INVENTORY_SIZE = 27;
    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

    public CompanionEntity(EntityType<? extends CompanionEntity> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static void registerSpawnPlacements() {
        // Intentionally left empty for now; companions are summoned with the charm.
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.1D, 4.0F, 2.0F, false));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return false;
    }

    public void openInventory(Player player) {
        if (!this.level().isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new CompanionMenuProvider(this), buffer -> buffer.writeVarInt(this.getId()));
            this.inventory.startOpen(player);
            this.level().gameEvent(GameEvent.CONTAINER_OPEN, this.position(), GameEvent.Context.of(this));
        }
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public void setOwner(Player player) {
        this.setTame(true);
        this.setOwnerUUID(player.getUUID());
        if (this.getCustomName() == null) {
            this.setCustomName(Component.literal(player.getName().getString() + "'s Companion"));
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        for (ItemStack stack : this.inventory.removeAllItems()) {
            this.spawnAtLocation(stack);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.getOwner() instanceof Player owner) {
            double distance = this.distanceToSqr(owner);
            if (distance > 144.0D) {
                Vec3 position = owner.position();
                this.teleportTo(position.x, position.y, position.z);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag listTag = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            CompoundTag slotTag = new CompoundTag();
            slotTag.putByte("Slot", (byte) i);
            if (!stack.isEmpty()) {
                stack.save(slotTag);
            }
            listTag.add(slotTag);
        }
        tag.put("Inventory", listTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ListTag listTag = tag.getList("Inventory", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag slotTag = listTag.getCompound(i);
            int slot = slotTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.getContainerSize()) {
                inventory.setItem(slot, ItemStack.parseOptional(this.registryAccess(), slotTag));
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.15F, 1.0F);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isOwnedBy(player)) {
            this.openInventory(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private record CompanionMenuProvider(CompanionEntity companion) implements net.minecraft.world.MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.translatable("menu.%s.inventory".formatted(CompanionMod.MOD_ID));
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
            return new CompanionMenu(containerId, playerInventory, companion);
        }
    }
}
