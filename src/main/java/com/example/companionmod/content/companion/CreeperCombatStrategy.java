package com.example.companionmod.content.companion;

import net.minecraft.world.entity.monster.Creeper;
import com.example.companionmod.content.CompanionEntity;

/**
 * Encapsulates creeper-specific combat spacing and attack decisions so the main
 * combat handler stays focused on general logic.
 */
public class CreeperCombatStrategy {
    private static final double BOW_MIN_RANGE = 9.0D;
    private static final double BOW_MAX_RANGE = 14.0D;
    private static final double BOW_RETREAT_RANGE = 7.0D;
    private static final double MELEE_STRIKE_RANGE = 3.2D;
    private static final double MELEE_APPROACH_RANGE = 4.6D;
    private static final double MELEE_POST_HIT_RETREAT = 7.8D;

    private final CompanionEntity companion;

    public CreeperCombatStrategy(CompanionEntity companion) {
        this.companion = companion;
    }

    public Plan plan(Creeper creeper, boolean usingBow, double distance) {
        boolean primed = isPrimed(creeper);
        Action action = Action.HOLD;
        boolean attackNow = false;
        boolean retreatAfterAttack = false;
        double speed = 1.0D;
        boolean raiseShield = false;

        if (usingBow) {
            if (primed || distance < BOW_RETREAT_RANGE) {
                action = Action.RETREAT;
                speed = 1.35D;
                raiseShield = primed && distance < 3.2D;
            } else if (distance > BOW_MAX_RANGE + 1.0D) {
                action = Action.APPROACH;
                speed = 1.05D;
            }

            attackNow = !primed && distance >= BOW_MIN_RANGE && distance <= BOW_MAX_RANGE;
        } else {
            if (primed || distance < MELEE_STRIKE_RANGE - 0.4D) {
                action = Action.RETREAT;
                speed = 1.4D;
                raiseShield = primed && distance < 3.0D;
            } else if (distance > MELEE_APPROACH_RANGE) {
                action = Action.APPROACH;
                speed = 1.2D;
            } else {
                action = Action.HOLD;
            }

            attackNow = !primed && distance <= MELEE_STRIKE_RANGE;
            retreatAfterAttack = true;
        }

        return new Plan(action, speed, attackNow, retreatAfterAttack, raiseShield);
    }

    public void retreatAfterHit(Creeper creeper) {
        double distance = companion.distanceTo(creeper);
        if (distance < MELEE_POST_HIT_RETREAT) {
            companion.getNavigation().moveTo(companion.getX() + companion.getLookAngle().x * -3.0D,
                    companion.getY(),
                    companion.getZ() + companion.getLookAngle().z * -3.0D,
                    1.35D);
        }
    }

    public double meleeStrikeRange() {
        return MELEE_STRIKE_RANGE;
    }

    private boolean isPrimed(Creeper creeper) {
        return creeper.isIgnited() || creeper.getSwellDir() > 0 || creeper.getSwelling(0.0F) > 0;
    }

    public enum Action {
        APPROACH,
        RETREAT,
        HOLD
    }

    public record Plan(Action action, double speed, boolean shouldAttack, boolean retreatAfterAttack,
                       boolean raiseShield) { }
}
