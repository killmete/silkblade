package swu.cp112.silkblade.entity.item;

import swu.cp112.silkblade.entity.combat.BuffManager;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Handles the application of item effects to entities.
 * This class contains the logic for applying consumable item effects to players.
 */
public class ItemEffectSystem {
    
    /**
     * Applies the effect of a consumable item to the player
     * @param player The player to apply the effect to
     * @param item The consumable item to apply
     * @param skipConsumption True to skip decreasing the quantity (useful in combat)
     * @return True if the item was used successfully, false otherwise
     */
    public static boolean applyItemEffect(Player player, ConsumableItem item, boolean skipConsumption) {
        if (item == null || player == null) {
            return false;
        }
        
        // Item is consumed only if not skipped
        boolean consumed = true;
        if (!skipConsumption) {
            consumed = item.decreaseQuantity(1);
            if (!consumed) {
                GameLogger.logInfo("Could not consume item: " + item.getName() + " (Quantity: " + item.getQuantity() + ")");
                return false;
            }
        }
        
        // Apply primary effect
        applyEffect(player, item.getEffect(), item.getEffectAmount());
        
        // Apply secondary effect if it exists
        if (item.getSecondaryEffect() != null) {
            applyEffect(player, item.getSecondaryEffect(), item.getSecondaryEffectAmount());
        }
        
        // Apply buffs if they exist
        if (item.getBuffDuration() > 0) {
            applyBuffs(player, item.getBuffDuration(), item.getBuffAtkAmount(), item.getBuffDefAmount());
        }
        
        GameLogger.logInfo("Applied item effect: " + item.getName() + " to player");
        return true;
    }
    
    /**
     * Applies the effect of a consumable item to the player (consuming the item)
     * @param player The player to apply the effect to
     * @param item The consumable item to apply
     * @return True if the item was used successfully, false otherwise
     */
    public static boolean applyItemEffect(Player player, ConsumableItem item) {
        return applyItemEffect(player, item, false);
    }
    
    /**
     * Apply a specific effect to the player
     * @param player The player to apply the effect to
     * @param effect The effect type
     * @param amount The amount of the effect
     */
    private static void applyEffect(Player player, ConsumableItem.ItemEffect effect, int amount) {
        if (effect == null) {
            return;
        }
        
        switch (effect) {
            case HEAL_HP:
                player.heal(amount);
                break;
                
            case RESTORE_MP:
                player.increaseMP(amount);
                break;
                
            case FULL_HEAL:
                player.fullHeal();
                break;
                
            case FULL_RESTORE:
                player.fullRestore();
                break;
                
            case BUFF_ATK:
            case BUFF_DEF:
                // These are handled by applyBuffs
                break;
        }
    }
    
    /**
     * Apply temporary buffs to the player
     * @param player The player to apply buffs to
     * @param duration The duration of the buffs in turns
     * @param atkAmount The amount to increase attack by
     * @param defAmount The amount to increase defense by
     */
    private static void applyBuffs(Player player, int duration, int atkAmount, int defAmount) {
        if (atkAmount > 0) {
            player.addStatBuff(BuffManager.StatType.ATTACK, atkAmount, duration);
        }
        
        if (defAmount > 0) {
            player.addStatBuff(BuffManager.StatType.DEFENSE, defAmount, duration);
        }
    }
} 