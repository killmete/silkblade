package swu.cp112.silkblade.entity.combat;

import com.badlogic.gdx.utils.Array;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Manages temporary buffs for entities in combat.
 */
public class BuffManager {

    /**
     * Represents a temporary stat buff
     */
    public static class StatBuff {
        private final StatType type;
        private final int amount;
        private int remainingTurns;

        public StatBuff(StatType type, int amount, int duration) {
            this.type = type;
            this.amount = amount;
            this.remainingTurns = duration;
        }

        public StatType getType() {
            return type;
        }

        public int getAmount() {
            return amount;
        }

        public int getRemainingTurns() {
            return remainingTurns;
        }

        public void decrementTurns() {
            remainingTurns--;
        }

        public boolean isExpired() {
            return remainingTurns <= 0;
        }
    }

    /**
     * The type of stat being buffed
     */
    public enum StatType {
        ATTACK,
        DEFENSE,
        CRITICAL_RATE
    }

    private final Array<StatBuff> activeBuffs;
    private final Player player;

    // Cache to avoid recalculating every frame
    private int cachedAttackBuff;
    private int cachedDefenseBuff;
    private float cachedCritRateBuff;

    /**
     * Creates a new buff manager for a player
     * @param player The player this buff manager belongs to
     */
    public BuffManager(Player player) {
        this.player = player;
        this.activeBuffs = new Array<>();
        this.cachedAttackBuff = 0;
        this.cachedDefenseBuff = 0;
        this.cachedCritRateBuff = 0;
    }

    /**
     * Adds a new buff to the player
     * @param type The stat type to buff
     * @param amount The amount to buff by
     * @param duration The duration in turns
     */
    public void addBuff(StatType type, int amount, int duration) {
        if (amount <= 0 || duration <= 0) {
            return; // Ignore non-positive buffs or durations
        }

        // Add the new buff
        StatBuff buff = new StatBuff(type, amount, duration);
        activeBuffs.add(buff);

        // Recalculate cached values
        recalculateBuffs();

        GameLogger.logInfo("Added " + type + " buff: +" + amount + " for " + duration + " turns");
    }

    /**
     * Updates all buffs, reducing their durations and removing expired ones
     * @return True if any buffs were removed
     */
    public boolean updateBuffs() {
        boolean buffRemoved = false;

        // Check each buff
        for (int i = activeBuffs.size - 1; i >= 0; i--) {
            StatBuff buff = activeBuffs.get(i);
            buff.decrementTurns();

            if (buff.isExpired()) {
                activeBuffs.removeIndex(i);
                buffRemoved = true;
                GameLogger.logInfo(buff.getType() + " buff expired");
            }
        }

        // If any buffs were removed, recalculate
        if (buffRemoved) {
            recalculateBuffs();
        }

        return buffRemoved;
    }

    /**
     * Recalculates the cached buff values
     */
    private void recalculateBuffs() {
        cachedAttackBuff = 0;
        cachedDefenseBuff = 0;
        cachedCritRateBuff = 0;

        for (StatBuff buff : activeBuffs) {
            switch (buff.getType()) {
                case ATTACK:
                    cachedAttackBuff += buff.getAmount();
                    break;
                case DEFENSE:
                    cachedDefenseBuff += buff.getAmount();
                    break;
                case CRITICAL_RATE:
                    cachedCritRateBuff += buff.getAmount() / 100f; // Convert from percentage
                    break;
            }
        }
    }

    /**
     * @return The current total attack buff amount
     */
    public int getAttackBuff() {
        return cachedAttackBuff;
    }

    /**
     * @return The current total defense buff amount
     */
    public int getDefenseBuff() {
        return cachedDefenseBuff;
    }

    /**
     * @return The current total critical rate buff amount (as a decimal)
     */
    public float getCritRateBuff() {
        return cachedCritRateBuff;
    }

    /**
     * @return The list of all active buffs
     */
    public Array<StatBuff> getActiveBuffs() {
        return new Array<>(activeBuffs); // Return a copy for safety
    }

    /**
     * Remove all active buffs
     */
    public void clearBuffs() {
        activeBuffs.clear();
        cachedAttackBuff = 0;
        cachedDefenseBuff = 0;
        cachedCritRateBuff = 0;
//        GameLogger.logInfo("All buffs cleared");
    }

    /**
     * Remove a specific buff
     * @param buffToRemove The specific buff to remove
     */
    public void removeBuff(StatBuff buffToRemove) {
        activeBuffs.removeValue(buffToRemove, true);
        // Recalculate cached buff values
        recalculateBuffs();
        GameLogger.logInfo("Removed " + buffToRemove.getType() + " buff of " + buffToRemove.getAmount());
    }
}
