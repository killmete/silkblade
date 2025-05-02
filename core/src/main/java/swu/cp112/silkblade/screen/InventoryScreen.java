package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import swu.cp112.silkblade.core.Main;
import swu.cp112.silkblade.entity.combat.Player;
import swu.cp112.silkblade.entity.item.ConsumableItem;
import swu.cp112.silkblade.entity.item.Equipment;
import swu.cp112.silkblade.entity.item.Inventory;
import swu.cp112.silkblade.screen.transition.ScreenTransition;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Screen for managing equipment and items.
 */
public class InventoryScreen implements Screen {
    /**
     * Display configuration
     */
    private static final class DisplayConfig {
        // Layout
        static final float TITLE_Y_POSITION = 60;
        static final float PLAYER_STATS_START_Y = 400;
        static final float PLAYER_STATS_SPACING = 35;
        static final float SECTION_TITLE_Y = 150; // Aligned with player stats
        static final float ITEMS_START_Y = 200; // Moved up to align with stats display
        static final float ITEMS_SPACING_Y = 40;
        static final float DESCRIPTION_Y = 350;
        static final float DESCRIPTION_HEIGHT = 280;
        static final float NAVIGATION_HELP_Y = 40;
        static final float LEFT_MARGIN = 80;
        static final float RIGHT_MARGIN = 640;
        static final float EQUIPPED_ITEM_NAME_OFFSET = 250;
        static final float FONT_SCALE = 1.7f;
        static final float ITEM_FONT_SCALE = 1.3f;
        static final float STATS_FONT_SCALE = 1.1f;
        static final float DESC_FONT_SCALE = 1.0f;
        static final int MAX_VISIBLE_ITEMS = 10;

        // Colors
        static final Color TITLE_COLOR = Color.WHITE;
        static final Color PLAYER_INFO_COLOR = Color.CYAN;
        static final Color SELECTED_COLOR = Color.YELLOW;
        static final Color EQUIPPED_COLOR = Color.GREEN;
        static final Color DEFAULT_COLOR = Color.WHITE;
        static final Color CONSUMABLE_COLOR = Color.WHITE;
        static final Color COMBAT_ITEM_COLOR = Color.ORANGE; // New color for combat-selected items
        static final Color DESCRIPTION_COLOR = Color.LIGHT_GRAY;
        static final Color NAVIGATION_COLOR = Color.GRAY;
        static final Color BACKGROUND_COLOR = Color.BLACK;
        static final Color POSITIVE_BONUS_COLOR = Color.GREEN;
        static final Color NEGATIVE_BONUS_COLOR = Color.RED;
        static final Color STATS_LABEL_COLOR = Color.LIGHT_GRAY;
        static final Color DESCRIPTION_BACKGROUND = new Color(0, 0, 0, 0.8f);
        static final Color SCROLL_INDICATOR_COLOR = Color.YELLOW;
    }

    /**
     * Menu configuration
     */
    private static final class MenuConfig {
        static final String TITLE = "INVENTORY";
        static final String EQUIPPED_TITLE = "EQUIPPED";
        static final String ITEMS_TITLE = "ITEMS";
        static final String EMPTY_SLOT = "- Empty -";
        static final String NAVIGATION_HELP = "Arrow Keys: Navigate | Enter: Equip/Use | E: Examine | C: Toggle for Combat | Esc: Back";
        static final float STAR_SIZE = 26f; // Size for star icon
        static final float STAR_X_OFFSET = 10f; // Small offset from the end of the name text
        static final float STAR_Y_OFFSET = 0f; // Vertical offset for star
    }

    /**
     * Audio configuration
     */
    private static final class AudioConfig {
        static final float MUSIC_VOLUME = 0.05f;
        static final String FONT_PATH = "fonts/DTM.fnt";
        static final String MUSIC_PATH = "music/main_menu.mp3";
        static final String SELECT_SOUND_PATH = "sounds/select.wav";
        static final String EQUIP_SOUND_PATH = "sounds/select.wav";
        static final String USE_SOUND_PATH = "sounds/heal.wav";
        static final String STAR_TEXTURE_PATH = "star.png";
    }

    /**
     * Core components
     */
    private final Game game;
    private final FitViewport viewport;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Player player;
    private final Inventory inventory;
    private final com.badlogic.gdx.graphics.Texture starTexture;

    /**
     * Audio components
     */
    private final Sound selectSound;
    private final Sound equipSound;
    private final Sound useSound;
    // Keep for future reference but comment out
    // private final Music music;

    /**
     * State
     */
    private boolean isLeftSide = true; // true: equipped items, false: inventory items
    private int selectedIndexLeft = 0;
    private int selectedIndexRight = 0;
    private int topItemIndex = 0; // For scrolling inventory
    private boolean examiningItem = false;
    private String itemDescription = "";
    private boolean inputEnabled = true;
    private final com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;

    private float genesisSineTime = 0;

    // Added to track the item tier for the examine box outline
    private swu.cp112.silkblade.entity.item.ItemTier currentExaminedItemTier = null;

    public InventoryScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();

        this.batch = new SpriteBatch();
        this.font = new BitmapFont(Gdx.files.internal(AudioConfig.FONT_PATH));
        font.setColor(DisplayConfig.DEFAULT_COLOR);
        font.getData().setScale(DisplayConfig.FONT_SCALE);

        this.selectSound = Gdx.audio.newSound(Gdx.files.internal(AudioConfig.SELECT_SOUND_PATH));
        this.equipSound = Gdx.audio.newSound(Gdx.files.internal(AudioConfig.EQUIP_SOUND_PATH));
        this.useSound = Gdx.audio.newSound(Gdx.files.internal(AudioConfig.USE_SOUND_PATH));
        // Keep for future reference but comment out
        // this.music = Gdx.audio.newMusic(Gdx.files.internal(AudioConfig.MUSIC_PATH));
        // this.music.setLooping(true);
        // this.music.setVolume(AudioConfig.MUSIC_VOLUME);

        // Load star texture
        this.starTexture = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal(AudioConfig.STAR_TEXTURE_PATH));

        // Initialize ShapeRenderer
        this.shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();

        // Load player data
        this.player = Player.loadFromFile();
        this.inventory = player.getInventory();

        // Sort inventory items
        sortInventoryItems();
    }

    /**
     * Sort inventory items - first by type (weapon, armor, accessory, consumable),
     * then by rarity (Genesis to Normal), and finally alphabetically within each tier.
     */
    private void sortInventoryItems() {
        // Create temporary arrays to hold each category
        Array<Equipment> weapons = new Array<>();
        Array<Equipment> armors = new Array<>();
        Array<Equipment> accessories = new Array<>();

        // Categorize equipment by type
        for (Equipment item : inventory.getInventoryItems()) {
            switch (item.getType()) {
                case WEAPON:
                    weapons.add(item);
                    break;
                case ARMOR:
                    armors.add(item);
                    break;
                case ACCESSORY:
                    accessories.add(item);
                    break;
            }
        }

        // Sort each category by rarity (highest to lowest) and then alphabetically
        sortByRarityAndName(weapons);
        sortByRarityAndName(armors);
        sortByRarityAndName(accessories);

        // Clear inventory and add items back in the correct order
        Array<Equipment> originalItems = new Array<>(inventory.getInventoryItems());
        inventory.getInventoryItems().clear();

        // Add back in the right order: weapons, armors, accessories
        inventory.getInventoryItems().addAll(weapons);
        inventory.getInventoryItems().addAll(armors);
        inventory.getInventoryItems().addAll(accessories);

        // Sort consumables by rarity and name
        sortConsumablesByRarityAndName(inventory.getConsumableItems());
    }

    /**
     * Sort equipment by rarity (highest to lowest) and then alphabetically by name
     */
    private void sortByRarityAndName(Array<Equipment> items) {
        items.sort(new java.util.Comparator<Equipment>() {
            @Override
            public int compare(Equipment item1, Equipment item2) {
                // First sort by rarity (highest to lowest)
                int rarityCompare = item2.getTier().ordinal() - item1.getTier().ordinal();
                if (rarityCompare != 0) {
                    return rarityCompare;
                }

                // If same rarity, sort alphabetically
                return item1.getName().compareTo(item2.getName());
            }
        });
    }

    /**
     * Sort consumables by rarity (highest to lowest) and then alphabetically by name
     */
    private void sortConsumablesByRarityAndName(Array<ConsumableItem> items) {
        items.sort(new java.util.Comparator<ConsumableItem>() {
            @Override
            public int compare(ConsumableItem item1, ConsumableItem item2) {
                // First sort by rarity (highest to lowest)
                int rarityCompare = item2.getTier().ordinal() - item1.getTier().ordinal();
                if (rarityCompare != 0) {
                    return rarityCompare;
                }

                // If same rarity, sort alphabetically
                return item1.getName().compareTo(item2.getName());
            }
        });
    }

    /**
     * Rendering methods
     */
    @Override
    public void render(float delta) {
        // Update rainbow color animation time for Genesis tier items
        genesisSineTime += delta * 2.0f; // Speed of the rainbow cycle
        if (genesisSineTime > Math.PI * 2) {
            genesisSineTime -= Math.PI * 2;
        }

        clearScreen();
        drawScreen();
        if (inputEnabled && !ScreenTransition.isTransitioning()) {
            handleInput();
        }
    }

    private void clearScreen() {
        ScreenUtils.clear(DisplayConfig.BACKGROUND_COLOR);
    }

    private void drawScreen() {
        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw title
        font.setColor(DisplayConfig.TITLE_COLOR);
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, MenuConfig.TITLE);
        font.draw(batch, MenuConfig.TITLE,
                screenWidth / 2 - layout.width / 2,
                screenHeight - DisplayConfig.TITLE_Y_POSITION);

        // Draw player stats
        drawPlayerStats(screenHeight);

        // Draw section titles
        float originalScale = font.getData().scaleX;
        font.getData().setScale(DisplayConfig.ITEM_FONT_SCALE);

        font.setColor(isLeftSide ? DisplayConfig.SELECTED_COLOR : DisplayConfig.DEFAULT_COLOR);
        font.draw(batch, MenuConfig.EQUIPPED_TITLE,
                DisplayConfig.LEFT_MARGIN,
                screenHeight - DisplayConfig.SECTION_TITLE_Y);

        font.setColor(isLeftSide ? DisplayConfig.DEFAULT_COLOR : DisplayConfig.SELECTED_COLOR);
        font.draw(batch, MenuConfig.ITEMS_TITLE,
                DisplayConfig.RIGHT_MARGIN,
                screenHeight - DisplayConfig.SECTION_TITLE_Y);

        // Draw equipped items (left side)
        drawEquippedItems(screenHeight);

        // Draw inventory items (right side)
        drawInventoryItems(screenHeight);

        // Draw navigation help near bottom but not too close
        font.getData().setScale(DisplayConfig.DESC_FONT_SCALE);
        font.setColor(DisplayConfig.NAVIGATION_COLOR);
        layout.setText(font, MenuConfig.NAVIGATION_HELP);
        font.draw(batch, MenuConfig.NAVIGATION_HELP,
                screenWidth / 2 - layout.width / 2,
                DisplayConfig.NAVIGATION_HELP_Y);

        // Draw item description if examining - now in a box in the middle of the screen
        if (examiningItem && !itemDescription.isEmpty()) {
            drawItemDescriptionBox(screenWidth, screenHeight);
        }

        // Reset font scale
        font.getData().setScale(originalScale);

        batch.end();
    }

    /**
     * Draw player stats with base values and bonuses in parentheses
     */
    private void drawPlayerStats(float screenHeight) {
        float originalScale = font.getData().scaleX;
        font.getData().setScale(DisplayConfig.STATS_FONT_SCALE);

        // Get player stats
        int baseHP = player.getBaseMaxHP();
        int baseMP = player.getBaseMaxMP();
        int baseAtk = player.getBaseAttack();
        int baseDef = player.getBaseDefense();
        float baseCrit = player.getBaseCritRate();

        // Get current player stats (base + bonus)
        int curMP = player.getMP();
        int curAtk = player.getAttack();
        int curDef = player.getDefense();
        float curCrit = player.getCritRate();

        // Get bonuses
        int hpBonus = player.getMaxHP() - baseHP;
        int mpBonus = player.getMaxMP() - baseMP;
        int atkBonus = player.getAttack() - baseAtk;
        int defBonus = player.getDefense() - baseDef;
        float critBonus = player.getCritRate() - baseCrit;

        // Format and draw stats - both HP and MP show current/max format
        float y = screenHeight - DisplayConfig.PLAYER_STATS_START_Y;

        // Draw HP label and current/max HP
        font.setColor(DisplayConfig.STATS_LABEL_COLOR);
        font.draw(batch, "HP:", DisplayConfig.LEFT_MARGIN, y);

        // Draw current/max HP
        font.setColor(DisplayConfig.DEFAULT_COLOR);
        String hpText = player.getCurrentHP() + "/" + player.getMaxHP();
        font.draw(batch, hpText, DisplayConfig.LEFT_MARGIN + 100, y);

        // Calculate width of HP text for proper spacing
        com.badlogic.gdx.graphics.g2d.GlyphLayout hpLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, hpText);
        float hpWidth = hpLayout.width;

        // Draw HP bonus in parentheses with color
        if (hpBonus != 0) {
            font.setColor(hpBonus > 0 ?
                DisplayConfig.POSITIVE_BONUS_COLOR :
                DisplayConfig.NEGATIVE_BONUS_COLOR);

            String bonusText = (hpBonus > 0 ? "+" : "") + hpBonus;
            font.draw(batch, "(" + bonusText + ")", DisplayConfig.LEFT_MARGIN + 100 + hpWidth + 30, y);
        }

        // Draw MP label and current/max MP (in the same format as HP)
        y = screenHeight - DisplayConfig.PLAYER_STATS_START_Y - DisplayConfig.PLAYER_STATS_SPACING;

        font.setColor(DisplayConfig.STATS_LABEL_COLOR);
        font.draw(batch, "MP:", DisplayConfig.LEFT_MARGIN, y);

        // Draw current/max MP
        font.setColor(DisplayConfig.DEFAULT_COLOR);
        String mpText = player.getMP() + "/" + player.getMaxMP();
        font.draw(batch, mpText, DisplayConfig.LEFT_MARGIN + 100, y);

        // Calculate width of MP text for proper spacing
        com.badlogic.gdx.graphics.g2d.GlyphLayout mpLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, mpText);
        float mpWidth = mpLayout.width;

        // Draw MP bonus in parentheses with color
        if (mpBonus != 0) {
            font.setColor(mpBonus > 0 ?
                DisplayConfig.POSITIVE_BONUS_COLOR :
                DisplayConfig.NEGATIVE_BONUS_COLOR);

            String bonusText = (mpBonus > 0 ? "+" : "") + mpBonus;
            font.draw(batch, "(" + bonusText + ")", DisplayConfig.LEFT_MARGIN + 100 + mpWidth + 30, y);
        }

        // Now draw the remaining stats (ATK, DEF, CRIT)
        String[] labels = {"ATK:", "DEF:", "CRIT:"};
        int[] curValues = {curAtk, curDef, (int)(curCrit * 100)};
        float[] bonuses = {atkBonus, defBonus, critBonus * 100};
        String[] suffixes = {"", "", "%"};

        for (int i = 0; i < labels.length; i++) {
            y = screenHeight - DisplayConfig.PLAYER_STATS_START_Y - (i + 2) * DisplayConfig.PLAYER_STATS_SPACING;

            // Draw label
            font.setColor(DisplayConfig.STATS_LABEL_COLOR);
            font.draw(batch, labels[i], DisplayConfig.LEFT_MARGIN, y);

            // Draw value
            font.setColor(DisplayConfig.DEFAULT_COLOR);
            String baseText = String.valueOf(curValues[i]) + suffixes[i];
            font.draw(batch, baseText, DisplayConfig.LEFT_MARGIN + 100, y);

            // Calculate width of stat text for proper spacing
            com.badlogic.gdx.graphics.g2d.GlyphLayout statLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, baseText);
            float statWidth = statLayout.width;

            // Draw bonus in parentheses with color
            if (bonuses[i] != 0) {
                String bonusText = (bonuses[i] > 0 ? "+" : "") + (int)bonuses[i] + suffixes[i];

                // Choose color based on positive/negative
                font.setColor(bonuses[i] > 0 ?
                    DisplayConfig.POSITIVE_BONUS_COLOR :
                    DisplayConfig.NEGATIVE_BONUS_COLOR);

                font.draw(batch, "(" + bonusText + ")", DisplayConfig.LEFT_MARGIN + 100 + statWidth + 30, y);
            }
        }

        font.getData().setScale(originalScale);
    }

    private void drawEquippedItems(float screenHeight) {
        float originalScale = font.getData().scaleX;
        font.getData().setScale(DisplayConfig.ITEM_FONT_SCALE);

        // Get equipped items
        Array<Equipment> equipped = inventory.getEquippedItems();
        String[] slotNames = {"Weapon", "Armor", "Accessory"};

        for (int i = 0; i < slotNames.length; i++) {
            boolean isSelected = isLeftSide && i == selectedIndexLeft;
            Equipment item = equipped.get(i);

            // Slot name
            font.setColor(isSelected ? DisplayConfig.SELECTED_COLOR : DisplayConfig.DEFAULT_COLOR);
            String prefix = isSelected ? "> " : "  ";
            float y = screenHeight - DisplayConfig.ITEMS_START_Y - i * DisplayConfig.ITEMS_SPACING_Y;

            font.draw(batch, prefix + slotNames[i] + ":", DisplayConfig.LEFT_MARGIN, y);

            // Item name or "Empty" with increased spacing
            String itemText = item != null ? item.getName() : MenuConfig.EMPTY_SLOT;

            if (item != null) {
                // Set color based on item tier
                setColorByTier(item.getTier(), isSelected);
            } else {
                font.setColor(DisplayConfig.DEFAULT_COLOR);
            }

            font.draw(batch, itemText, DisplayConfig.LEFT_MARGIN + DisplayConfig.EQUIPPED_ITEM_NAME_OFFSET, y);
        }

        font.getData().setScale(originalScale);
    }

    private void drawInventoryItems(float screenHeight) {
        float originalScale = font.getData().scaleX;
        font.getData().setScale(DisplayConfig.ITEM_FONT_SCALE);

        // Get inventory items and consumables
        Array<Equipment> items = inventory.getInventoryItems();
        Array<ConsumableItem> consumables = inventory.getConsumableItems();

        if (items.size == 0 && consumables.size == 0) {
            // Nothing to draw
            font.getData().setScale(originalScale);
            return;
        }

        // Create itemRows (actual items without headers) for selection
        Array<Object> itemRows = new Array<>();
        Array<Object> allRows = new Array<>(); // Both items and headers
        Array<Boolean> isHeader = new Array<>(); // Whether each row is a header

        // Set up groupings and headings
        Equipment.EquipmentType lastType = null;
        boolean addedConsumableHeader = false;

        // Process equipment items first
        for (int i = 0; i < items.size; i++) {
            Equipment item = items.get(i);

            // Add header if this is a new type
            if (lastType != item.getType()) {
                lastType = item.getType();
                allRows.add(lastType.getDisplayName());
                isHeader.add(true);
            }

            // Add item
            allRows.add(item);
            isHeader.add(false);
            itemRows.add(item);
        }

        // Then process consumable items
        if (consumables.size > 0) {
            allRows.add("CONSUMABLE");
            isHeader.add(true);

            for (int i = 0; i < consumables.size; i++) {
                allRows.add(consumables.get(i));
                isHeader.add(false);
                itemRows.add(consumables.get(i));
            }
        }

        // Sanity checks for selection indices
        if (topItemIndex >= allRows.size) {
            topItemIndex = Math.max(0, allRows.size - 1);
        }

        // Make sure selectedIndexRight isn't beyond the number of items
        if (selectedIndexRight >= itemRows.size) {
            selectedIndexRight = Math.max(0, itemRows.size - 1);
        }

        // Find the absolute index (in allRows) of the selected item
        int selectedAbsoluteIndex = -1;
        int itemCount = 0;

        for (int i = 0; i < allRows.size; i++) {
            if (!isHeader.get(i)) { // If it's an item, not a header
                if (itemCount == selectedIndexRight) {
                    selectedAbsoluteIndex = i;
                    break;
                }
                itemCount++;
            }
        }

        // If we couldn't find the selected item, adjust
        if (selectedAbsoluteIndex == -1 && itemRows.size > 0) {
            selectedIndexRight = 0;
            // Find the first non-header
            for (int i = 0; i < allRows.size; i++) {
                if (!isHeader.get(i)) {
                    selectedAbsoluteIndex = i;
                    break;
                }
            }
        }

        // Adjust topItemIndex to ensure selected item is visible
        if (selectedAbsoluteIndex != -1) {
            // Find header index for the selected item
            int headerIndex = selectedAbsoluteIndex;
            while (headerIndex > 0 && !isHeader.get(headerIndex)) {
                headerIndex--;
            }
            
            // Only show header if it would take up at most one line of the visible area
            // This prevents the scrolling from always locking to headers
            if (headerIndex >= 0 && isHeader.get(headerIndex)) {
                // If this is the first item in a category and we're near the top of the list,
                // we can show the header, but don't force it otherwise
                if (selectedAbsoluteIndex == headerIndex + 1 && 
                    // Only do this if the header is just one position above current view
                    // or we're near the beginning of the list
                    (headerIndex == topItemIndex - 1 || headerIndex < 3)) {
                    topItemIndex = Math.max(0, headerIndex);
                }
            }
            
            // Standard visibility checks always take priority to keep selected item visible
            if (selectedAbsoluteIndex < topItemIndex) {
                topItemIndex = selectedAbsoluteIndex;
            }
            else if (selectedAbsoluteIndex >= topItemIndex + DisplayConfig.MAX_VISIBLE_ITEMS) {
                topItemIndex = selectedAbsoluteIndex - DisplayConfig.MAX_VISIBLE_ITEMS + 1;
            }
        }

        // Determine visible range
        int endIndex = Math.min(topItemIndex + DisplayConfig.MAX_VISIBLE_ITEMS, allRows.size);

        // Draw visible items
        float y = screenHeight - DisplayConfig.ITEMS_START_Y;

        for (int i = topItemIndex; i < endIndex; i++) {
            boolean isCurrentItemHeader = isHeader.get(i);
            boolean isSelected = !isLeftSide && !isCurrentItemHeader && i == selectedAbsoluteIndex;

            String prefix = isSelected ? "> " : "  ";
            Object rowItem = allRows.get(i);

            if (isCurrentItemHeader) {
                // Draw section header
                font.setColor(DisplayConfig.TITLE_COLOR);
                font.draw(batch, "--- " + rowItem.toString() + " ---",
                        DisplayConfig.RIGHT_MARGIN + 120, y);
            } else if (rowItem instanceof Equipment) {
                // Draw equipment item
                Equipment equipment = (Equipment) rowItem;
                setColorByTier(equipment.getTier(), isSelected);
                font.draw(batch, prefix + equipment.getName(), DisplayConfig.RIGHT_MARGIN, y);
            } else if (rowItem instanceof ConsumableItem) {
                // Draw consumable item
                ConsumableItem consumable = (ConsumableItem) rowItem;

                // Create the full display text (name + quantity)
                String nameWithQuantity = consumable.getName() + " x" + consumable.getQuantity();
                String displayText = prefix + nameWithQuantity;

                if (isSelected) {
                    font.setColor(DisplayConfig.SELECTED_COLOR);
                } else {
                    setColorByTier(consumable.getTier(), false);
                }

                // Draw the full text (name with quantity)
                font.draw(batch, displayText, DisplayConfig.RIGHT_MARGIN, y);

                // If this is a combat item, draw the star icon after quantity
                if (inventory.isSelectedForCombat(consumable)) {
                    drawStarIcon(DisplayConfig.RIGHT_MARGIN, y, displayText, isSelected);
                }
            }

            // Move to next row
            y -= DisplayConfig.ITEMS_SPACING_Y;
        }

        // Draw scroll indicators if needed
        if (topItemIndex > 0) {
            font.setColor(DisplayConfig.SCROLL_INDICATOR_COLOR);
            font.draw(batch, "^ MORE ITEMS ^",
                    DisplayConfig.RIGHT_MARGIN + 150,
                    screenHeight - DisplayConfig.ITEMS_START_Y + DisplayConfig.ITEMS_SPACING_Y);
        }

        if (endIndex < allRows.size) {
            font.setColor(DisplayConfig.SCROLL_INDICATOR_COLOR);
            font.draw(batch, "v MORE ITEMS v",
                    DisplayConfig.RIGHT_MARGIN + 150,
                    screenHeight - DisplayConfig.ITEMS_START_Y - DisplayConfig.MAX_VISIBLE_ITEMS * DisplayConfig.ITEMS_SPACING_Y - 20);
        }

        font.getData().setScale(originalScale);
    }

    /**
     * Draw the item description in a centered box with a semi-transparent background
     */
    private void drawItemDescriptionBox(float screenWidth, float screenHeight) {
        // End batch before using shapeRenderer
        batch.end();

        // Determine the dynamic size based on text content
        font.getData().setScale(DisplayConfig.DESC_FONT_SCALE);
        String title = "Item Details";
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, title);

        // Measure description text width
        com.badlogic.gdx.graphics.g2d.GlyphLayout descLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, itemDescription);

        // Close instruction text
        String closeText = "Press any key to close";
        com.badlogic.gdx.graphics.g2d.GlyphLayout closeLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, closeText);

        // Calculate box dimensions based on text size
        float contentWidth = Math.max(Math.max(titleLayout.width, descLayout.width), closeLayout.width) + 60; // Add padding
        float minWidth = screenWidth * 0.3f; // Minimum width
        float boxWidth = Math.max(contentWidth, minWidth);
        boxWidth = Math.min(boxWidth, screenWidth * 0.7f); // Cap maximum width to 70% (reduced from 80%)

        // Count number of lines in description (roughly estimate based on width)
        int numLines = (int)Math.ceil(descLayout.width / boxWidth) + itemDescription.split("\n").length;
        float lineHeight = descLayout.height * 1.4f; // Slightly reduced spacing (was 1.5f)
        float boxHeight = lineHeight * (numLines + 4); // Add space for title and close text
        boxHeight = Math.min(boxHeight, screenHeight * 0.65f); // Cap maximum height (reduced from 70%)

        // Center the box on screen
        float boxX = (screenWidth - boxWidth) / 2;
        float boxY = (screenHeight - boxHeight) / 2;

        // Draw semi-transparent background
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glLineWidth(5);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw black box fill
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.95f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();

        // Draw outline with tier color instead of white
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        
        if (currentExaminedItemTier != null) {
            // Use tier color for the outline
            if (currentExaminedItemTier.isAnimated()) {
                // For Genesis tier, create rainbow effect
                float r = (float) Math.sin(genesisSineTime) * 0.5f + 0.5f;
                float g = (float) Math.sin(genesisSineTime + 2.0f) * 0.5f + 0.5f;
                float b = (float) Math.sin(genesisSineTime + 4.0f) * 0.5f + 0.5f;
                shapeRenderer.setColor(r, g, b, 1f);
            } else {
                // For all other tiers, use the pre-defined color
                Color tierColor = currentExaminedItemTier.getColor();
                shapeRenderer.setColor(tierColor);
            }
        } else {
            // Fallback to white if tier is somehow null
            shapeRenderer.setColor(1, 1, 1, 1f);
        }
        
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        Gdx.gl.glLineWidth(1);
        // Restart the batch for drawing text
        batch.begin();

        // Draw title in yellow centered at top of box
        font.setColor(DisplayConfig.SELECTED_COLOR);
        font.draw(batch, title,
                screenWidth / 2 - titleLayout.width / 2,
                boxY + boxHeight - 30);

        // Draw actual description
        font.setColor(DisplayConfig.DESCRIPTION_COLOR);
        font.draw(batch, itemDescription,
                boxX + 20,
                boxY + boxHeight - 30 - titleLayout.height - 20, // Position below title with padding
                boxWidth - 40, // Padding on both sides
                1, // Align left
                true); // Wrap

        // Draw close instruction at bottom of box
        font.setColor(DisplayConfig.NAVIGATION_COLOR);
        font.draw(batch, closeText,
                screenWidth / 2 - closeLayout.width / 2,
                boxY + 50);
    }

    /**
     * Input handling methods
     */
    private void handleInput() {
        if (examiningItem) {
            // When examining an item, pressing any key returns to normal view
            if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                examiningItem = false;
                currentExaminedItemTier = null; // Reset the item tier
                selectSound.play();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            // Switch between equipped and inventory
            isLeftSide = !isLeftSide;
            selectSound.play();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (isLeftSide) {
                moveLeftSelection(-1);
            } else {
                moveRightSelection(-1);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (isLeftSide) {
                moveLeftSelection(1);
            } else {
                moveRightSelection(1);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (isLeftSide) {
                // Unequip item
                unequipSelected();
            } else {
                // Equip or use item
                equipOrUseSelected();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            // Examine item
            examineSelected();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            // Toggle combat selection for consumable items
            toggleCombatSelection();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goBack();
        }
    }

    private void moveLeftSelection(int direction) {
        int totalOptions = inventory.getEquippedItems().size;
        selectedIndexLeft = (selectedIndexLeft + direction + totalOptions) % totalOptions;
        selectSound.play();
    }

    private void moveRightSelection(int direction) {
        // Get inventory items and consumables
        Array<Equipment> items = inventory.getInventoryItems();
        Array<ConsumableItem> consumables = inventory.getConsumableItems();
        int totalItemCount = items.size + consumables.size;

        if (totalItemCount == 0) return;

        // Store original selection and scroll position
        int originalSelectedIndex = selectedIndexRight;
        int originalTopIndex = topItemIndex;
        
        // Adjust selection
        selectedIndexRight += direction;

        // Handle wrapping
        if (selectedIndexRight < 0) {
            selectedIndexRight = totalItemCount - 1;
        }
        if (selectedIndexRight >= totalItemCount) {
            selectedIndexRight = 0;
        }
        
        // Always play sound before any early returns
        selectSound.play();
        
        // Build the allRows data structure for accurate navigation
        Array<Object> allRows = new Array<>();
        Array<Boolean> isHeader = new Array<>();
        buildInventoryRows(allRows, isHeader);
        
        // Find the absolute indices for both the previous and current selection
        int selectedAbsoluteIndex = -1;
        int previousAbsoluteIndex = -1;
        findIndicesInAllRows(allRows, isHeader, items, consumables, originalSelectedIndex, selectedIndexRight, 
                            previousAbsoluteIndex, selectedAbsoluteIndex);
        
        // Special case: if we're scrolling up in the same section and there's minimal items,
        // don't change the scroll position to avoid jumpiness
        if (direction < 0 && selectedAbsoluteIndex >= 0 && previousAbsoluteIndex >= 0) {
            // Check if we're moving within the same section by seeing if there's no header between the items
            boolean sameSection = true;
            for (int i = Math.min(selectedAbsoluteIndex, previousAbsoluteIndex); 
                 i <= Math.max(selectedAbsoluteIndex, previousAbsoluteIndex); i++) {
                if (i >= 0 && i < isHeader.size && isHeader.get(i)) {
                    sameSection = false;
                    break;
                }
            }
            
            // If we're in the same section and the currently selected item is still visible
            // with the current scroll position, don't change the scroll position
            if (sameSection && selectedAbsoluteIndex >= originalTopIndex && 
                selectedAbsoluteIndex < originalTopIndex + DisplayConfig.MAX_VISIBLE_ITEMS) {
                // Keep the original scroll position
                topItemIndex = originalTopIndex;
                return;
            }
        }
        
        // If selectedAbsoluteIndex is valid, adjust scrolling with minimal movement
        if (selectedAbsoluteIndex != -1) {
            // When scrolling down, don't scroll until it's necessary
            if (direction > 0) {
                if (selectedAbsoluteIndex >= originalTopIndex + DisplayConfig.MAX_VISIBLE_ITEMS) {
                    // Just scroll enough to make the item visible
                    topItemIndex = selectedAbsoluteIndex - DisplayConfig.MAX_VISIBLE_ITEMS + 1;
                } else {
                    // Item is already visible, don't change scroll position
                    topItemIndex = originalTopIndex;
                }
            }
            // When scrolling up, ensure selected item is visible at top
            else if (direction < 0) {
                if (selectedAbsoluteIndex < originalTopIndex) {
                    // Only scroll exactly as much as needed
                    topItemIndex = selectedAbsoluteIndex;
                } else {
                    // Item is already visible, don't change scroll position
                    topItemIndex = originalTopIndex;
                }
            }
            
            // If we wrapped around (from bottom to top)
            if (originalSelectedIndex > selectedIndexRight && direction < 0) {
                // Jump to the end of the list for better user experience when wrapping
                topItemIndex = Math.max(0, allRows.size - DisplayConfig.MAX_VISIBLE_ITEMS);
            }
            // If we wrapped around (from top to bottom)
            else if (originalSelectedIndex < selectedIndexRight && direction > 0 && selectedIndexRight == 0) {
                // Jump to the beginning of the list
                topItemIndex = 0;
            }
        }
    }
    
    /**
     * Helper method to build the complete inventory rows data structure
     */
    private void buildInventoryRows(Array<Object> allRows, Array<Boolean> isHeader) {
        Array<Equipment> items = inventory.getInventoryItems();
        Array<ConsumableItem> consumables = inventory.getConsumableItems();
        
        // Build allRows structure
        Equipment.EquipmentType lastType = null;
        
        // Process equipment items first
        for (int i = 0; i < items.size; i++) {
            Equipment item = items.get(i);

            // Add header if this is a new type
            if (lastType != item.getType()) {
                lastType = item.getType();
                allRows.add(lastType.getDisplayName());
                isHeader.add(true);
            }

            // Add item
            allRows.add(item);
            isHeader.add(false);
        }

        // Then process consumable items
        if (consumables.size > 0) {
            allRows.add("CONSUMABLE");
            isHeader.add(true);

            for (int i = 0; i < consumables.size; i++) {
                allRows.add(consumables.get(i));
                isHeader.add(false);
            }
        }
    }
    
    /**
     * Helper method to find the absolute indices of selections in allRows
     */
    private void findIndicesInAllRows(Array<Object> allRows, Array<Boolean> isHeader,
                                    Array<Equipment> items, Array<ConsumableItem> consumables,
                                    int originalIndex, int currentIndex,
                                    int previousAbsoluteIndex, int selectedAbsoluteIndex) {
        // Find indices in allRows
        int originalItemCount = 0;
        int currentItemCount = 0;
        
        for (int i = 0; i < allRows.size; i++) {
            if (!isHeader.get(i)) {
                // This is an actual item, not a header
                Object item = allRows.get(i);
                
                boolean isEquipment = item instanceof Equipment;
                int itemIndex = -1;
                
                if (isEquipment) {
                    itemIndex = items.indexOf((Equipment)item, true);
                    if (itemIndex == originalIndex) previousAbsoluteIndex = i;
                    if (itemIndex == currentIndex) selectedAbsoluteIndex = i;
                } else if (item instanceof ConsumableItem) {
                    itemIndex = items.size + consumables.indexOf((ConsumableItem)item, true);
                    if (itemIndex == originalIndex) previousAbsoluteIndex = i;
                    if (itemIndex == currentIndex) selectedAbsoluteIndex = i;
                }
            }
        }
    }

    private Object getSelectedInventoryItem() {
        Array<Equipment> items = inventory.getInventoryItems();
        Array<ConsumableItem> consumables = inventory.getConsumableItems();

        if (selectedIndexRight < items.size) {
            return items.get(selectedIndexRight);
        } else if (selectedIndexRight - items.size < consumables.size) {
            return consumables.get(selectedIndexRight - items.size);
        }

        return null;
    }

    private void equipOrUseSelected() {
        Object selectedItem = getSelectedInventoryItem();

        if (selectedItem == null) return;

        if (selectedItem instanceof Equipment) {
            // Save current scroll position and selected item
            int currentTopIndex = topItemIndex;
            String itemName = ((Equipment)selectedItem).getName();
            
            // Equip the item
            Equipment item = (Equipment) selectedItem;
            Equipment previousItem = inventory.equipItem(item);
            equipSound.play();

            // If there was a previous item, add it back to inventory
            if (previousItem != null) {
                inventory.addToInventory(previousItem);
            }

            player.saveToFile();

            // Re-sort inventory after changes but try to maintain similar visual position
            updateAfterInventoryChangeWithPosition(currentTopIndex, itemName);
        } else if (selectedItem instanceof ConsumableItem) {
            // Use consumable
            ConsumableItem item = (ConsumableItem) selectedItem;

            // Remember the item name, index and scroll position before using it
            String itemName = item.getName();
            int itemQuantity = item.getQuantity();
            int currentIndex = selectedIndexRight;
            int currentTopIndex = topItemIndex;
            boolean isLastOne = (itemQuantity == 1);
            
            // Store nearby items to help maintain position after change
            String nextItemName = null;
            String prevItemName = null;
            
            // Try to find adjacent items to use as reference points
            Array<Equipment> items = inventory.getInventoryItems();
            Array<ConsumableItem> consumables = inventory.getConsumableItems();
            
            // Build item representation to find next/prev items
            Array<Object> allItems = new Array<>();
            for (Equipment equip : items) allItems.add(equip);
            for (ConsumableItem cons : consumables) allItems.add(cons);
            
            if (allItems.size > 0) {
                // Find the selected item's position
                int posInAllItems = -1;
                for (int i = 0; i < allItems.size; i++) {
                    Object obj = allItems.get(i);
                    if ((obj instanceof Equipment && ((Equipment)obj).getName().equals(itemName)) ||
                        (obj instanceof ConsumableItem && ((ConsumableItem)obj).getName().equals(itemName))) {
                        posInAllItems = i;
                        break;
                    }
                }
                
                // If found, get adjacent items
                if (posInAllItems >= 0) {
                    if (posInAllItems > 0) {
                        Object prevObj = allItems.get(posInAllItems - 1);
                        if (prevObj instanceof Equipment) {
                            prevItemName = ((Equipment)prevObj).getName();
                        } else if (prevObj instanceof ConsumableItem) {
                            prevItemName = ((ConsumableItem)prevObj).getName();
                        }
                    }
                    
                    if (posInAllItems < allItems.size - 1) {
                        Object nextObj = allItems.get(posInAllItems + 1);
                        if (nextObj instanceof Equipment) {
                            nextItemName = ((Equipment)nextObj).getName();
                        } else if (nextObj instanceof ConsumableItem) {
                            nextItemName = ((ConsumableItem)nextObj).getName();
                        }
                    }
                }
            }

            // Apply item effect
            switch (item.getEffect()) {
                case HEAL_HP:
                    player.heal(item.getEffectAmount());
                    useSound.setVolume(useSound.play(), 0.15f);
                    break;
                case RESTORE_MP:
                    player.increaseMP(item.getEffectAmount());
                    useSound.setVolume(useSound.play(), 0.15f);
                    break;
                case BUFF_ATK:
                case BUFF_DEF:
                    // Buffs not implemented yet
                    selectSound.setVolume(selectSound.play(), 0.15f);
                    break;
            }

            // Decrease quantity
            inventory.useConsumableItem(item);
            player.saveToFile();

            // Re-sort inventory after changes
            sortInventoryItems();
            
            // Identify what will help us maintain position
            String targetItem = null;
            if (!isLastOne) {
                // If not the last one, prefer to stay on the same item
                targetItem = itemName;
            } else if (nextItemName != null) {
                // If it's the last one, prefer to move to the next item
                targetItem = nextItemName;
            } else if (prevItemName != null) {
                // If no next item, try previous item
                targetItem = prevItemName;
            }
            
            // First try to select the appropriate item
            if (targetItem != null) {
                findAndSelectItemByName(targetItem);
            } else {
                // Fall back to index-based selection
                // Keep the selection index within bounds
                Array<Equipment> itemsAfter = inventory.getInventoryItems();
                Array<ConsumableItem> consumablesAfter = inventory.getConsumableItems();
                int totalItems = itemsAfter.size + consumablesAfter.size;
                selectedIndexRight = Math.min(currentIndex, Math.max(0, totalItems - 1));
            }
            
            // Now find the actual position of the selected item in the complete list
            Array<Object> allRows = new Array<>();
            Array<Boolean> isHeader = new Array<>();
            buildInventoryRows(allRows, isHeader);
            
            int selectedAbsoluteIndex = -1;
            for (int i = 0; i < allRows.size; i++) {
                if (!isHeader.get(i)) {
                    Object obj = allRows.get(i);
                    if (targetItem != null) {
                        // Find by name
                        if ((obj instanceof Equipment && ((Equipment)obj).getName().equals(targetItem)) ||
                            (obj instanceof ConsumableItem && ((ConsumableItem)obj).getName().equals(targetItem))) {
                            selectedAbsoluteIndex = i;
                            break;
                        }
                    } else {
                        // Count non-headers to match selectedIndexRight
                        int countedItems = 0;
                        for (int j = 0; j < allRows.size; j++) {
                            if (!isHeader.get(j)) {
                                if (countedItems == selectedIndexRight) {
                                    selectedAbsoluteIndex = j;
                                    break;
                                }
                                countedItems++;
                            }
                        }
                        if (selectedAbsoluteIndex >= 0) break;
                    }
                }
            }
            
            // Now determine the best topItemIndex to maintain visual position
            // Try to keep the same items visible if possible
            if (selectedAbsoluteIndex >= 0) {
                // Ideal case - try to keep the current scroll position
                if (selectedAbsoluteIndex >= currentTopIndex && 
                    selectedAbsoluteIndex < currentTopIndex + DisplayConfig.MAX_VISIBLE_ITEMS) {
                    // We can keep the same scroll position
                    topItemIndex = currentTopIndex;
                }
                // If that's not possible, center the selected item
                else if (selectedAbsoluteIndex >= DisplayConfig.MAX_VISIBLE_ITEMS / 2) {
                    topItemIndex = Math.max(0, Math.min(
                        allRows.size - DisplayConfig.MAX_VISIBLE_ITEMS,
                        selectedAbsoluteIndex - (DisplayConfig.MAX_VISIBLE_ITEMS / 2)
                    ));
                }
                // Otherwise, start from the top
                else {
                    topItemIndex = 0;
                }
            } else {
                // Fallback - use original position if possible
                if (currentTopIndex < allRows.size) {
                    topItemIndex = Math.min(currentTopIndex, allRows.size - DisplayConfig.MAX_VISIBLE_ITEMS);
                } else {
                    topItemIndex = Math.max(0, allRows.size - DisplayConfig.MAX_VISIBLE_ITEMS);
                }
            }
        }
    }

    /**
     * Helper method to find and select an item by name
     */
    private void findAndSelectItemByName(String itemName) {
        Array<Equipment> items = inventory.getInventoryItems();
        Array<ConsumableItem> consumables = inventory.getConsumableItems();

        // First try to find among consumables (more likely to find it here)
        for (int i = 0; i < consumables.size; i++) {
            if (consumables.get(i).getName().equals(itemName)) {
                selectedIndexRight = items.size + i;
                return;
            }
        }

        // Then try among equipment
        for (int i = 0; i < items.size; i++) {
            if (items.get(i).getName().equals(itemName)) {
                selectedIndexRight = i;
                return;
            }
        }

        // If not found, keep current selection
    }

    private void examineSelected() {
        if (isLeftSide) {
            // Examine equipped item
            Equipment item = inventory.getEquippedItems().get(selectedIndexLeft);
            if (item != null) {
                examiningItem = true;
                buildItemDescription(item);
                selectSound.play();
            }
        } else {
            // Examine inventory item
            Object selectedItem = getSelectedInventoryItem();

            if (selectedItem != null) {
                examiningItem = true;

                if (selectedItem instanceof Equipment) {
                    buildItemDescription((Equipment) selectedItem);
                } else if (selectedItem instanceof ConsumableItem) {
                    buildItemDescription((ConsumableItem) selectedItem);
                }

                selectSound.play();
            }
        }
    }

    private void toggleCombatSelection() {
        if (isLeftSide) return; // Only applicable to right side (inventory items)

        Object selectedItem = getSelectedInventoryItem();

        // Only toggle combat selection for consumable items
        if (selectedItem instanceof ConsumableItem) {
            ConsumableItem item = (ConsumableItem) selectedItem;
            if (inventory.toggleCombatSelection(item)) {
                selectSound.play();
                player.saveToFile(); // Save changes
            }
        }
    }

    private void unequipSelected() {
        // Save the current scroll position of inventory
        int currentTopIndex = topItemIndex;
        
        Equipment.EquipmentType type = Equipment.EquipmentType.values()[selectedIndexLeft];
        
        // Get the equipped item before unequipping to track its name
        Equipment equippedItem = inventory.getEquippedItems().get(type.ordinal());
        String itemName = equippedItem != null ? equippedItem.getName() : null;
        
        if (inventory.unequipItem(type)) {
            equipSound.play();
            player.saveToFile();

            // Re-sort inventory after changes, and try to select the just-unequipped item
            updateAfterInventoryChangeWithPosition(currentTopIndex, itemName);
            
            // Find and select the newly unequipped item if possible
            if (itemName != null) {
                findAndSelectItemByName(itemName);
            }
        } else {
            // Failed to unequip (probably inventory full)
            selectSound.play();
        }
    }

    private void buildItemDescription(Equipment item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getName()).append(" [").append(item.getTier().getDisplayName()).append("]").append("\n\n");
        sb.append(item.getDescription()).append("\n\n");

        sb.append("Type: ").append(item.getType().getDisplayName()).append("\n");

        if (item.getAttackBonus() != 0) {
            sb.append("Attack: ").append(formatBonus(item.getAttackBonus())).append("\n");
        }
        if (item.getDefenseBonus() != 0) {
            sb.append("Defense: ").append(formatBonus(item.getDefenseBonus())).append("\n");
        }
        if (item.getMaxHPBonus() != 0) {
            sb.append("Max HP: ").append(formatBonus(item.getMaxHPBonus())).append("\n");
        }
        if (item.getMaxMPBonus() != 0) {
            sb.append("Max MP: ").append(formatBonus(item.getMaxMPBonus())).append("\n");
        }
        if (item.getCritRateBonus() != 0) {
            sb.append("Crit Rate: ").append(formatPercentBonus(item.getCritRateBonus())).append("\n");
        }

        itemDescription = sb.toString();
        currentExaminedItemTier = item.getTier();
    }

    private void buildItemDescription(ConsumableItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getName()).append(" [").append(item.getTier().getDisplayName()).append("]").append(" (x").append(item.getQuantity()).append(")\n\n");
        sb.append(item.getDescription()).append("\n\n");

        sb.append("Effect: ").append(item.getEffect().getDescription()).append("\n");
        sb.append("Value: ").append(item.getEffectAmount()).append("\n");

        if (inventory.isSelectedForCombat(item)) {
            sb.append("\nSelected for use in combat");
        }

        itemDescription = sb.toString();
        currentExaminedItemTier = item.getTier();
    }

    private String formatBonus(int value) {
        return (value > 0 ? "+" : "") + value;
    }

    private String formatPercentBonus(float value) {
        return (value > 0 ? "+" : "") + (int)(value * 100) + "%";
    }

    private void goBack() {
        game.setScreen(new ScreenTransition(
            game,
            this,
            new MainNavigationScreen(game),
            ScreenTransition.TransitionType.CROSS_FADE
        ));
    }

    /**
     * Screen lifecycle methods
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        // Keep for future reference but comment out
        // music.dispose();
        selectSound.dispose();
        equipSound.dispose();
        useSound.dispose();
        shapeRenderer.dispose();
        starTexture.dispose(); // Also dispose the star texture
    }

    @Override
    public void hide() {
        // Keep for future reference but comment out
        // if (music != null) {
        //     music.stop();
        // }

        // No need to stop the shared music
    }

    @Override
    public void show() {
        // Keep for future reference but comment out
        // if (music != null && !music.isPlaying()) {
        //     music.play();
        // }

        // Make sure the shared music is playing
        swu.cp112.silkblade.core.Main.resumeBackgroundMusic();

        // Re-sort inventory when showing screen (in case it changed)
        sortInventoryItems();
    }

    // Unused Screen interface methods
    @Override public void pause() {}
    @Override public void resume() {}

    /**
     * Updated version of updateAfterInventoryChange that tries to maintain the visual position
     * @param previousTopIndex The previous topItemIndex to try to maintain
     * @param targetItemName The name of an item to try to keep visible (can be null)
     */
    private void updateAfterInventoryChangeWithPosition(int previousTopIndex, String targetItemName) {
        // First do the standard sorting and validation
        sortInventoryItems();

        // Ensure selected index is still valid
        Array<Equipment> items = inventory.getInventoryItems();
        Array<ConsumableItem> consumables = inventory.getConsumableItems();
        int totalItems = items.size + consumables.size;

        if (totalItems == 0) {
            selectedIndexRight = 0;
            topItemIndex = 0;
            return;
        }
        
        // Try to keep topItemIndex close to what it was before if possible
        topItemIndex = Math.min(previousTopIndex, Math.max(0, totalItems - DisplayConfig.MAX_VISIBLE_ITEMS));
        
        // Ensure selectedIndexRight is valid
        selectedIndexRight = Math.min(selectedIndexRight, totalItems - 1);
        
        // If we specified a target item to keep visible, find it
        if (targetItemName != null) {
            // Look for the equipment or consumable with the target name
            int targetIndex = -1;
            
            // Check equipment items
            for (int i = 0; i < items.size; i++) {
                if (items.get(i).getName().equals(targetItemName)) {
                    targetIndex = i;
                    break;
                }
            }
            
            // If not found in equipment, check consumables
            if (targetIndex == -1) {
                for (int i = 0; i < consumables.size; i++) {
                    if (consumables.get(i).getName().equals(targetItemName)) {
                        targetIndex = items.size + i;
                        break;
                    }
                }
            }
            
            // If we found the target item, make sure it's visible
            if (targetIndex != -1) {
                // If the item is above the visible area, scroll up
                if (targetIndex < topItemIndex) {
                    topItemIndex = targetIndex;
                }
                // If the item is below the visible area, scroll down
                else if (targetIndex >= topItemIndex + DisplayConfig.MAX_VISIBLE_ITEMS) {
                    topItemIndex = targetIndex - DisplayConfig.MAX_VISIBLE_ITEMS + 1;
                }
            }
        }
    }

    /**
     * Set font color based on item tier, with optional selection override
     */
    private void setColorByTier(swu.cp112.silkblade.entity.item.ItemTier tier, boolean isSelected) {
        if (isSelected) {
            font.setColor(DisplayConfig.SELECTED_COLOR);
            return;
        }

        if (tier == null) {
            font.setColor(DisplayConfig.DEFAULT_COLOR);
            return;
        }

        if (tier.isAnimated()) {
            // For Genesis tier, create rainbow effect
            float r = (float) Math.sin(genesisSineTime) * 0.5f + 0.5f;
            float g = (float) Math.sin(genesisSineTime + 2.0f) * 0.5f + 0.5f;
            float b = (float) Math.sin(genesisSineTime + 4.0f) * 0.5f + 0.5f;
            font.setColor(r, g, b, 1);
        } else {
            // For all other tiers, use the pre-defined color
            font.setColor(tier.getColor());
        }
    }

    /**
     * Draw a combat-selected star icon after the quantity
     */
    private void drawStarIcon(float x, float y, String fullText, boolean isSelected) {
        // Calculate position based on the width of the full text (including prefix if selected)
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, fullText);
        float textWidth = layout.width;

        // Use white tint for the black star texture
        batch.setColor(1, 1, 1, 1);

        // Position after the full text (name + quantity)
        batch.draw(
            starTexture,
            x + textWidth + MenuConfig.STAR_X_OFFSET,
            y + MenuConfig.STAR_Y_OFFSET - MenuConfig.STAR_SIZE, // Adjust Y for drawable position
            MenuConfig.STAR_SIZE,
            MenuConfig.STAR_SIZE
        );

        // Reset color to avoid affecting other sprites
        batch.setColor(1, 1, 1, 1);
    }
}
