package swu.cp112.silkblade.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import swu.cp112.silkblade.entity.item.ItemDatabase;
import swu.cp112.silkblade.entity.item.ItemTier;
import swu.cp112.silkblade.screen.transition.ScreenTransition;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Screen for buying items and equipment.
 */
public class ShopScreen implements Screen {

    /**
     * A shop item contains item data plus a price
     */
    private static class ShopItem {
        private final Object item; // Either Equipment or ConsumableItem
        private final int price;
        private final boolean requiresBoss1;
        private final boolean requiresBoss2;
        private final boolean requiresBoss3;
        private final boolean requiresBoss4;
        private final boolean requiresBoss5;

        public ShopItem(Object item, int price) {
            this(item, price, false, false, false, false, false);
        }

        public ShopItem(Object item, int price, boolean requiresBoss1, boolean requiresBoss2,
                       boolean requiresBoss3, boolean requiresBoss4, boolean requiresBoss5) {
            this.item = item;
            this.price = price;
            this.requiresBoss1 = requiresBoss1;
            this.requiresBoss2 = requiresBoss2;
            this.requiresBoss3 = requiresBoss3;
            this.requiresBoss4 = requiresBoss4;
            this.requiresBoss5 = requiresBoss5;
        }

        public Object getItem() {
            return item;
        }

        public int getPrice() {
            return price;
        }

        public boolean isAvailable(Player player) {
            if (requiresBoss1 && !player.isBoss1Defeated()) return false;
            if (requiresBoss2 && !player.isBoss2Defeated()) return false;
            if (requiresBoss3 && !player.isBoss3Defeated()) return false;
            if (requiresBoss4 && !player.isBoss4Defeated()) return false;
            if (requiresBoss5 && !player.isBoss5Defeated()) return false;
            return true;
        }

        public ItemTier getTier() {
            if (item instanceof Equipment) {
                return ((Equipment)item).getTier();
            } else if (item instanceof ConsumableItem) {
                return ((ConsumableItem)item).getTier();
            }
            return ItemTier.NORMAL;
        }

        public String getName() {
            if (item instanceof Equipment) {
                return ((Equipment)item).getName();
            } else if (item instanceof ConsumableItem) {
                return ((ConsumableItem)item).getName();
            }
            return "Unknown Item";
        }

        public String getDescription() {
            if (item instanceof Equipment) {
                return ((Equipment)item).getDescription();
            } else if (item instanceof ConsumableItem) {
                return ((ConsumableItem)item).getDescription();
            }
            return "";
        }
    }

    /**
     * Display configuration
     */
    private static final class DisplayConfig {
        // Layout
        static final float TITLE_Y_POSITION = 60;
        static final float PLAYER_STATS_START_Y = 400;
        static final float SECTION_TITLE_Y = 150;
        static final float ITEMS_START_Y = 200;
        static final float ITEMS_SPACING_Y = 40;
        static final float DESCRIPTION_Y = 350;
        static final float NAVIGATION_HELP_Y = 40;
        static final float LEFT_MARGIN = 60;
        static final float RIGHT_MARGIN = 520;
        static final float FONT_SCALE = 1.7f;
        static final float ITEM_FONT_SCALE = 1.3f;
        static final float STATS_FONT_SCALE = 1.1f;
        static final float DESC_FONT_SCALE = 1.0f;
        static final int MAX_VISIBLE_ITEMS = 10;
        static final float CATEGORY_Y = 100;

        // Item column layout
        static final float PRICE_STATUS_OFFSET = 350;
        static final float TITLE_PADDING = 20;
        static final float STAT_LINE_SPACING = 35;
        static final float SECTION_SPACING = 15;
        static final float DESCRIPTION_BOX_WIDTH = 430;
        static final float ITEM_NAME_MAX_WIDTH = 300;

        // Panel configuration
        static final float PANEL_PADDING = 20;
        static final float PANEL_BORDER_THICKNESS = 3;
        static final float DETAILS_PANEL_WIDTH = 470;
        static final float DETAILS_PANEL_HEIGHT = 480;
        static final float SHOP_PANEL_WIDTH = 550;
        static final float SHOP_PANEL_HEIGHT = 480;
        static final float CATEGORY_PANEL_HEIGHT = 60;
        static final float CATEGORY_PANEL_Y_OFFSET = 30;
        static final float PANEL_Y_OFFSET = 20;

        // Colors
        static final Color TITLE_COLOR = Color.WHITE;
        static final Color PLAYER_INFO_COLOR = Color.CYAN;
        static final Color SELECTED_COLOR = Color.YELLOW;
        static final Color EQUIPPED_COLOR = Color.GREEN;
        static final Color DEFAULT_COLOR = Color.WHITE;
        static final Color CONSUMABLE_COLOR = Color.WHITE;
        static final Color DESCRIPTION_COLOR = Color.LIGHT_GRAY;
        static final Color NAVIGATION_COLOR = Color.GRAY;
        static final Color BACKGROUND_COLOR = Color.BLACK;
        static final Color POSITIVE_BONUS_COLOR = Color.GREEN;
        static final Color NEGATIVE_BONUS_COLOR = Color.RED;
        static final Color STATS_LABEL_COLOR = Color.LIGHT_GRAY;
        static final Color DESCRIPTION_BACKGROUND = new Color(0, 0, 0, 0.8f);
        static final Color SCROLL_INDICATOR_COLOR = Color.YELLOW;
        static final Color GOLD_COLOR = Color.GOLD;
        static final Color PRICE_COLOR = new Color(0.9f, 0.8f, 0.1f, 1f);
        static final Color UNAVAILABLE_COLOR = Color.DARK_GRAY;
        static final Color UNLOCKED_COLOR = new Color(0.4f, 1f, 0.4f, 1f);
        static final Color CATEGORY_COLOR = Color.CORAL;
        static final Color SELECTED_CATEGORY_COLOR = Color.ORANGE;
        static final Color ITEM_EFFECT_COLOR = Color.GREEN;
        static final Color ITEM_TYPE_COLOR = Color.WHITE;
        static final Color ITEM_TIER_COLOR = Color.WHITE;
        static final Color PANEL_BACKGROUND = new Color(0.1f, 0.1f, 0.2f, 0.8f);
        static final Color PANEL_BORDER = new Color(0.5f, 0.5f, 0.7f, 1f);
        static final Color CATEGORY_PANEL_BACKGROUND = new Color(0.15f, 0.15f, 0.25f, 0.8f);
    }

    /**
     * Menu configuration
     */
    private static final class MenuConfig {
        static final String TITLE = "SHOP";
        static final String DETAILS_TITLE = "ITEM DETAILS";
        static final String ITEMS_TITLE = "ITEMS FOR SALE";
        static final String NAVIGATION_HELP = "< > Change Category | ^ v Navigate Items | Enter: Buy | E: Examine | Esc: Back";
        static final String NOT_ENOUGH_GOLD = "Not enough gold!";
        static final String INVENTORY_FULL = "Inventory full!";
        static final String ITEM_PURCHASED = "Item purchased!";
        static final String[] CATEGORIES = {"WEAPONS", "ARMORS", "ACCESSORY", "CONSUMABLE"};
    }

    /**
     * Audio configuration
     */
    private static final class AudioConfig {
        static final String FONT_PATH = "fonts/DTM.fnt";
        static final String SELECT_SOUND_PATH = "sounds/select.wav";
        static final String BUY_SOUND_PATH = "sounds/buy.wav";
        static final String ERROR_SOUND_PATH = "sounds/error.wav";
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
    private final ItemDatabase itemDB;

    /**
     * Audio components
     */
    private final Sound selectSound;
    private final Sound buySound;
    private final Sound errorSound;

    /**
     * State
     */
    private int selectedIndex = 0;
    private int topItemIndex = 0;
    private int selectedCategory = 0; // 0: weapons, 1: armors, 2: accessories, 3: consumables
    private boolean examiningItem = false;
    private String itemDescription = "";
    private boolean inputEnabled = true;
    private String statusMessage = "";
    private float statusMessageTimer = 0;
    private final com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;

    private float genesisSineTime = 0;

    // Added to track the item tier for the examine box outline
    private ItemTier currentExaminedItemTier = null;

    // Shop items list by category
    private final Array<ShopItem> weaponItems = new Array<>();
    private final Array<ShopItem> armorItems = new Array<>();
    private final Array<ShopItem> accessoryItems = new Array<>();
    private final Array<ShopItem> consumableItems = new Array<>();

    public ShopScreen(Game game) {
        this.game = game;
        this.viewport = Main.getViewport();
        this.camera = Main.getCamera();

        this.batch = new SpriteBatch();
        this.font = new BitmapFont(Gdx.files.internal(AudioConfig.FONT_PATH));
        font.setColor(DisplayConfig.DEFAULT_COLOR);
        font.getData().setScale(DisplayConfig.FONT_SCALE);

        this.selectSound = Gdx.audio.newSound(Gdx.files.internal(AudioConfig.SELECT_SOUND_PATH));
        this.buySound = Gdx.audio.newSound(Gdx.files.internal(AudioConfig.BUY_SOUND_PATH));
        this.errorSound = Gdx.audio.newSound(Gdx.files.internal(AudioConfig.ERROR_SOUND_PATH));

        // Initialize ShapeRenderer
        this.shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();

        // Load player data
        this.player = Player.loadFromFile();
        this.inventory = player.getInventory();
        this.itemDB = ItemDatabase.getInstance();

        // Initialize shop items
        initializeShopItems();
    }

    /**
     * Initialize shop items
     */
    private void initializeShopItems() {
        // Get all items from the database
        Array<Equipment> allEquipment = itemDB.getAllEquipment();
        Array<ConsumableItem> allConsumables = itemDB.getAllConsumables();

        // Starter items that should be excluded
        String[] starterItems = {
            "WEAPON_SOFT_LAMPHUN_SILK",
            "ARMOR_LAMPHUN_SHIRT",
            "ACCESSORY_SPIRIT_CHARM"
        };

        // Process all equipment
        for (Equipment equipment : allEquipment) {
            // Skip starter items
            boolean isStarter = false;
            for (String starterId : starterItems) {
                if (equipment.getId().equals(starterId)) {
                    isStarter = true;
                    break;
                }
            }
            if (isStarter) continue;

            // Assign price based on tier and type
            int price = calculateItemPrice(equipment);

            // Determine boss requirements based on tier
            boolean requiresBoss1 = equipment.getTier().ordinal() >= ItemTier.RARE.ordinal();
            boolean requiresBoss2 = equipment.getTier().ordinal() >= ItemTier.HEROIC.ordinal();
            boolean requiresBoss3 = equipment.getTier().ordinal() >= ItemTier.LEGENDARY.ordinal();
            boolean requiresBoss4 = equipment.getTier().ordinal() >= ItemTier.GENESIS.ordinal();
            boolean requiresBoss5 = equipment.getTier() == ItemTier.END;

            // Add to appropriate category
            ShopItem shopItem = new ShopItem(equipment, price, requiresBoss1, requiresBoss2, requiresBoss3, requiresBoss4, requiresBoss5);
            switch(equipment.getType()) {
                case WEAPON:
                    weaponItems.add(shopItem);
                    break;
                case ARMOR:
                    armorItems.add(shopItem);
                    break;
                case ACCESSORY:
                    accessoryItems.add(shopItem);
                    break;
            }
        }

        // Process all consumables
        for (ConsumableItem consumable : allConsumables) {
            // Skip starter items
            boolean isStarter = false;
            for (String starterId : starterItems) {
                if (consumable.getId().equals(starterId)) {
                    isStarter = true;
                    break;
                }
            }
            if (isStarter) continue;

            // Assign price based on tier and effect
            int price = calculateItemPrice(consumable);

            // Determine boss requirements based on tier
            boolean requiresBoss1 = consumable.getTier().ordinal() >= ItemTier.RARE.ordinal();
            boolean requiresBoss2 = consumable.getTier().ordinal() >= ItemTier.HEROIC.ordinal();
            boolean requiresBoss3 = consumable.getTier().ordinal() >= ItemTier.LEGENDARY.ordinal();
            boolean requiresBoss4 = consumable.getTier().ordinal() >= ItemTier.GENESIS.ordinal();
            boolean requiresBoss5 = consumable.getTier() == ItemTier.END;

            // Add to consumable category
            consumableItems.add(new ShopItem(consumable, price, requiresBoss1, requiresBoss2, requiresBoss3, requiresBoss4, requiresBoss5));
        }

        // Sort items by tier (lowest to highest) in each category
        sortItemsByTier(weaponItems);
        sortItemsByTier(armorItems);
        sortItemsByTier(accessoryItems);
        sortItemsByTier(consumableItems);
    }

    /**
     * Sort shop items by tier (normal to genesis) and then alphabetically by name
     */
    private void sortItemsByTier(Array<ShopItem> items) {
        items.sort(new java.util.Comparator<ShopItem>() {
            @Override
            public int compare(ShopItem item1, ShopItem item2) {
                // First sort by rarity (lowest to highest)
                int rarityCompare = item1.getTier().ordinal() - item2.getTier().ordinal();
                if (rarityCompare != 0) {
                    return rarityCompare;
                }

                // If same rarity, sort alphabetically
                return item1.getName().compareTo(item2.getName());
            }
        });
    }

    /**
     * Calculate price for equipment based on tier and stats
     */
    private int calculateItemPrice(Equipment equipment) {
        int basePrice;
        switch(equipment.getTier()) {
            case NORMAL:
                basePrice = 75;  // Reduced from 100
                break;
            case RARE:
                basePrice = 300; // Reduced from 500
                break;
            case HEROIC:
                basePrice = 800; // Reduced from 1500
                break;
            case LEGENDARY:
                basePrice = 2000; // Reduced from 5000
                break;
            case GENESIS:
                basePrice = 8000; // Reduced from 20000
                break;
            case END:
                basePrice = 18000; // Reduced from 50000
                break;
            default:
                basePrice = 75;
        }

        // Adjust price based on equipment stats (reduced multipliers)
        int statModifier = 0;
        statModifier += equipment.getAttackBonus() * 4; // Reduced from 10
        statModifier += equipment.getDefenseBonus() * 4; // Reduced from 10
        statModifier += equipment.getMaxHPBonus() * 1; // Reduced from 2
        statModifier += equipment.getMaxMPBonus() * 1; // Reduced from 2
        statModifier += (int)(equipment.getCritRateBonus() * 500); // Reduced from 1000
        statModifier += (int)(equipment.getMaxHPPercentBonus() * 500); // Reduced from 1000 
        statModifier += (int)(equipment.getMaxMPPercentBonus() * 500); // Reduced from 1000
        statModifier += (int)(equipment.getAttackPercentBonus() * 500); // Reduced from 1000
        statModifier += (int)(equipment.getDefensePercentBonus() * 500); // Reduced from 1000

        return basePrice + statModifier;
    }

    /**
     * Calculate price for consumable items based on tier and effect
     */
    private int calculateItemPrice(ConsumableItem consumable) {
        int basePrice;
        switch(consumable.getTier()) {
            case NORMAL:
                basePrice = 25; // Reduced from 50
                break;
            case RARE:
                basePrice = 100; // Reduced from 200
                break;
            case HEROIC:
                basePrice = 300; // Reduced from 600
                break;
            case LEGENDARY:
                basePrice = 600; // Reduced from 1200
                break;
            case GENESIS:
                basePrice = 2000; // Reduced from 5000
                break;
            case END:
                basePrice = 5000; // Reduced from 12000
                break;
            default:
                basePrice = 25; // Reduced from 50
        }

        // Adjust price based on effect (reduced multipliers)
        switch(consumable.getEffect()) {
            case HEAL_HP:
                basePrice += consumable.getEffectAmount() * 0.25; // Reduced from 0.5
                break;
            case RESTORE_MP:
                basePrice += consumable.getEffectAmount() * 0.4; // Reduced from 0.75
                break;
            case FULL_HEAL:
                basePrice += 150; // Reduced from 300
                break;
            case FULL_RESTORE:
                basePrice += 250; // Reduced from 500
                break;
        }

        // Add for secondary effects (reduced prices)
        if (consumable.getSecondaryEffect() != null) {
            basePrice += 50 + consumable.getSecondaryEffectAmount() * 0.25; // Reduced from 100 + 0.5
        }

        // Add for buffs (reduced prices)
        if (consumable.getBuffDuration() > 0) {
            basePrice += consumable.getBuffDuration() * 25; // Reduced from 50
            basePrice += (consumable.getBuffAtkAmount() + consumable.getBuffDefAmount()) * 10; // Reduced from 20
        }

        return basePrice;
    }

    private Array<ShopItem> getCurrentCategoryItems() {
        switch (selectedCategory) {
            case 0: return weaponItems;
            case 1: return armorItems;
            case 2: return accessoryItems;
            case 3: return consumableItems;
            default: return weaponItems;
        }
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

        // Update status message timer
        if (statusMessageTimer > 0) {
            statusMessageTimer -= delta;
            if (statusMessageTimer <= 0) {
                statusMessage = "";
            }
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

        // Draw panels first (before starting batch)
        drawPanels(screenWidth, screenHeight);

        batch.begin();

        // Draw title
        font.setColor(DisplayConfig.TITLE_COLOR);
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, MenuConfig.TITLE);
        font.draw(batch, MenuConfig.TITLE,
                screenWidth / 2 - layout.width / 2,
                screenHeight - DisplayConfig.TITLE_Y_POSITION);

//        // Draw player gold
//        font.setColor(DisplayConfig.GOLD_COLOR);
//        String goldText = "Gold: " + player.getGold();
//        font.draw(batch, goldText,
//                screenWidth / 2 - layout.width / 2,
//                screenHeight - DisplayConfig.TITLE_Y_POSITION - 60);

        // Draw section titles
        float originalScale = font.getData().scaleX;
        font.getData().setScale(DisplayConfig.ITEM_FONT_SCALE);

        font.setColor(DisplayConfig.DEFAULT_COLOR);
        font.draw(batch, MenuConfig.DETAILS_TITLE,
                DisplayConfig.LEFT_MARGIN,
                screenHeight - DisplayConfig.SECTION_TITLE_Y);

        font.draw(batch, MenuConfig.ITEMS_TITLE,
                DisplayConfig.RIGHT_MARGIN,
                screenHeight - DisplayConfig.SECTION_TITLE_Y);

        // Draw category navigation
        drawCategoryNavigation(screenWidth, screenHeight);

        // Draw item details (left side)
        drawItemDetails(screenHeight);

        // Draw shop items (right side)
        drawShopItems(screenHeight);

        // Draw status message if exists
        if (statusMessage != null && !statusMessage.isEmpty() && statusMessageTimer > 0) {
            font.setColor(DisplayConfig.SELECTED_COLOR);
            layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, statusMessage);
            font.draw(batch, statusMessage,
                    screenWidth / 2 - layout.width / 2,
                    screenHeight / 2 - 100);
        }

        // Draw navigation help
        font.getData().setScale(DisplayConfig.DESC_FONT_SCALE);
        font.setColor(DisplayConfig.NAVIGATION_COLOR);
        layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, MenuConfig.NAVIGATION_HELP);
        font.draw(batch, MenuConfig.NAVIGATION_HELP,
                screenWidth / 2 - layout.width / 2,
                DisplayConfig.NAVIGATION_HELP_Y);

        // Draw item description if examining - in a box in the middle of the screen
        if (examiningItem && !itemDescription.isEmpty()) {
            drawItemDescriptionBox(screenWidth, screenHeight);
        }

        // Reset font scale
        font.getData().setScale(originalScale);

        batch.end();
    }

    /**
     * Draw all panels with backgrounds and borders
     */
    private void drawPanels(float screenWidth, float screenHeight) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw details panel (left side) - adjusted to include section header
        float detailsPanelX = DisplayConfig.LEFT_MARGIN - DisplayConfig.PANEL_PADDING;
        float detailsPanelY = screenHeight - DisplayConfig.ITEMS_START_Y - DisplayConfig.DETAILS_PANEL_HEIGHT + DisplayConfig.PANEL_Y_OFFSET;
        drawPanel(
            detailsPanelX,
            detailsPanelY,
            DisplayConfig.DETAILS_PANEL_WIDTH,
            DisplayConfig.DETAILS_PANEL_HEIGHT,
            DisplayConfig.PANEL_BACKGROUND,
            DisplayConfig.PANEL_BORDER
        );

        // Draw shop items panel (right side) - adjusted to include section header
        float shopPanelX = DisplayConfig.RIGHT_MARGIN - DisplayConfig.PANEL_PADDING;
        float shopPanelY = screenHeight - DisplayConfig.ITEMS_START_Y - DisplayConfig.SHOP_PANEL_HEIGHT + DisplayConfig.PANEL_Y_OFFSET;
        drawPanel(
            shopPanelX,
            shopPanelY,
            DisplayConfig.SHOP_PANEL_WIDTH,
            DisplayConfig.SHOP_PANEL_HEIGHT,
            DisplayConfig.PANEL_BACKGROUND,
            DisplayConfig.PANEL_BORDER
        );

        // Draw category panel
        float categoryPanelWidth = screenWidth - 120; // Margin on both sides
        float categoryPanelX = (screenWidth - categoryPanelWidth) / 2;
        float categoryPanelY = screenHeight - DisplayConfig.CATEGORY_Y - DisplayConfig.CATEGORY_PANEL_Y_OFFSET;
        drawPanel(
            categoryPanelX,
            categoryPanelY,
            categoryPanelWidth,
            DisplayConfig.CATEGORY_PANEL_HEIGHT,
            DisplayConfig.CATEGORY_PANEL_BACKGROUND,
            DisplayConfig.PANEL_BORDER
        );

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Draw a single panel with background and border
     */
    private void drawPanel(float x, float y, float width, float height, Color backgroundColor, Color borderColor) {
        // Draw panel background
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(backgroundColor);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        // Draw panel border
        Gdx.gl.glLineWidth(DisplayConfig.PANEL_BORDER_THICKNESS);
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(borderColor);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    private void drawItemDetails(float screenHeight) {
        float originalScale = font.getData().scaleX;
        float statLineHeight = DisplayConfig.STAT_LINE_SPACING;

        // If no item is selected, show a default message
        if (getCurrentCategoryItems().size == 0 || selectedIndex < 0 || selectedIndex >= getCurrentCategoryItems().size) {
            font.setColor(DisplayConfig.DEFAULT_COLOR);
            font.getData().setScale(DisplayConfig.ITEM_FONT_SCALE);
            font.draw(batch, "Select an item to view details",
                    DisplayConfig.LEFT_MARGIN,
                    screenHeight - DisplayConfig.ITEMS_START_Y);
            font.getData().setScale(originalScale);
            return;
        }

        // Get the selected shop item
        ShopItem item = getCurrentCategoryItems().get(selectedIndex);

        // Start position for drawing details
        float yPos = screenHeight - DisplayConfig.ITEMS_START_Y;

        // Draw the item name with its tier color - larger font for visual hierarchy
        font.getData().setScale(DisplayConfig.ITEM_FONT_SCALE * 1.2f);
        setColorByTier(item.getTier(), false);
        font.draw(batch, item.getName(),
                DisplayConfig.LEFT_MARGIN,
                yPos);
        yPos -= statLineHeight + 20;

        // Use smaller font for price and other details
        font.getData().setScale(DisplayConfig.ITEM_FONT_SCALE * 0.9f);

        // Draw the price
        font.setColor(DisplayConfig.PRICE_COLOR);
        font.draw(batch, "Price: " + item.getPrice() + " gold",
                DisplayConfig.LEFT_MARGIN,
                yPos);
        yPos -= statLineHeight;

        // Draw item type and tier with better spacing
        font.setColor(DisplayConfig.ITEM_TYPE_COLOR);
        String itemType = "";
        if (item.getItem() instanceof Equipment) {
            Equipment equipment = (Equipment) item.getItem();
            itemType = "Type: " + equipment.getType().getDisplayName();
        } else if (item.getItem() instanceof ConsumableItem) {
            itemType = "Type: Consumable";
        }

        font.draw(batch, itemType,
                DisplayConfig.LEFT_MARGIN,
                yPos);
        yPos -= statLineHeight;

        font.setColor(DisplayConfig.ITEM_TIER_COLOR);
        font.draw(batch, "Tier: " + item.getTier().getDisplayName(),
                DisplayConfig.LEFT_MARGIN,
                yPos);
        yPos -= statLineHeight;

        // Draw item stats with proper spacing
        if (item.getItem() instanceof Equipment) {
            Equipment equipment = (Equipment) item.getItem();

            // Add spacing before stats
            yPos -= DisplayConfig.SECTION_SPACING;

            if (equipment.getAttackBonus() != 0) {
                font.setColor(equipment.getAttackBonus() > 0 ? DisplayConfig.POSITIVE_BONUS_COLOR : DisplayConfig.NEGATIVE_BONUS_COLOR);
                font.draw(batch, "Attack: " + formatBonus(equipment.getAttackBonus()),
                        DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;
            }

            if (equipment.getAttackPercentBonus() > 0) {
                font.setColor(DisplayConfig.POSITIVE_BONUS_COLOR);
                font.draw(batch, "Attack: " + formatPercentBonus(equipment.getAttackPercentBonus()),
                        DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;
            }

            if (equipment.getDefenseBonus() != 0) {
                font.setColor(equipment.getDefenseBonus() > 0 ? DisplayConfig.POSITIVE_BONUS_COLOR : DisplayConfig.NEGATIVE_BONUS_COLOR);
                font.draw(batch, "Defense: " + formatBonus(equipment.getDefenseBonus()),
                        DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;
            }

            if (equipment.getDefensePercentBonus() > 0) {
                font.setColor(DisplayConfig.POSITIVE_BONUS_COLOR);
                font.draw(batch, "Defense: " + formatPercentBonus(equipment.getDefensePercentBonus()),
                        DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;
            }

            if (equipment.getMaxHPBonus() != 0) {
                font.setColor(equipment.getMaxHPBonus() > 0 ? DisplayConfig.POSITIVE_BONUS_COLOR : DisplayConfig.NEGATIVE_BONUS_COLOR);
                font.draw(batch, "Max HP: " + formatBonus(equipment.getMaxHPBonus()),
                        DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;
            }

            if (equipment.getMaxHPPercentBonus() > 0) {
                font.setColor(DisplayConfig.POSITIVE_BONUS_COLOR);
                font.draw(batch, "Max HP: " + formatPercentBonus(equipment.getMaxHPPercentBonus()),
                        DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;
            }

            if (equipment.getMaxMPBonus() != 0) {
                font.setColor(equipment.getMaxMPBonus() > 0 ? DisplayConfig.POSITIVE_BONUS_COLOR : DisplayConfig.NEGATIVE_BONUS_COLOR);
                font.draw(batch, "Max MP: " + formatBonus(equipment.getMaxMPBonus()),
                        DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;
            }

            if (equipment.getMaxMPPercentBonus() > 0) {
                font.setColor(DisplayConfig.POSITIVE_BONUS_COLOR);
                font.draw(batch, "Max MP: " + formatPercentBonus(equipment.getMaxMPPercentBonus()),
                        DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;
            }

            if (equipment.getCritRateBonus() > 0) {
                font.setColor(DisplayConfig.POSITIVE_BONUS_COLOR);
                font.draw(batch, "Crit Rate: " + formatPercentBonus(equipment.getCritRateBonus()),
                        DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;
            }

            // Special properties section
            boolean hasSpecialProps = equipment.hasDoubleAttack() || equipment.getThornDamage() > 0 ||
                                    equipment.hasDeathDefiance() || equipment.hasFreeSkillCast();

            if (hasSpecialProps) {
                yPos -= DisplayConfig.SECTION_SPACING;
                font.setColor(DisplayConfig.ITEM_EFFECT_COLOR);
                font.draw(batch, "Special Properties:", DisplayConfig.LEFT_MARGIN, yPos);
                yPos -= statLineHeight;

                if (equipment.hasDoubleAttack()) {
                    font.setColor(DisplayConfig.POSITIVE_BONUS_COLOR);
                    font.draw(batch, "Double Attack", DisplayConfig.LEFT_MARGIN, yPos);
                    yPos -= statLineHeight;
                }

                if (equipment.getThornDamage() > 0) {
                    font.setColor(DisplayConfig.POSITIVE_BONUS_COLOR);
                    font.draw(batch, "Thorn Damage: " + formatPercentBonus(equipment.getThornDamage()),
                            DisplayConfig.LEFT_MARGIN, yPos);
                    yPos -= statLineHeight;
                }

                if (equipment.hasDeathDefiance()) {
                    font.setColor(DisplayConfig.POSITIVE_BONUS_COLOR);
                    font.draw(batch, "Death Defiance", DisplayConfig.LEFT_MARGIN, yPos);
                    yPos -= statLineHeight;
                }

                if (equipment.hasFreeSkillCast()) {
                    font.setColor(DisplayConfig.POSITIVE_BONUS_COLOR);
                    font.draw(batch, "Free Skill Cast", DisplayConfig.LEFT_MARGIN, yPos);
                    yPos -= statLineHeight;
                }
            }
        }
        // Draw effect if it's a consumable
        else if (item.getItem() instanceof ConsumableItem) {
            ConsumableItem consumable = (ConsumableItem) item.getItem();

            // Add spacing before stats
            yPos -= DisplayConfig.SECTION_SPACING;

            font.setColor(DisplayConfig.ITEM_EFFECT_COLOR);
            font.draw(batch, "Effect: " + consumable.getEffect().getDescription(),
                    DisplayConfig.LEFT_MARGIN,
                    yPos);
            yPos -= statLineHeight;

            // Check if it's a full heal/restore effect
            if (consumable.getEffect() == ConsumableItem.ItemEffect.FULL_HEAL ||
                consumable.getEffect() == ConsumableItem.ItemEffect.FULL_RESTORE) {
                font.draw(batch, "Value: 100%",
                        DisplayConfig.LEFT_MARGIN,
                        yPos);
            } else {
                font.draw(batch, "Value: " + consumable.getEffectAmount(),
                        DisplayConfig.LEFT_MARGIN,
                        yPos);
            }
            yPos -= statLineHeight;
        }

        // Add spacing before description
        yPos -= DisplayConfig.SECTION_SPACING;

        // Use even smaller font for description
        font.getData().setScale(DisplayConfig.ITEM_FONT_SCALE * 0.8f);

        // Draw description with proper wrapping and nicer layout
        font.setColor(DisplayConfig.DESCRIPTION_COLOR);
        float descWidth = DisplayConfig.DESCRIPTION_BOX_WIDTH;

        // Center longer descriptions for better readability
        String description = item.getDescription();
        if (description.length() > 30) {
            // For longer descriptions, center them and add some margins
            float centerX = DisplayConfig.LEFT_MARGIN + (descWidth / 2);
            font.draw(batch, description,
                    DisplayConfig.LEFT_MARGIN,
                    yPos,
                    descWidth, 1, true);
        } else {
            // For shorter descriptions, center them manually for better appearance
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, description);
            float textX = DisplayConfig.LEFT_MARGIN + (descWidth - layout.width) / 2;
            font.draw(batch, description, textX, yPos);
        }

        // Draw availability status for locked items
        if (!item.isAvailable(player)) {
            yPos -= statLineHeight * 2.5f;
            font.setColor(Color.RED);
            font.draw(batch, "Locked - Defeat more bosses to unlock",
                    DisplayConfig.LEFT_MARGIN,
                    yPos);
        }

        // Reset font scale
        font.getData().setScale(originalScale);
    }

    private void drawShopItems(float screenHeight) {
        float originalScale = font.getData().scaleX;
        font.getData().setScale(DisplayConfig.ITEM_FONT_SCALE);

        Array<ShopItem> items = getCurrentCategoryItems();

        // If no items are available
        if (items.size == 0) {
            font.setColor(DisplayConfig.DEFAULT_COLOR);
            font.draw(batch, "No items available in this category", DisplayConfig.RIGHT_MARGIN, screenHeight - DisplayConfig.ITEMS_START_Y);
            font.getData().setScale(originalScale);
            return;
        }

        // Determine visible range
        int endIndex = Math.min(topItemIndex + DisplayConfig.MAX_VISIBLE_ITEMS, items.size);

        // Draw visible items
        for (int i = topItemIndex; i < endIndex; i++) {
            ShopItem item = items.get(i);
            boolean isSelected = i == selectedIndex;
            boolean isAvailable = item.isAvailable(player);

            float y = screenHeight - DisplayConfig.ITEMS_START_Y - (i - topItemIndex) * DisplayConfig.ITEMS_SPACING_Y;

            // Item prefix
            String prefix = isSelected ? "> " : "  ";

            // Item name with tier-based color
            if (isAvailable) {
                setColorByTier(item.getTier(), isSelected);
            } else {
                font.setColor(DisplayConfig.UNAVAILABLE_COLOR);
            }

            // Get tier number for display
            int tierNumber = item.getTier().ordinal() + 1; // +1 because NORMAL=0, RARE=1, etc.

            // Draw item name (truncate if too long) with tier in brackets
            String itemName = item.getName();
            if (itemName.length() > 14) {
                itemName = itemName.substring(0, 12) + "..";
            }

            // Add tier number in brackets
            String displayName = prefix + itemName + " [T" + tierNumber + "]";
            font.draw(batch, displayName, DisplayConfig.RIGHT_MARGIN, y);

            // Draw price/availability status in the same position (consolidated)
            if (!isAvailable) {
                // For locked items, show locked status
                font.draw(batch, "[LOCKED]", DisplayConfig.RIGHT_MARGIN + DisplayConfig.PRICE_STATUS_OFFSET, y);
            } else {
                // For available items, show price
                font.setColor(DisplayConfig.PRICE_COLOR);
                String priceText = item.getPrice() + "g";
                font.draw(batch, priceText, DisplayConfig.RIGHT_MARGIN + DisplayConfig.PRICE_STATUS_OFFSET, y);
            }
        }

        // Draw scroll indicators if needed
        if (topItemIndex > 0) {
            font.setColor(DisplayConfig.SCROLL_INDICATOR_COLOR);
            font.draw(batch, "^ MORE ITEMS ^",
                    DisplayConfig.RIGHT_MARGIN + 100,
                    screenHeight - DisplayConfig.ITEMS_START_Y + DisplayConfig.ITEMS_SPACING_Y);
        }

        if (endIndex < items.size) {
            font.setColor(DisplayConfig.SCROLL_INDICATOR_COLOR);
            font.draw(batch, "v MORE ITEMS v",
                    DisplayConfig.RIGHT_MARGIN + 100,
                    screenHeight - DisplayConfig.ITEMS_START_Y - DisplayConfig.MAX_VISIBLE_ITEMS * DisplayConfig.ITEMS_SPACING_Y - 20);
        }

        // Reset font scale
        font.getData().setScale(originalScale);
    }

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
        boxWidth = Math.min(boxWidth, screenWidth * 0.7f); // Cap maximum width to 70%

        // Count number of lines in description (roughly estimate based on width)
        int numLines = (int)Math.ceil(descLayout.width / boxWidth) + itemDescription.split("\n").length;
        float lineHeight = descLayout.height * 1.4f; // Slightly reduced spacing

        // Calculate height with fixed padding at the bottom for close text
        float contentHeight = lineHeight * numLines + titleLayout.height + 60; // Add padding
        float boxHeight = Math.max(contentHeight + closeLayout.height + 60, screenHeight * 0.3f); // Minimum height
        boxHeight = Math.min(boxHeight, screenHeight * 0.75f); // Cap maximum height (increased from 65%)

        // Center the box on screen
        float boxX = (screenWidth - boxWidth) / 2;
        float boxY = (screenHeight - boxHeight) / 2;

        // Draw semi-transparent background
        Gdx.gl.glEnable(GL20.GL_BLEND);
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
                if (currentExaminedItemTier == ItemTier.GENESIS) {
                    // For Genesis tier, create blue-purple galaxy effect
                    float r = (float) Math.abs(Math.sin(genesisSineTime * 0.5f)) * 0.3f + 0.3f; // limited red
                    float g = (float) Math.abs(Math.sin(genesisSineTime * 0.7f)) * 0.2f + 0.1f; // very limited green
                    float b = (float) Math.abs(Math.sin(genesisSineTime * 0.9f)) * 0.4f + 0.6f; // strong blue base
                    shapeRenderer.setColor(r, g, b, 1f);
                } else if (currentExaminedItemTier == ItemTier.END) {
                    // For END tier, create true rainbow effect
                    float r = (float) Math.sin(genesisSineTime) * 0.5f + 0.5f;
                    float g = (float) Math.sin(genesisSineTime + 2.0f) * 0.5f + 0.5f;
                    float b = (float) Math.sin(genesisSineTime + 4.0f) * 0.5f + 0.5f;
                    shapeRenderer.setColor(r, g, b, 1f);
                }
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

        // Draw actual description with a maximum height to prevent overflow
        font.setColor(DisplayConfig.DESCRIPTION_COLOR);
        float maxDescHeight = boxHeight - 120; // Leave space for title and close text
        font.draw(batch, itemDescription,
                boxX + 20,
                boxY + boxHeight - 30 - titleLayout.height - 20, // Position below title with padding
                boxWidth - 40, // Padding on both sides
                1, // Align left
                true); // Wrap

        // Draw close instruction at bottom of box with more padding
        font.setColor(DisplayConfig.NAVIGATION_COLOR);
        font.draw(batch, closeText,
                screenWidth / 2 - closeLayout.width / 2,
                boxY + 30); // Fixed position from bottom
    }

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            // Change category (move left)
            selectedCategory = (selectedCategory - 1 + MenuConfig.CATEGORIES.length) % MenuConfig.CATEGORIES.length;
            selectSound.play();

            // Reset selection indices for the new category
            selectedIndex = 0;
            topItemIndex = 0;

            // Update details for the newly selected item
            updateSelectedItemDetails();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            // Change category (move right)
            selectedCategory = (selectedCategory + 1) % MenuConfig.CATEGORIES.length;
            selectSound.play();

            // Reset selection indices for the new category
            selectedIndex = 0;
            topItemIndex = 0;

            // Update details for the newly selected item
            updateSelectedItemDetails();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            moveSelection(-1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            moveSelection(1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            // Buy selected item
            buySelected();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            // Examine item
            examineSelected();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goBack();
        }
    }

    private void moveSelection(int direction) {
        Array<ShopItem> items = getCurrentCategoryItems();
        if (items.size == 0) {
            return;
        }

        int originalSelectedIndex = selectedIndex;

        // Adjust selection with wrapping
        selectedIndex = (selectedIndex + direction + items.size) % items.size;

        // Handle scrolling if the selection moves outside the visible area
        if (selectedIndex < topItemIndex) {
            topItemIndex = selectedIndex;
        } else if (selectedIndex >= topItemIndex + DisplayConfig.MAX_VISIBLE_ITEMS) {
            topItemIndex = selectedIndex - DisplayConfig.MAX_VISIBLE_ITEMS + 1;
        }

        // Handle wrap-around scrolling
        if (originalSelectedIndex > selectedIndex && direction < 0 && selectedIndex == items.size - 1) {
            // When wrapping from top to bottom
            topItemIndex = Math.max(0, items.size - DisplayConfig.MAX_VISIBLE_ITEMS);
        } else if (originalSelectedIndex < selectedIndex && direction > 0 && selectedIndex == 0) {
            // When wrapping from bottom to top
            topItemIndex = 0;
        }

        selectSound.play();
        updateSelectedItemDetails();
    }

    private void updateSelectedItemDetails() {
        Array<ShopItem> items = getCurrentCategoryItems();
        if (items.size > 0 && selectedIndex >= 0 && selectedIndex < items.size) {
            ShopItem item = items.get(selectedIndex);
            // We don't actually need to do anything here since drawItemDetails will read directly
            // from the current selection
        }
    }

    private void examineSelected() {
        Array<ShopItem> items = getCurrentCategoryItems();
        if (items.size == 0 || selectedIndex < 0 || selectedIndex >= items.size) {
            return;
        }

        ShopItem item = items.get(selectedIndex);
        buildItemDescription(item);
        examiningItem = true;
        selectSound.play();
    }

    private void buySelected() {
        Array<ShopItem> items = getCurrentCategoryItems();
        if (items.size == 0 || selectedIndex < 0 || selectedIndex >= items.size) {
            return;
        }

        ShopItem selectedItem = items.get(selectedIndex);

        // Check if item is available based on boss defeats
        if (!selectedItem.isAvailable(player)) {
            statusMessage = "Item is locked! Defeat more bosses to unlock.";
            statusMessageTimer = 2.0f;
            errorSound.play();
            return;
        }

        // Check if player has enough gold
        if (player.getGold() < selectedItem.getPrice()) {
            statusMessage = MenuConfig.NOT_ENOUGH_GOLD;
            statusMessageTimer = 2.0f;
            errorSound.play();
            return;
        }

        // Try to add to inventory
        boolean added = false;

        if (selectedItem.getItem() instanceof Equipment) {
            Equipment equipment = (Equipment) selectedItem.getItem();
            added = inventory.addToInventory(equipment);

            if (!added) {
                statusMessage = MenuConfig.INVENTORY_FULL;
                statusMessageTimer = 2.0f;
                errorSound.play();
                return;
            }
        } else if (selectedItem.getItem() instanceof ConsumableItem) {
            ConsumableItem consumable = (ConsumableItem) selectedItem.getItem();

            // Check if the player already has this consumable to stack it
            boolean hasExisting = false;
            for (ConsumableItem existingItem : inventory.getConsumableItems()) {
                // If the item has the same ID and tier, stack it
                if (existingItem.getId().equals(consumable.getId()) &&
                    existingItem.getTier().equals(consumable.getTier())) {

                    // Increase quantity instead of adding a new item
                    existingItem.setQuantity(existingItem.getQuantity() + 1);
                    hasExisting = true;
                    added = true;
                    break;
                }
            }

            // If no existing item found, add a new one
            if (!hasExisting) {
                // Set quantity to 1 for new item
                consumable.setQuantity(1);
                added = inventory.addConsumableItem(consumable);
            }

            if (!added) {
                statusMessage = "Cannot add more of this item!";
                statusMessageTimer = 2.0f;
                errorSound.play(0.35f);
                return;
            }
        }

        // Deduct gold
        player.setGold(player.getGold() - selectedItem.getPrice());

        // Save player data
        player.saveToFile();

        // Show success message
        statusMessage = MenuConfig.ITEM_PURCHASED;
        statusMessageTimer = 2.0f;
        buySound.play(0.35f);
    }

    private void buildItemDescription(ShopItem shopItem) {
        StringBuilder sb = new StringBuilder();
        int tierNumber = shopItem.getTier().ordinal() + 1; // +1 because NORMAL=0, RARE=1, etc.

        // Set the current examined item tier for the outline color
        currentExaminedItemTier = shopItem.getTier();

        if (shopItem.getItem() instanceof Equipment) {
            Equipment equipment = (Equipment) shopItem.getItem();

            // Add tier and equipment type
            sb.append("Tier ").append(tierNumber).append(" ").append(shopItem.getTier().getDisplayName()).append("\n");
            sb.append("Type: ").append(equipment.getType().getDisplayName()).append("\n\n");

            // Add description
            sb.append(equipment.getDescription()).append("\n\n");

            // Add stats bonuses
            sb.append("Bonuses:\n");
            if (equipment.getAttackBonus() != 0) {
                sb.append("ATK: ").append(formatBonus(equipment.getAttackBonus()));
                if (equipment.getAttackPercentBonus() > 0) {
                    sb.append(", ").append(formatPercentBonus(equipment.getAttackPercentBonus())).append(" ATK");
                }
                sb.append("\n");
            }
            if (equipment.getDefenseBonus() != 0) {
                sb.append("DEF: ").append(formatBonus(equipment.getDefenseBonus()));
                if (equipment.getDefensePercentBonus() > 0) {
                    sb.append(", ").append(formatPercentBonus(equipment.getDefensePercentBonus())).append(" DEF");
                }
                sb.append("\n");
            }
            if (equipment.getMaxHPBonus() != 0) {
                sb.append("HP: ").append(formatBonus(equipment.getMaxHPBonus()));
                if (equipment.getMaxHPPercentBonus() > 0) {
                    sb.append(", ").append(formatPercentBonus(equipment.getMaxHPPercentBonus())).append(" HP");
                }
                sb.append("\n");
            }
            if (equipment.getMaxMPBonus() != 0) {
                sb.append("MP: ").append(formatBonus(equipment.getMaxMPBonus()));
                if (equipment.getMaxMPPercentBonus() > 0) {
                    sb.append(", ").append(formatPercentBonus(equipment.getMaxMPPercentBonus())).append(" MP");
                }
                sb.append("\n");
            }
            if (equipment.getCritRateBonus() > 0) {
                sb.append("CRIT: +").append(Math.round(equipment.getCritRateBonus() * 100)).append("%\n");
            }

            // Special effects
            if (equipment.hasDoubleAttack()) {
                sb.append("Special: Chance for double attack\n");
            }
            if (equipment.getThornDamage() > 0) {
                sb.append("Special: ").append(Math.round(equipment.getThornDamage() * 100)).append("% damage reflection\n");
            }
            if (equipment.hasDeathDefiance()) {
                sb.append("Special: Death Defiance (survive fatal damage once per battle)\n");
            }
            if (equipment.hasFreeSkillCast()) {
                sb.append("Special: Free Skill Cast (cast any skill without MP cost once per battle)\n");
            }

            // Add price
            sb.append("\nPrice: ").append(shopItem.getPrice()).append(" gold");

        } else if (shopItem.getItem() instanceof ConsumableItem) {
            ConsumableItem consumable = (ConsumableItem) shopItem.getItem();

            // Add tier and consumable type
            sb.append("Tier ").append(tierNumber).append(" ").append(shopItem.getTier().getDisplayName()).append("\n");
            sb.append("Type: Consumable\n\n");

            // Add description
            sb.append(consumable.getDescription()).append("\n\n");

            // Add effects
            sb.append("Effects:\n");
            sb.append("- ").append(consumable.getEffect().getDescription());
            if (consumable.getEffect() != ConsumableItem.ItemEffect.FULL_HEAL &&
                consumable.getEffect() != ConsumableItem.ItemEffect.FULL_RESTORE) {
                sb.append(" (").append(consumable.getEffectAmount()).append(")");
            } else {
                sb.append(" (100%)");
            }
            sb.append("\n");

            // Secondary effect if any
            if (consumable.getSecondaryEffect() != null) {
                sb.append("- ").append(consumable.getSecondaryEffect().getDescription());
                if (consumable.getSecondaryEffect() != ConsumableItem.ItemEffect.FULL_HEAL &&
                    consumable.getSecondaryEffect() != ConsumableItem.ItemEffect.FULL_RESTORE) {
                    sb.append(" (").append(consumable.getSecondaryEffectAmount()).append(")");
                } else {
                    sb.append(" (100%)");
                }
                sb.append("\n");
            }

            // Buffs if any
            if (consumable.getBuffDuration() > 0) {
                sb.append("\nBuffs (").append(consumable.getBuffDuration()).append(" turns):\n");
                if (consumable.getBuffAtkAmount() > 0) {
                    sb.append("- ATK +").append(consumable.getBuffAtkAmount()).append("\n");
                }
                if (consumable.getBuffDefAmount() > 0) {
                    sb.append("- DEF +").append(consumable.getBuffDefAmount()).append("\n");
                }
            }

            // Add price
            sb.append("\nPrice: ").append(shopItem.getPrice()).append(" gold");
        }
        itemDescription = sb.toString();
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
     * Set font color based on item tier, with optional selection override
     */
    private void setColorByTier(ItemTier tier, boolean isSelected) {
        if (isSelected) {
            font.setColor(DisplayConfig.SELECTED_COLOR);
            return;
        }

        if (tier == null) {
            font.setColor(DisplayConfig.DEFAULT_COLOR);
            return;
        }

        if (tier.isAnimated()) {
            if (tier == ItemTier.GENESIS) {
                // For Genesis tier, create blue-purple galaxy effect
                float r = (float) Math.abs(Math.sin(genesisSineTime * 0.5f)) * 0.3f + 0.3f; // limited red
                float g = (float) Math.abs(Math.sin(genesisSineTime * 0.7f)) * 0.2f + 0.1f; // very limited green
                float b = (float) Math.abs(Math.sin(genesisSineTime * 0.9f)) * 0.4f + 0.6f; // strong blue base
                font.setColor(r, g, b, 1);
            } else if (tier == ItemTier.END) {
                // For END tier, create true rainbow effect
                float r = (float) Math.sin(genesisSineTime) * 0.5f + 0.5f;
                float g = (float) Math.sin(genesisSineTime + 2.0f) * 0.5f + 0.5f;
                float b = (float) Math.sin(genesisSineTime + 4.0f) * 0.5f + 0.5f;
                font.setColor(r, g, b, 1);
            }
        } else {
            // For all other tiers, use the pre-defined color
            font.setColor(tier.getColor());
        }
    }

    private String formatBonus(int value) {
        return (value > 0 ? "+" : "") + value;
    }

    private String formatPercentBonus(float value) {
        int percentValue = (int)(value * 100);
        return "+" + percentValue + "%";
    }

    private void drawCategoryNavigation(float screenWidth, float screenHeight) {
        // Draw category tabs with more spacing
        float tabWidth = 170;
        float spacing = 40;
        float startX = (screenWidth - (tabWidth * MenuConfig.CATEGORIES.length + spacing * (MenuConfig.CATEGORIES.length - 1))) / 2;

        for (int i = 0; i < MenuConfig.CATEGORIES.length; i++) {
            float x = startX + i * (tabWidth + spacing);

            // Draw category name
            if (i == selectedCategory) {
                font.setColor(DisplayConfig.SELECTED_CATEGORY_COLOR);
                font.draw(batch, "< " + MenuConfig.CATEGORIES[i] + " >", x, screenHeight - DisplayConfig.CATEGORY_Y);
            } else {
                font.setColor(DisplayConfig.CATEGORY_COLOR);
                font.draw(batch, MenuConfig.CATEGORIES[i], x + 20, screenHeight - DisplayConfig.CATEGORY_Y);
            }
        }
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
        selectSound.dispose();
        buySound.dispose();
        errorSound.dispose();
        shapeRenderer.dispose();
    }

    @Override
    public void hide() {
        // No need to do anything special on hide
    }

    @Override
    public void show() {
        // Make sure the shared music is playing
        swu.cp112.silkblade.core.Main.resumeBackgroundMusic();
    }

    // Unused Screen interface methods
    @Override public void pause() {}
    @Override public void resume() {}
}
