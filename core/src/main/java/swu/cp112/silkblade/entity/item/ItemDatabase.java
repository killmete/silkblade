package swu.cp112.silkblade.entity.item;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import swu.cp112.silkblade.util.GameLogger;

/**
 * Central database of all available items in the game.
 * Items are defined as code instead of in JSON to make it harder to cheat.
 */
public class ItemDatabase {
    // Singleton instance
    private static ItemDatabase instance;

    // Maps to store items by ID
    private final ObjectMap<String, Equipment> equipmentDatabase = new ObjectMap<>();
    private final ObjectMap<String, ConsumableItem> consumableDatabase = new ObjectMap<>();

    // Private constructor (singleton)
    private ItemDatabase() {
        initializeEquipment();
        initializeConsumables();
        
        // Add tier example items
        initializeTierExamples();
    }

    /**
     * Get the singleton instance
     */
    public static ItemDatabase getInstance() {
        if (instance == null) {
            instance = new ItemDatabase();
        }
        return instance;
    }

    /**
     * Initialize all equipment items in the game
     */
    private void initializeEquipment() {
        // Weapons
        registerEquipment(new Equipment(
            "WEAPON_REAL_KNIFE",
            "Real Knife",
            "A worn dagger. For cutting plants... or enemies.",
            Equipment.EquipmentType.WEAPON)
            .withAttackBonus(10)
            .withCritRateBonus(0.05f)
        );

        registerEquipment(new Equipment(
            "WEAPON_WORN_BLADE",
            "Worn Blade",
            "A simple sword, somewhat dulled with use.",
            Equipment.EquipmentType.WEAPON)
            .withAttackBonus(5)
        );

        // Armor
        registerEquipment(new Equipment(
            "ARMOR_SILK_ROBE",
            "Silk Robe",
            "A light robe made of fine silk.Offers minimal protection.",
            Equipment.EquipmentType.ARMOR)
            .withDefenseBonus(3)
            .withMaxMPBonus(5)
        );

        // Accessories
        registerEquipment(new Equipment(
            "ACCESSORY_HEART_LOCKET",
            "Heart Locket",
            "A golden locket in the shape of a heart. \"Best Friends Forever\"",
            Equipment.EquipmentType.ACCESSORY)
            .withMaxHPBonus(15)
            .withDefenseBonus(5)
        );
    }

    /**
     * Initialize all consumable items in the game
     */
    private void initializeConsumables() {
        registerConsumable(new ConsumableItem(
            "CONSUMABLE_ELIXIR",
            "Elixir",
            "Restores 30 HP",
            ConsumableItem.ItemEffect.HEAL_HP,
            30
        ));

        registerConsumable(new ConsumableItem(
            "CONSUMABLE_MANA_POTION",
            "Mana Potion",
            "Restores 15 MP",
            ConsumableItem.ItemEffect.RESTORE_MP,
            15
        ));

        registerConsumable(new ConsumableItem(
            "CONSUMABLE_STRENGTH_POTION",
            "Strength Potion",
            "Temporarily increases attack power",
            ConsumableItem.ItemEffect.BUFF_ATK,
            10
        ));
    }
    
    /**
     * Initialize example items for each tier for testing purposes
     */
    private void initializeTierExamples() {
        // NORMAL tier equipment (already defined in base equipment)
        
        // RARE tier equipment
        registerEquipment(new Equipment(
            "TIER_RARE_WEAPON",
            "Azure Blade",
            "A sword with a blue glowing edge. Rare quality.",
            Equipment.EquipmentType.WEAPON)
            .withTier(ItemTier.RARE)
            .withAttackBonus(20)
            .withCritRateBonus(0.07f)
        );
        
        registerEquipment(new Equipment(
            "TIER_RARE_ARMOR",
            "Cobalt Mail",
            "Armor forged from rare cobalt ore. Provides enhanced protection.",
            Equipment.EquipmentType.ARMOR)
            .withTier(ItemTier.RARE)
            .withDefenseBonus(15)
            .withMaxHPBonus(10)
        );
        
        // HEROIC tier equipment
        registerEquipment(new Equipment(
            "TIER_HEROIC_WEAPON",
            "Amethyst Saber",
            "A blade fashioned from enchanted purple crystal. Heroic quality.",
            Equipment.EquipmentType.WEAPON)
            .withTier(ItemTier.HEROIC)
            .withAttackBonus(35)
            .withCritRateBonus(0.10f)
            .withMaxMPBonus(15)
        );
        
        registerEquipment(new Equipment(
            "TIER_HEROIC_ACCESSORY",
            "Violet Pendant",
            "A pendant that pulses with heroic energy. Enhances magical abilities.",
            Equipment.EquipmentType.ACCESSORY)
            .withTier(ItemTier.HEROIC)
            .withMaxHPBonus(25)
            .withMaxMPBonus(25)
            .withDefenseBonus(10)
        );
        
        // LEGENDARY tier equipment
        registerEquipment(new Equipment(
            "TIER_LEGENDARY_WEAPON",
            "Excalibur",
            "The legendary sword of kings. Its golden blade cuts through anything.",
            Equipment.EquipmentType.WEAPON)
            .withTier(ItemTier.LEGENDARY)
            .withAttackBonus(50)
            .withCritRateBonus(0.15f)
            .withMaxHPBonus(20)
        );
        
        registerEquipment(new Equipment(
            "TIER_LEGENDARY_ARMOR",
            "Divine Plate",
            "Golden armor blessed by the gods.\nLegendary quality.",
            Equipment.EquipmentType.ARMOR)
            .withTier(ItemTier.LEGENDARY)
            .withDefenseBonus(35)
            .withMaxHPBonus(50)
            .withMaxMPBonus(20)
        );
        
        // GENESIS tier equipment
        registerEquipment(new Equipment(
            "TIER_GENESIS_ACCESSORY",
            "Ethereal Crown",
            "A crown that transcends reality itself. The ultimate accessory.",
            Equipment.EquipmentType.ACCESSORY)
            .withTier(ItemTier.GENESIS)
            .withAttackBonus(30)
            .withDefenseBonus(30)
            .withMaxHPBonus(100)
            .withMaxMPBonus(100)
            .withCritRateBonus(0.20f)
        );
        
        // Example consumables of different tiers
        registerConsumable(new ConsumableItem(
            "TIER_RARE_POTION",
            "Greater Healing Potion",
            "A rare potion that restores 60 HP.",
            ConsumableItem.ItemEffect.HEAL_HP,
            60
        ).withTier(ItemTier.RARE));
        
        registerConsumable(new ConsumableItem(
            "TIER_HEROIC_POTION",
            "Royal Elixir",
            "A heroic potion that restores 120 HP.",
            ConsumableItem.ItemEffect.HEAL_HP,
            120
        ).withTier(ItemTier.HEROIC));
        
        registerConsumable(new ConsumableItem(
            "TIER_LEGENDARY_POTION",
            "Golden Nectar",
            "A legendary potion that restores 50 MP.",
            ConsumableItem.ItemEffect.RESTORE_MP,
            50
        ).withTier(ItemTier.LEGENDARY));
        
        registerConsumable(new ConsumableItem(
            "TIER_GENESIS_POTION",
            "Essence of Creation",
            "The rarest potion in existence. Restores 200 HP and adds temporary buffs.",
            ConsumableItem.ItemEffect.HEAL_HP,
            200
        ).withTier(ItemTier.GENESIS));
    }

    /**
     * Register an equipment item in the database
     */
    private void registerEquipment(Equipment equipment) {
        equipmentDatabase.put(equipment.getId(), equipment);
        GameLogger.logInfo("Registered equipment: " + equipment.getId());
    }

    /**
     * Register a consumable item in the database
     */
    private void registerConsumable(ConsumableItem consumable) {
        consumableDatabase.put(consumable.getId(), consumable);
        GameLogger.logInfo("Registered consumable: " + consumable.getId());
    }

    /**
     * Get an equipment item by its ID
     */
    public Equipment getEquipmentById(String id) {
        Equipment equipment = equipmentDatabase.get(id);
        if (equipment == null) {
            return null;
        }
        // Return a clone to prevent modifying the original
        return equipment.clone();
    }

    /**
     * Get a consumable item by its ID
     */
    public ConsumableItem getConsumableById(String id) {
        ConsumableItem consumable = consumableDatabase.get(id);
        if (consumable == null) {
            return null;
        }
        // Return a clone to prevent modifying the original
        return consumable.clone();
    }

    /**
     * Get all available equipment items
     */
    public Array<Equipment> getAllEquipment() {
        Array<Equipment> result = new Array<>(equipmentDatabase.size);
        for (Equipment equipment : equipmentDatabase.values()) {
            result.add(equipment.clone());
        }
        return result;
    }

    /**
     * Get all available consumable items
     */
    public Array<ConsumableItem> getAllConsumables() {
        Array<ConsumableItem> result = new Array<>(consumableDatabase.size);
        for (ConsumableItem consumable : consumableDatabase.values()) {
            result.add(consumable.clone());
        }
        return result;
    }
}
