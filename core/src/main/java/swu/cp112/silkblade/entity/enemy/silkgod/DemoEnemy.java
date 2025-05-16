package swu.cp112.silkblade.entity.enemy.silkgod;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.entity.enemy.AbstractEnemy;
import swu.cp112.silkblade.pattern.silkgod.*;

public class DemoEnemy extends AbstractEnemy {
    private Player player;
    private boolean isEvolved = false;

    public DemoEnemy() {
        super("???????", 500,
            new Texture(Gdx.files.internal("enemy.png")),
            350f, 325f);

        // Initialize components
        this.player = Player.loadFromFile();

        // Add all patterns first
        // IMPORTANT: The order matters for pattern rotation
        // We're NOT calling initializePatterns() here anymore to avoid selecting a pattern too early
        // The first pattern will be selected when combat actually starts in startTurn()
        this.addAttackPattern(new TelegraphPattern());
        this.addAttackPattern(new CrossfirePattern());
        this.addAttackPattern(new ConvergingStarPattern());
        this.addAttackPattern(new RotatingStarPattern());
        this.addAttackPattern(new HomingExplosionPattern());
        this.addAttackPattern(new FallenStarPattern());
        // Don't initialize patterns yet - let combat scene handle it during startTurn

        // Initialize remaining properties
        initializeEnemy();
    }

    private void initializeEnemy() {
        // Set base rewards
        this.setBaseRewards(100000, 1000000);

        // Scale to player level
        scaleToPlayerLevel(player.getLevel());

        // Set primary color and hit sound
        this.primaryColor = Color.FIREBRICK;
        this.setHitSound("sounds/hit.wav");

        // Initialize dialogues
        initializeDialogues();
    }

    private void initializeDialogues() {
        this.encounterDialogue = this.getName() + " appears!";
        this.clearRandomTurnDialogues();

        // Add random turn dialogues
        String[] dialogues = {
            ". . . . ."
        };

        for (String dialogue : dialogues) {
            this.addRandomTurnDialogue(dialogue);
        }
    }

    @Override
    protected void selectNewPattern() {
        // Check if health is below 50% and we haven't evolved yet
//        if (!isEvolved && getCurrentHP() <= getMaxHP() * 0.5f) {
//            isEvolved = true;
//            // Clear existing patterns
//            patternManager.clearPatterns();
//            // Add evolved patterns
//            this.addAttackPattern(new TelegraphPattern());
//            this.addAttackPattern(new CrossfirePattern());
//            this.addAttackPattern(new ConvergingStarPattern());
//            this.addAttackPattern(new RotatingStarPattern());
//            this.addAttackPattern(new HomingExplosionPattern());
//            // Add evolved dialogue
//            this.attackDialogue = "The Silk Overlord's power surges!";
//            this.addRandomTurnDialogue("The Silk Overlord's power continues to grow!");
//        }

        // Select pattern using parent class method
        super.selectNewPattern();
    }
}
