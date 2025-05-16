package swu.cp112.silkblade.pattern;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

/**
 * Manages attack patterns for enemies, allowing dynamic selection
 * and management of different attack strategies.
 */
public class EnemyAttackPatternManager {
    private List<EnemyAttackPattern> availablePatterns;
    private List<EnemyAttackPattern> unusedPatterns;
    private EnemyAttackPattern currentPattern;

    /**
     * Constructor that initializes with a default set of patterns.
     */
    public EnemyAttackPatternManager() {
        availablePatterns = new ArrayList<>();
        unusedPatterns = new ArrayList<>();
    }

    /**
     * Add a new attack pattern to the available patterns.
     *
     * @param pattern The attack pattern to add
     */
    public void addPattern(EnemyAttackPattern pattern) {
        availablePatterns.add(pattern);
        unusedPatterns.add(pattern);
    }

    /**
     * Randomly select a new attack pattern.
     *
     * @return The selected attack pattern
     */
    public EnemyAttackPattern selectRandomPattern() {
        if (availablePatterns.isEmpty()) {
            throw new IllegalStateException("No attack patterns available");
        }

        // If all patterns have been used, reset the unused patterns list
        if (unusedPatterns.isEmpty()) {
            unusedPatterns.addAll(availablePatterns);
            swu.cp112.silkblade.util.GameLogger.logInfo("All patterns exhausted, resetting pattern pool with " + 
                                                      availablePatterns.size() + " available patterns");
        }

        // Get the previous pattern before selecting a new one
        EnemyAttackPattern previousPattern = currentPattern;
        
        // Select a random pattern
        int randomIndex = MathUtils.random(0, unusedPatterns.size() - 1);
        currentPattern = unusedPatterns.remove(randomIndex);
        
        // If we only had one pattern, immediately reset the pool for next selection
        if (unusedPatterns.isEmpty()) {
            unusedPatterns.addAll(availablePatterns);
            // Don't include the pattern we just selected to avoid immediate repetition
            unusedPatterns.remove(currentPattern);
            swu.cp112.silkblade.util.GameLogger.logInfo("Last pattern used, resetting pattern pool with " + 
                                                      unusedPatterns.size() + " available patterns");
        }
        
        // Log the selected pattern
        swu.cp112.silkblade.util.GameLogger.logInfo("Selected pattern: " + currentPattern.getPatternName() + 
                                                   " (" + unusedPatterns.size() + " unused patterns remaining)");

        return currentPattern;
    }

    /**
     * Get the current attack pattern.
     *
     * @return The current attack pattern
     */
    public EnemyAttackPattern getCurrentPattern() {
        return currentPattern;
    }

    /**
     * Remove a specific attack pattern.
     *
     * @param pattern The pattern to remove
     */
    public void removePattern(EnemyAttackPattern pattern) {
        availablePatterns.remove(pattern);
        unusedPatterns.remove(pattern);
    }

    /**
     * Clear all available patterns.
     */
    public void clearPatterns() {
        availablePatterns.clear();
        unusedPatterns.clear();
    }
}
