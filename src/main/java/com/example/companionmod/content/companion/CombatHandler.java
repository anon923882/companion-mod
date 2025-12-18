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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.item.Items;
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
    private static final double ZOMBIE_SAFE_DISTANCE = 4.5D;
    private static final double ZOMBIE_RETREAT_DISTANCE = 3.2D;
    private static final double CREEPER_PREFERRED_MIN = 8.5D;
    private static final double CREEPER_PREFERRED_MAX = 12.0D;

    private final CompanionEntity companion;
    private final CompanionInventory inventory;
    private final CompanionEquipmentHandler equipmentHandler;
    private int threatTicker;
    private int equipmentTicker;
    private LivingEntity currentTarget;
    private boolean usingBow;

    public CombatHandler(CompanionEntity companion, CompanionInventory inventory, CompanionEquipmentHandler equipmentHandler) {
        this.companion = companion;
        this.inventory = inventory;
        this.equipmentHandler = equipmentHandler;
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
        WeaponChoice choice = pickBestWeapon();
        usingBow = choice.usingBow;

        if (!choice.stack.isEmpty()) {
            equipmentHandler.equipMainHandFromInventory(choice.inventoryIndex);
        }

        equipShieldIfNeeded();
    }

    private WeaponChoice pickBestWeapon() {
        WeaponChoice best = new WeaponChoice(ItemStack.EMPTY, CompanionInventory.HOTBAR_START, false, -Float.MAX_VALUE);
        int arrowCount = countArrows();

        for (int slot = CompanionInventory.HOTBAR_START; slot < inventory.getContainerSize(); slot++) {
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
        for (int slot = CompanionInventory.HOTBAR_START; slot < inventory.getContainerSize(); slot++) {
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
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ItemTags.ARROWS)) {
                arrows += stack.getCount();
            }
        }
        return arrows;
    }

    private ItemStack findArrow(ItemStack bowStack, boolean bowSelected) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
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

    private void equipShieldIfNeeded() {
        boolean wantsShield = currentTarget instanceof RangedAttackMob || currentTarget instanceof Creeper;
        ItemStack offhand = inventory.getItem(CompanionInventory.OFFHAND_SLOT);
        if (wantsShield && offhand.getItem() instanceof ShieldItem) {
            equipmentHandler.syncEquipmentSlot(EquipmentSlot.OFFHAND);
            return;
        }

        if (!wantsShield) {
            return;
        }

        int shieldSlot = -1;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Items.SHIELD)) {
                shieldSlot = i;
                break;
            }
        }

        if (shieldSlot >= 0) {
            ItemStack shield = inventory.getItem(shieldSlot).copy();
            ItemStack previousOffhand = offhand.copy();
            inventory.setItem(CompanionInventory.OFFHAND_SLOT, shield);
            inventory.setItem(shieldSlot, previousOffhand);
            equipmentHandler.syncEquipmentSlot(EquipmentSlot.OFFHAND);
        }
    }

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
                stopShieldUse();
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
            if (isUsingBow()) {
                return attackCooldown <= 0 && distanceSqr <= 144;
            }
            if (getTarget() instanceof Creeper creeper) {
                if (creeper.isIgnited() || creeper.getSwellDir() > 0) {
                    return false;
                }
                return attackCooldown <= 0 && distanceSqr >= (CREEPER_PREFERRED_MIN * CREEPER_PREFERRED_MIN)
                        && distanceSqr <= (CREEPER_PREFERRED_MAX * CREEPER_PREFERRED_MAX);
            }
            if (getTarget().getType().is(EntityTypeTags.UNDEAD)) {
                return attackCooldown <= 0 && distanceSqr <= (ZOMBIE_SAFE_DISTANCE * ZOMBIE_SAFE_DISTANCE);
            }
            return attackCooldown <= 0 && distanceSqr <= meleeRangeSqr();
        }

        private void doAttack(LivingEntity target, double distanceSqr) {
            if (isUsingBow()) {
                companion.performRangedAttack(target, (float) Math.sqrt(distanceSqr) / 15.0F);
                attackCooldown = bowCooldown();
                return;
            }

            companion.setSprinting(!(target instanceof Creeper));
            if (companion.onGround() && !(target instanceof Creeper)) {
                companion.getJumpControl().jump();
            }
            companion.doHurtTarget(target);
            attackCooldown = meleeCooldown();

            if (target instanceof Creeper) {
                retreatFrom(target, 1.2D);
            }
        }

        private void pursueTarget(LivingEntity target, double distanceSqr) {
            if (target instanceof Creeper) {
                handleCreeperSpacing(target, distanceSqr);
                return;
            }

            if (target instanceof RangedAttackMob) {
                tryRaiseShield();
            } else {
                stopShieldUse();
            }

            if (isZombieHordeNearby(target)) {
                if (distanceSqr < ZOMBIE_RETREAT_DISTANCE * ZOMBIE_RETREAT_DISTANCE) {
                    retreatFrom(target, 1.2D);
                    return;
                }
                if (distanceSqr <= meleeRangeSqr()) {
                    companion.getNavigation().stop();
                    return;
                }
                if (distanceSqr < ZOMBIE_SAFE_DISTANCE * ZOMBIE_SAFE_DISTANCE) {
                    companion.getNavigation().moveTo(target, 1.05D);
                    return;
                }
            }

            if (distanceSqr <= meleeRangeSqr()) {
                companion.getNavigation().stop();
            } else if (distanceSqr > 3.5D * 3.5D) {
                companion.getNavigation().moveTo(target, 1.15D);
            } else {
                retreatFrom(target, 0.95D);
            }
        }

        private void maintainBowDistance(LivingEntity target, double distanceSqr) {
            double preferredMin = 49.0D;
            double preferredMax = 144.0D;
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

            if (target instanceof RangedAttackMob) {
                tryRaiseShield();
            } else {
                stopShieldUse();
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

        private void handleCreeperSpacing(LivingEntity target, double distanceSqr) {
            double distance = Math.sqrt(distanceSqr);
            boolean primed = target instanceof Creeper creeper && (creeper.isIgnited() || creeper.getSwellDir() > 0 || creeper.getSwelling(0.0F) > 0);
            if (distance < CREEPER_PREFERRED_MIN || primed) {
                retreatFrom(target, 1.35D);
                stopShieldUse();
                attackCooldown = Math.max(attackCooldown, 10);
            } else if (distance > CREEPER_PREFERRED_MAX) {
                companion.getNavigation().moveTo(target, isUsingBow() ? 0.9D : 1.05D);
                stopShieldUse();
            } else {
                companion.getNavigation().stop();
                stopShieldUse();
            }
        }

        private boolean isZombieHordeNearby(LivingEntity target) {
            if (!(target.getType().is(EntityTypeTags.UNDEAD))) {
                return false;
            }
            List<Monster> nearby = companion.level().getEntitiesOfClass(Monster.class, target.getBoundingBox().inflate(4.0D));
            int zombies = 0;
            for (Monster monster : nearby) {
                if (monster.getType().is(EntityTypeTags.UNDEAD)) {
                    zombies++;
                }
                if (zombies >= 3) {
                    return true;
                }
            }
            return zombies >= 2;
        }

        private void retreatFrom(LivingEntity target, double speed) {
            Vec3 direction = companion.position().subtract(target.position());
            if (direction.lengthSqr() < 0.01D) {
                direction = new Vec3(1, 0, 0);
            }
            Vec3 retreat = direction.normalize().scale(2.5D);
            companion.getNavigation().moveTo(companion.getX() + retreat.x, companion.getY(), companion.getZ() + retreat.z, speed);
        }

        private double meleeRangeSqr() {
            double reach = Math.max(2.25D, companion.getBbWidth() * 3.0F);
            return reach * reach;
        }

        private void tryRaiseShield() {
            ItemStack offhand = companion.getOffhandItem();
            if (offhand.getItem() instanceof ShieldItem) {
                companion.startUsingItem(InteractionHand.OFF_HAND);
            }
        }

        private void stopShieldUse() {
            if (companion.isUsingItem()) {
                companion.stopUsingItem();
            }
        }
    }
}
