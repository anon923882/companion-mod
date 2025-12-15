package com.example.companionmod.content;

import com.example.companionmod.CompanionMod;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public class CompanionEntity extends PathfinderMob implements MenuProvider {
    private static final int INVENTORY_SIZE = 41;
    private static final int HEAD_SLOT_INDEX = 0;
    private static final int CHEST_SLOT_INDEX = 1;
    private static final int LEGS_SLOT_INDEX = 2;
    private static final int FEET_SLOT_INDEX = 3;
    private static final int MAINHAND_SLOT_INDEX = 4;

    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

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


    public SimpleContainer getInventory() {
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
        NonNullList<ItemStack> stacks = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            stacks.set(i, inventory.getItem(i));
        }
        ContainerHelper.saveAllItems(tag, stacks, this.registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Inventory")) {
            NonNullList<ItemStack> stacks = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag, stacks, this.registryAccess());
            for (int i = 0; i < stacks.size() && i < inventory.getContainerSize(); i++) {
                inventory.setItem(i, stacks.get(i));
            }
            syncAllEquipmentSlots();
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        int index = slotIndexFor(slot);
        if (index >= 0) {
            inventory.setItem(index, stack);
            super.setItemSlot(slot, stack);
        } else {
            super.setItemSlot(slot, stack);
        }
    }

    public void syncAllEquipmentSlots() {
        syncEquipmentSlot(EquipmentSlot.HEAD);
        syncEquipmentSlot(EquipmentSlot.CHEST);
        syncEquipmentSlot(EquipmentSlot.LEGS);
        syncEquipmentSlot(EquipmentSlot.FEET);
        syncEquipmentSlot(EquipmentSlot.MAINHAND);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            pullEquipmentState();
        }
    }

    public void syncEquipmentSlot(EquipmentSlot equipmentSlot) {
        int index = slotIndexFor(equipmentSlot);
        if (index >= 0) {
            ItemStack stack = inventory.getItem(index);
            ItemStack previous = super.getItemBySlot(equipmentSlot);
            if (stacksDiffer(previous, stack)) {
                super.setItemSlot(equipmentSlot, stack.copy());
            }
        }
    }

    private void pullEquipmentState() {
        updateInventorySlot(EquipmentSlot.HEAD);
        updateInventorySlot(EquipmentSlot.CHEST);
        updateInventorySlot(EquipmentSlot.LEGS);
        updateInventorySlot(EquipmentSlot.FEET);
        updateInventorySlot(EquipmentSlot.MAINHAND);
    }

    private void updateInventorySlot(EquipmentSlot slot) {
        int index = slotIndexFor(slot);
        if (index >= 0) {
            ItemStack equipped = super.getItemBySlot(slot);
            ItemStack stored = inventory.getItem(index);
            if (stacksDiffer(stored, equipped)) {
                inventory.setItem(index, equipped.copy());
            }
        }
    }

    private static boolean stacksDiffer(ItemStack first, ItemStack second) {
        if (first.isEmpty() && second.isEmpty()) {
            return false;
        }
        if (first.isEmpty() != second.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(first, second)) {
            return true;
        }
        if (first.getCount() != second.getCount()) {
            return true;
        }
        return first.getDamageValue() != second.getDamageValue();
    }

    private static int slotIndexFor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> HEAD_SLOT_INDEX;
            case CHEST -> CHEST_SLOT_INDEX;
            case LEGS -> LEGS_SLOT_INDEX;
            case FEET -> FEET_SLOT_INDEX;
            case MAINHAND -> MAINHAND_SLOT_INDEX;
            default -> -1;
        };
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide) {
            pullEquipmentState();
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
