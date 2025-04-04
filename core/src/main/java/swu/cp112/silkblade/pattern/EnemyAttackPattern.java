package swu.cp112.silkblade.pattern;

import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.Enemy;

import java.util.List;

/**
 * Represents an attack pattern strategy for enemies.
 * Allows for modular and flexible attack pattern design.
 */
public interface EnemyAttackPattern {
    /**
     * Generate bullets for this specific attack pattern.
     *
     * @param enemy The enemy executing the attack
     * @param arenaX Arena's X position
     * @param arenaY Arena's Y position
     * @param arenaWidth Arena width
     * @param arenaHeight Arena height
     * @return List of generated bullets
     */
    List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                 float arenaWidth, float arenaHeight);

    /**
     * Get the name of the attack pattern.
     *
     * @return Pattern name as a string
     */
    String getPatternName();

    /**
     * Get configuration parameters for this attack pattern.
     *
     * @return AttackPatternConfig containing pattern details
     */
    AttackPatternConfig getConfig();
}
