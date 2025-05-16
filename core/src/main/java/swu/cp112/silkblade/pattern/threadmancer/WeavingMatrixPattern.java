package swu.cp112.silkblade.pattern.threadmancer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import swu.cp112.silkblade.entity.combat.Bullet;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.entity.enemy.Enemy;
import swu.cp112.silkblade.pattern.AttackPatternConfig;
import swu.cp112.silkblade.pattern.EnemyAttackPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * WeavingMatrixPattern - The third attack pattern for the Threadmancer boss
 * Creates an intricate, grid-like pattern of threads that weave together,
 * forming a mesmerizing and dangerous matrix that requires strategic
 * movement to navigate safely.
 *
 * Pattern consists of:
 * 1. Thread grids that form and move across the arena
 * 2. Weaving intersections that create temporary safe zones
 * 3. Pulsing thread intensity that provides visual rhythm
 */
public class WeavingMatrixPattern implements EnemyAttackPattern {
    // Pattern configuration
    private static final AttackPatternConfig CONFIG = new AttackPatternConfig(
        10, 15, 3.0f, 1.1f, 24, 400f, 400f,
        new Color(0.2f, 0.8f, 0.5f, 1.0f), false,
        "Weaving Matrix", 2.5f
    );

    // Pattern timing
    private int spawnCycle = 0;
    private int patternPhase = 0;
    private static final int PHASES = 3;
    private static final int MATRIX_SPAWN_INTERVAL = 1;
    private float patternTimer = 0f;

    // Matrix parameters
    private static final float THREAD_SPEED = 980f;
    private static final float TELEGRAPH_TIME = 1.1f;
    private boolean horizontalFirst = true;

    // Grid parameters
    private static final int MIN_GRID_LINES = 6;
    private static final int MAX_GRID_LINES = 9;
    private static final float GRID_SPACING_MIN = 40f;
    private static final float GRID_SPACING_MAX = 70f;

    @Override
    public List<Bullet> generateBullets(Enemy enemy, float arenaX, float arenaY,
                                       float arenaWidth, float arenaHeight) {
        List<Bullet> bullets = new ArrayList<>();

        // Get player position for targeting
        float playerX = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerX() :
            arenaX + arenaWidth / 2;
        float playerY = enemy instanceof AbstractEnemy ?
            ((AbstractEnemy) enemy).getLastPlayerY() :
            arenaY + arenaHeight / 2;

        // Increment spawn cycle and update pattern timer
        spawnCycle++;
        patternTimer += 0.05f;
        if (patternTimer > MathUtils.PI2) {
            patternTimer -= MathUtils.PI2;
        }

        // Generate bullets based on spawn cycle
        // Calculate damage based on enemy's attack damage
        float baseDamage = MathUtils.random(CONFIG.getMinDamage(), CONFIG.getMaxDamage());
        float scaledDamage = baseDamage * (1.0f + (enemy.getAttackDamage() * 0.1f));

        // Generate different patterns based on phase
        switch (patternPhase) {
            case 0:
                generateOrthogonalGrid(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight, scaledDamage);
                break;
            case 1:
                generateWeavingGrid(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight, scaledDamage);
                break;
            case 2:
                generatePulsingMatrix(bullets, enemy, playerX, playerY, arenaWidth, arenaHeight, scaledDamage);
                break;
        }

        // Toggle horizontal-first flag for variety
        horizontalFirst = !horizontalFirst;

        // Cycle to next phase
        patternPhase = (patternPhase + 1) % PHASES;

        return bullets;
    }

    /**
     * Generates an orthogonal grid of threads (horizontal and vertical)
     */
    private void generateOrthogonalGrid(List<Bullet> bullets, Enemy enemy,
                                      float playerX, float playerY,
                                      float arenaWidth, float arenaHeight,
                                      float damage) {
        // Create orthogonal grid (horizontal and vertical lines)

        // Calculate number of grid lines and spacing
        int horizontalLines = MathUtils.random(MIN_GRID_LINES, MAX_GRID_LINES);
        int verticalLines = MathUtils.random(MIN_GRID_LINES, MAX_GRID_LINES);

        float horizontalSpacing = MathUtils.random(GRID_SPACING_MIN, GRID_SPACING_MAX);
        float verticalSpacing = MathUtils.random(GRID_SPACING_MIN, GRID_SPACING_MAX);

        // Center the grid around the player with some offset
        float gridOffsetX = MathUtils.random(-50f, 50f);
        float gridOffsetY = MathUtils.random(-50f, 50f);

        float gridCenterX = playerX + gridOffsetX;
        float gridCenterY = playerY + gridOffsetY;

        // Calculate grid boundaries
        float gridStartX = gridCenterX - (horizontalLines * horizontalSpacing / 2);
        float gridStartY = gridCenterY - (verticalLines * verticalSpacing / 2);

        // Create horizontal lines first or vertical lines first based on flag
        if (horizontalFirst) {
            // Create horizontal lines
            createHorizontalLines(bullets, horizontalLines, gridStartX, gridStartY,
                                 horizontalSpacing, verticalLines * verticalSpacing, damage);

            // Create vertical lines with delay
            createVerticalLines(bullets, verticalLines, gridStartX, gridStartY,
                               verticalSpacing, horizontalLines * horizontalSpacing, damage, 0.3f);
        } else {
            // Create vertical lines
            createVerticalLines(bullets, verticalLines, gridStartX, gridStartY,
                               verticalSpacing, horizontalLines * horizontalSpacing, damage, 0f);

            // Create horizontal lines with delay
            createHorizontalLines(bullets, horizontalLines, gridStartX, gridStartY,
                                 horizontalSpacing, verticalLines * verticalSpacing, damage, 0.3f);
        }
    }

    /**
     * Creates horizontal lines for the grid
     */
    private void createHorizontalLines(List<Bullet> bullets, int lines, float startX, float startY,
                                     float spacing, float length, float damage) {
        createHorizontalLines(bullets, lines, startX, startY, spacing, length, damage, 0f);
    }

    /**
     * Creates horizontal lines for the grid with optional telegraph delay
     */
    private void createHorizontalLines(List<Bullet> bullets, int lines, float startX, float startY,
                                     float spacing, float length, float damage, float delayOffset) {
        for (int i = 0; i < lines; i++) {
            float lineY = startY + i * spacing;

            // Create thread bullet for this line
            Bullet bullet = new Bullet(
                damage,
                startX, lineY,
                THREAD_SPEED, 0,
                5f,
                new Color(0.2f, 0.8f, 0.5f, 0.9f),
                false
            );

            // Style the bullet as a horizontal thread
            stylizeMatrixBullet(bullet, 0.8f);

            // Set telegraph time with offset if specified
            bullet.enableTelegraphing(TELEGRAPH_TIME + delayOffset, 0.2f);

            // Set thread to travel the entire length of the grid

            bullets.add(bullet);
        }
    }

    /**
     * Creates vertical lines for the grid
     */
    private void createVerticalLines(List<Bullet> bullets, int lines, float startX, float startY,
                                   float spacing, float length, float damage) {
        createVerticalLines(bullets, lines, startX, startY, spacing, length, damage, 0f);
    }

    /**
     * Creates vertical lines for the grid with optional telegraph delay
     */
    private void createVerticalLines(List<Bullet> bullets, int lines, float startX, float startY,
                                   float spacing, float length, float damage, float delayOffset) {
        for (int i = 0; i < lines; i++) {
            float lineX = startX + i * spacing;

            // Create thread bullet for this line
            Bullet bullet = new Bullet(
                damage,
                lineX, startY,
                0, THREAD_SPEED,
                5f,
                new Color(0.2f, 0.8f, 0.5f, 0.9f),
                false
            );

            // Style the bullet as a vertical thread
            stylizeMatrixBullet(bullet, 0.8f);

            // Set telegraph time with offset if specified
            bullet.enableTelegraphing(TELEGRAPH_TIME + delayOffset, 0.2f);

            bullets.add(bullet);
        }
    }

    /**
     * Generates a weaving grid where threads intertwine in a more complex pattern
     */
    private void generateWeavingGrid(List<Bullet> bullets, Enemy enemy,
                                   float playerX, float playerY,
                                   float arenaWidth, float arenaHeight,
                                   float damage) {
        // Create a weaving grid with diagonal threads

        // Calculate number of diagonal lines
        int diagonalLines = MathUtils.random(MIN_GRID_LINES, MAX_GRID_LINES);
        float spacing = MathUtils.random(GRID_SPACING_MIN, GRID_SPACING_MAX);

        // Calculate grid size based on diagonal lines and spacing
        float gridSize = diagonalLines * spacing;

        // Center grid around player with small random offset
        float gridCenterX = playerX + MathUtils.random(-30f, 30f);
        float gridCenterY = playerY + MathUtils.random(-30f, 30f);

        float gridStartX = gridCenterX - gridSize / 2;
        float gridStartY = gridCenterY - gridSize / 2;

        // Create diagonal lines in both directions (top-left to bottom-right)
        for (int i = -1; i <= diagonalLines; i++) {
            float startX = gridStartX + i * spacing;
            float startY = gridStartY;

            // Skip some lines randomly for partial grid
            if (MathUtils.randomBoolean(0.3f)) continue;

            // Calculate direction vector for diagonal
            float dirX = THREAD_SPEED * 0.7f;
            float dirY = THREAD_SPEED * 0.7f;

            // Create diagonal thread bullet
            Bullet bullet = new Bullet(
                damage,
                startX, startY,
                dirX, dirY,
                5f,
                new Color(0.3f, 0.9f, 0.4f, 0.9f),
                false
            );

            // Style bullet
            stylizeMatrixBullet(bullet, 0.7f);
            bullet.enableTelegraphing(TELEGRAPH_TIME, 0.15f);

            // Set max travel distance to ensure thread spans the grid
            float diagonalLength = (float)Math.sqrt(2 * gridSize * gridSize);

            bullets.add(bullet);
        }

        // Create diagonal lines in opposite direction (top-right to bottom-left)
        for (int i = -1; i <= diagonalLines; i++) {
            float startX = gridStartX + gridSize + i * spacing;
            float startY = gridStartY;

            // Skip some lines randomly for partial grid
            if (MathUtils.randomBoolean(0.3f)) continue;

            // Calculate direction vector for diagonal
            float dirX = -THREAD_SPEED * 0.7f;
            float dirY = THREAD_SPEED * 0.7f;

            // Create diagonal thread bullet
            Bullet bullet = new Bullet(
                damage,
                startX, startY,
                dirX, dirY,
                5f,
                new Color(0.3f, 0.9f, 0.4f, 0.9f),
                false
            );

            // Style bullet
            stylizeMatrixBullet(bullet, 0.7f);
            bullet.enableTelegraphing(TELEGRAPH_TIME * 0.8f, 0.15f);

            bullets.add(bullet);
        }
    }

    /**
     * Generates a pulsing matrix of threads with varying intensity
     */
    private void generatePulsingMatrix(List<Bullet> bullets, Enemy enemy,
                                     float playerX, float playerY,
                                     float arenaWidth, float arenaHeight,
                                     float damage) {
        // Create multiple pulsing matrices that emanate from different points

        // Number of burst centers to create (4-5)
        int numBursts = MathUtils.random(4, 5);
        
        // Threads per burst
        int threadsPerBurst = MathUtils.random(8, 10);
        
        // Track previous burst positions to ensure distribution
        List<float[]> burstPositions = new ArrayList<>();

        // Create multiple bursts around the arena
        for (int burst = 0; burst < numBursts; burst++) {
            // Determine burst position - mix of positions near player and elsewhere in arena
            float burstCenterX, burstCenterY;
            
            if (burst == 0) {
                // First burst is always near the player
                burstCenterX = playerX + MathUtils.random(-50f, 50f);
                burstCenterY = playerY + MathUtils.random(-50f, 50f);
            } else {
                // Other bursts are at different places in the arena
                // For better distribution, use different positioning approaches based on burst index
                boolean positionValid = false;
                int attempts = 0;
                
                do {
                    attempts++;
                    
                    // Try different positioning strategies based on burst number or random selection
                    float positionType = MathUtils.random(3); // 0-3 different positioning types
                    
                    if (positionType < 1) {
                        // Position relative to player with random distance and angle
                        float distance = MathUtils.random(100f, 280f);
                        float angle = MathUtils.random(MathUtils.PI2);
                        
                        burstCenterX = playerX + MathUtils.cos(angle) * distance;
                        burstCenterY = playerY + MathUtils.sin(angle) * distance;
                    } 
                    else if (positionType < 2) {
                        // Position in a random quadrant of the arena
                        burstCenterX = MathUtils.random(0, 1) < 0.5f ? 
                            MathUtils.random(50f, arenaWidth/2 - 50f) : 
                            MathUtils.random(arenaWidth/2 + 50f, arenaWidth - 50f);
                            
                        burstCenterY = MathUtils.random(0, 1) < 0.5f ? 
                            MathUtils.random(50f, arenaHeight/2 - 50f) : 
                            MathUtils.random(arenaHeight/2 + 50f, arenaHeight - 50f);
                    }
                    else if (positionType < 3) {
                        // Fully random position in arena
                        burstCenterX = MathUtils.random(50f, arenaWidth - 50f);
                        burstCenterY = MathUtils.random(50f, arenaHeight - 50f);
                    }
                    else {
                        // Position along arena edge/border
                        if (MathUtils.randomBoolean()) {
                            // Position along horizontal edge
                            burstCenterX = MathUtils.random(50f, arenaWidth - 50f);
                            burstCenterY = MathUtils.randomBoolean() ? 
                                MathUtils.random(50f, 100f) : 
                                MathUtils.random(arenaHeight - 100f, arenaHeight - 50f);
                        } else {
                            // Position along vertical edge
                            burstCenterX = MathUtils.randomBoolean() ? 
                                MathUtils.random(50f, 100f) : 
                                MathUtils.random(arenaWidth - 100f, arenaWidth - 50f);
                            burstCenterY = MathUtils.random(50f, arenaHeight - 50f);
                        }
                    }
                    
                    // Make sure position is within bounds
                    burstCenterX = MathUtils.clamp(burstCenterX, 50f, arenaWidth - 50f);
                    burstCenterY = MathUtils.clamp(burstCenterY, 50f, arenaHeight - 50f);
                    
                    // Check minimum distance from existing bursts to avoid overlap
                    positionValid = true;
                    for (float[] pos : burstPositions) {
                        float dx = pos[0] - burstCenterX;
                        float dy = pos[1] - burstCenterY;
                        float distSquared = dx*dx + dy*dy;
                        
                        // If too close to another burst, reject position
                        if (distSquared < 10000) { // 100^2, representing minimum 100 unit distance
                            positionValid = false;
                            break;
                        }
                    }
                } while (!positionValid && attempts < 10); // Limit attempts to avoid infinite loop
                
                // If we couldn't find a valid position after max attempts, just use random position
                if (!positionValid) {
                    burstCenterX = MathUtils.random(50f, arenaWidth - 50f);
                    burstCenterY = MathUtils.random(50f, arenaHeight - 50f);
                }
            }
            
            // Save this burst position
            burstPositions.add(new float[]{burstCenterX, burstCenterY});

            // Create threads that emanate from this burst center
            for (int i = 0; i < threadsPerBurst; i++) {
                // Calculate angle with oscillation for visual interest
                float baseAngle = (i * MathUtils.PI2 / threadsPerBurst);
                float pulseOffset = 0.2f * MathUtils.sin(patternTimer * 3 + i * 0.5f + burst);
                float angle = baseAngle + pulseOffset;

                // All threads spawn at this burst center
                float spawnX = burstCenterX;
                float spawnY = burstCenterY;

                // Threads move outward from center
                float dirX = MathUtils.cos(angle) * THREAD_SPEED * 0.8f;
                float dirY = MathUtils.sin(angle) * THREAD_SPEED * 0.8f;

                // Vary intensity based on burst ID and thread position
                float intensityBase = 0.6f + 0.4f * MathUtils.sin(patternTimer * 2 + i * 0.3f + burst * 0.5f);

                // Create bullet with color variation based on burst ID
                float hueShift = burst * 0.2f % 1.0f; // Keep hue shift within 0-1 range
                Color bulletColor = new Color(
                    MathUtils.clamp(0.2f + hueShift, 0f, 1f),
                    MathUtils.clamp(0.9f - hueShift, 0f, 1f),
                    MathUtils.clamp(0.6f, 0f, 1f),
                    0.8f
                );

                // Create the bullet
                Bullet bullet = new Bullet(
                    damage,
                    spawnX, spawnY,
                    dirX, dirY,
                    5f,
                    bulletColor,
                    false
                );

                // Style the bullet as a pulsing thread
                stylizeMatrixBullet(bullet, intensityBase);
                
                // Stagger telegraph timing slightly between bursts
                float telegraphMultiplier = 1.1f - (0.1f * burst);
                // Ensure minimum telegraph time for playability
                telegraphMultiplier = Math.max(telegraphMultiplier, 0.7f);
                bullet.enableTelegraphing(TELEGRAPH_TIME * telegraphMultiplier, 0.1f);

                bullets.add(bullet);
            }
        }
    }

    /**
     * Applies consistent matrix thread visual styling to bullets
     */
    private void stylizeMatrixBullet(Bullet bullet, float intensity) {
        bullet.setShape(Bullet.Shape.SQUARE);
        bullet.setTrailLength(18);
        bullet.setGlowing(true);
        bullet.setGlowIntensity(intensity);
    }

    @Override
    public String getPatternName() {
        return CONFIG.getPatternDescription();
    }

    @Override
    public AttackPatternConfig getConfig() {
        return CONFIG;
    }
}
