package com.example.companionmod.content.companion;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.example.companionmod.content.CompanionEntity;
import java.util.EnumSet;
import java.util.List;

/**
 * Handles threat evaluation, equipment selection, and combat decisions for the companion.
 * The logic is intentionally modular to make future tuning straightforward.
 */
public class CombatHandler {
    private static final int THREAT_RADIUS = 15;
    private static final int REENGAGE_RADIUS = 30;
    private static final int OWNER_PROXIMITY_TRIGGER = 8;
    private static final int THREAT_REEVALUATE_TICKS = 10;
    private static final int EQUIPMENT_REEVALUATE_TICKS = 30;

    private final CompanionEntity companion;
    private final CompanionInventory inventory;
    private int threatTicker;
    private int equipmentTicker;
    private LivingEntity currentTarget;
    private boolean usingBow;

    public CombatHandler(CompanionEntity companion, CompanionInventory inventory) {
        this.companion = companion;
        this.inventory = inventory;
    }

    public Goal createCombatGoal() {
        return new CompanionCombatGoal();
    }

    public void tick() {
        if (!(companion.level() instanceof ServerLevel)) {
            return;
        }

        Player owner = companion.getOwner();
        if (owner == null) {
            clearTarget();
            return;
        }

        if (companion.getHealth() <= companion.getMaxHealth() * 0.2F) {
            clearTarget();
            return;
        }

        if (++threatTicker >= THREAT_REEVALUATE_TICKS) {
            threatTicker = 0;
            chooseTarget(owner);
        }

        if (++equipmentTicker >= EQUIPMENT_REEVALUATE_TICKS || targetChangedToDifferentType()) {
            equipmentTicker = 0;
            selectEquipment();
        }

        if (currentTarget != null && companion.distanceToSqr(currentTarget) > REENGAGE_RADIUS * REENGAGE_RADIUS) {
            clearTarget();
        }
    }

    public boolean hasTarget() {
        return currentTarget != null && currentTarget.isAlive();
    }

    public LivingEntity getTarget() {
        return currentTarget;
    }

    public boolean isUsingBow() {
        return usingBow;
    }

    public void shootProjectile(LivingEntity target, ProjectileWeaponItem weapon, ItemStack bowStack, float distanceFactor) {
        ItemStack arrowStack = findArrow(bowStack, usingBow);
        if (arrowStack.isEmpty()) {
            usingBow = false;
            return;
        }

        AbstractArrow arrow = ProjectileUtil.getMobArrow(companion, arrowStack, distanceFactor, bowStack);
        if (bowStack.getItem() instanceof BowItem bowItem) {
            arrow = bowItem.customArrow(arrow, arrowStack, bowStack);
        }

        double dx = target.getX() - companion.getX();
        double dz = target.getZ() - companion.getZ();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double flat = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + flat * 0.2F, dz, 1.6F, 14 - companion.level().getDifficulty().getId() * 4);
        companion.level().addFreshEntity(arrow);
        bowStack.hurtAndBreak(1, companion, EquipmentSlot.MAINHAND);

        if (!hasInfinity(bowStack)) {
            arrowStack.shrink(1);
        }
    }

    private boolean targetChangedToDifferentType() {
        return currentTarget != null && currentTarget.tickCount == 0;
    }

    private void chooseTarget(Player owner) {
        List<Monster> threats = companion.level().getEntitiesOfClass(Monster.class, AABB.ofSize(owner.position(), THREAT_RADIUS * 2, THREAT_RADIUS * 2, THREAT_RADIUS * 2));
        LivingEntity priority = null;
        double bestScore = 0;
        LivingEntity recentAttacker = owner.getLastHurtByMob();

        for (Monster monster : threats) {
            if (!monster.isAlive()) continue;
            double distanceToOwner = monster.distanceTo(owner);
            if (distanceToOwner > THREAT_RADIUS) continue;

            double score = 0;
            if (recentAttacker != null && recentAttacker.is(monster)) {
                score += 500;
            }
            if (monster.getTarget() == owner || monster.getLastHurtByMob() == owner) {
                score += 200;
            }
            if (distanceToOwner <= OWNER_PROXIMITY_TRIGGER) {
                score += 150;
            }
            if (monster.hasLineOfSight(owner)) {
                score += 50;
            }
            if (monster instanceof Creeper) {
                score += 180;
                if (distanceToOwner <= 10) {
                    score += 120;
                }
            }
            score -= monster.distanceTo(companion) * 2;

            if (score > bestScore) {
                bestScore = score;
                priority = monster;
            }
        }

        if (priority != null && priority.isAlive()) {
            currentTarget = priority;
            companion.setTarget(priority);
        } else {
            clearTarget();
        }
    }

    private void clearTarget() {
        currentTarget = null;
        companion.setTarget(null);
    }

    private void selectEquipment() {
        ItemStack currentMainHand = inventory.getItem(CompanionInventory.MAINHAND_SLOT);
        WeaponChoice choice = pickBestWeapon();
        usingBow = choice.usingBow;

        if (!choice.stack.isEmpty() && !ItemStack.isSameItemSameComponents(choice.stack, currentMainHand)) {
            inventory.setItem(choice.inventoryIndex, currentMainHand);
            inventory.setItem(CompanionInventory.MAINHAND_SLOT, choice.stack);
            companion.syncEquipmentSlot(EquipmentSlot.MAINHAND);
        }
    }

    private WeaponChoice pickBestWeapon() {
        WeaponChoice best = new WeaponChoice(ItemStack.EMPTY, CompanionInventory.MAINHAND_SLOT, false, -Float.MAX_VALUE);
        int arrowCount = countArrows();

        for (int slot = CompanionInventory.MAINHAND_SLOT; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;

            float score = weaponScore(stack, arrowCount);
            boolean bowCandidate = stack.getItem() instanceof BowItem;
            if (score > best.score) {
                best = new WeaponChoice(stack.copy(), slot, bowCandidate, score);
            }
        }

        if (currentTarget instanceof Creeper && !best.stack.isEmpty()) {
            if (!(best.stack.getItem() instanceof BowItem) && arrowCount > 0) {
                WeaponChoice bowChoice = findBowChoice(arrowCount);
                if (bowChoice != null) {
                    best = bowChoice;
                }
            }
        }

        return best;
    }

    private WeaponChoice findBowChoice(int arrowCount) {
        WeaponChoice bowChoice = null;
        for (int slot = CompanionInventory.MAINHAND_SLOT; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof BowItem) {
                float score = weaponScore(stack, arrowCount);
                bowChoice = new WeaponChoice(stack.copy(), slot, true, score);
                break;
            }
        }
        return bowChoice;
    }

    private float weaponScore(ItemStack stack, int arrowCount) {
        Item item = stack.getItem();
        float base = 0;
        if (item instanceof BowItem) {
            if (arrowCount == 0) return -1000;
            base = 5;
            if (hasInfinity(stack)) {
                base += 3;
            }
        } else if (item instanceof SwordItem) {
            base = 8;
        } else if (item instanceof TieredItem) {
            base = 6;
        }

        if (currentTarget != null) {
            String id = currentTarget.getType().builtInRegistryHolder().key().location().getPath();
            int smite = enchantmentLevel(Enchantments.SMITE, stack);
            int arthropods = enchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, stack);
            int sharpness = enchantmentLevel(Enchantments.SHARPNESS, stack);

            if (currentTarget instanceof Mob mob && mob.getType().is(EntityTypeTags.UNDEAD) && smite > 0) {
                base += smite * 2;
            }
            if (id.contains("spider") && arthropods > 0) {
                base += arthropods * 2;
            }
            base += sharpness;
        }

        return base + stack.getDamageValue() * 0.01F;
    }

    private int countArrows() {
        int arrows = 0;
        for (int i = CompanionInventory.MAINHAND_SLOT; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ItemTags.ARROWS)) {
                arrows += stack.getCount();
            }
        }
        return arrows;
    }

    private ItemStack findArrow(ItemStack bowStack, boolean bowSelected) {
        for (int i = CompanionInventory.MAINHAND_SLOT; i < inventory.getContainerSize(); i++) {
            ItemStack candidate = inventory.getItem(i);
            if (candidate.is(ItemTags.ARROWS)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean hasInfinity(ItemStack bowStack) {
        return enchantmentLevel(Enchantments.INFINITY, bowStack) > 0;
    }

    private int enchantmentLevel(net.minecraft.resources.ResourceKey<Enchantment> key, ItemStack stack) {
        Holder.Reference<Enchantment> holder = companion.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
    }

    private record WeaponChoice(ItemStack stack, int inventoryIndex, boolean usingBow, float score) {}

    private class CompanionCombatGoal extends Goal {
        private int attackCooldown;

        private CompanionCombatGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return companion.getOwner() != null;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (!hasTarget()) {
                companion.getNavigation().stop();
                return;
            }

            companion.setTarget(target);
            double distanceSqr = companion.distanceToSqr(target);

            companion.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (isUsingBow()) {
                maintainBowDistance(target, distanceSqr);
            } else {
                pursueTarget(target, distanceSqr);
            }

            if (attackCooldown > 0) {
                attackCooldown--;
            }

            if (shouldAttackNow(distanceSqr)) {
                doAttack(target, distanceSqr);
            }
        }

        private boolean shouldAttackNow(double distanceSqr) {
            double reach = companion.getBbWidth() * 2.0F + companion.getBbWidth();
            double meleeRange = reach * reach + 2.5F;
            if (isUsingBow()) {
                return attackCooldown <= 0 && distanceSqr <= 144;
            }
            return attackCooldown <= 0 && distanceSqr <= meleeRange;
        }

        private void doAttack(LivingEntity target, double distanceSqr) {
            if (isUsingBow()) {
                companion.performRangedAttack(target, (float) Math.sqrt(distanceSqr) / 15.0F);
                attackCooldown = bowCooldown();
                return;
            }

            companion.setSprinting(true);
            if (companion.onGround()) {
                companion.getJumpControl().jump();
            }
            companion.doHurtTarget(target);
            attackCooldown = meleeCooldown();
        }

        private void pursueTarget(LivingEntity target, double distanceSqr) {
            double safeDistance = target instanceof Creeper ? 9.0D : 3.0D;
            if (distanceSqr > safeDistance * safeDistance) {
                companion.getNavigation().moveTo(target, 1.2D);
            } else {
                companion.getNavigation().moveTo(target, 0.9D);
            }
        }

        private void maintainBowDistance(LivingEntity target, double distanceSqr) {
            double preferredMin = 25.0D;
            double preferredMax = 100.0D;
            if (distanceSqr < preferredMin) {
                Vec3 retreat = companion.position().subtract(target.position()).normalize().scale(0.5D);
                companion.getNavigation().moveTo(companion.getX() + retreat.x, companion.getY(), companion.getZ() + retreat.z, 1.0D);
            } else if (distanceSqr > preferredMax) {
                companion.getNavigation().moveTo(target, 1.05D);
            } else {
                Path path = companion.getNavigation().getPath();
                if (path != null) {
                    companion.getNavigation().stop();
                }
            }
        }

        private int meleeCooldown() {
            double attackSpeed = companion.getAttributeValue(Attributes.ATTACK_SPEED);
            attackSpeed = attackSpeed <= 0 ? 4.0D : attackSpeed;
            return Mth.ceil(20D / attackSpeed);
        }

        private int bowCooldown() {
            return 20;
        }
    }
}
