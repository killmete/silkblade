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
            "WEAPON_SOFT_LAMPHUN_SILK",
            "Soft Lamphun Silk",
            "A delicate blade made from tightly-woven Lamphun silk.\nThough light, it cuts with surprising sharpness.",
            Equipment.EquipmentType.WEAPON)
            .withTier(ItemTier.NORMAL)
            .withAttackBonus(2)
        );
        registerEquipment(new Equipment(
            "WEAPON_ANCIENT_PATTERN_BLADE",
            "Ancient Pattern Blade",
            "An aged blade etched with ancient Lanna patterns.\nIts mystic design enhances the wielder's instincts.",
            Equipment.EquipmentType.WEAPON)
            .withTier(ItemTier.RARE)
            .withAttackBonus(4)
            .withCritRateBonus(0.05f)
        );
        registerEquipment(new Equipment(
            "WEAPON_TWIN_SILK_BLADES",
            "Twin Silk Blades",
            "Paired daggers crafted from enchanted silk steel.\nThey dance like wind, striking twice in a blink.",
            Equipment.EquipmentType.WEAPON)
            .withTier(ItemTier.HEROIC)
            .withAttackBonus(6)
            .withDoubleAttack(true)
        );
        registerEquipment(new Equipment(
            "WEAPON_ENCHANTED_SILK_SABER",
            "Enchanted Silk Saber",
            "A saber wrapped in protective silk runes.\nIt responds to the user's will, striking with uncanny precision.",
            Equipment.EquipmentType.WEAPON)
            .withTier(ItemTier.LEGENDARY)
            .withAttackBonus(12)
            .withCritRateBonus(0.15f)
        );
        registerEquipment(new Equipment(
            "WEAPON_SILKBLADE_OF_JUDGEMENT",
            "Silkblade of Judgment",
            "Forged for warriors of justice, this legendary blade slices with the speed of judgment and the fury of silk.",
            Equipment.EquipmentType.WEAPON)
            .withTier(ItemTier.GENESIS)
            .withAttackBonus(35)
            .withCritRateBonus(0.20f)
            .withDoubleAttack(true)
        );
        
        // END Tier weapon
        registerEquipment(new Equipment(
            "WEAPON_ETERNAL_SILKBLADE",
            "Eternal Silkblade",
            "A transcendent weapon that exists beyond time itself. Its blade cuts through reality, leaving no enemy standing.",
            Equipment.EquipmentType.WEAPON)
            .withTier(ItemTier.END)
            .withAttackBonus(80)
            .withCritRateBonus(0.30f)
            .withDoubleAttack(true)
            .withAttackPercentBonus(0.25f) // 25% attack bonus
        );


        // Armors
        registerEquipment(new Equipment(
            "ARMOR_LAMPHUN_SHIRT",
            "Traditional Lamphun Shirt",
            "A local shirt blessed with protective threads. Keeps you safer than it looks.",
            Equipment.EquipmentType.ARMOR)
            .withTier(ItemTier.NORMAL)
            .withDefenseBonus(5)
            .withMaxHPBonus(15)
        );
        registerEquipment(new Equipment(
            "ARMOR_SILK_GARMENT",
            "Reinforced Silk Garment",
            "Stylish yet sturdy, this reinforced silk clothing is favored by traveling monks and warriors alike.",
            Equipment.EquipmentType.ARMOR)
            .withTier(ItemTier.RARE)
            .withDefenseBonus(12)
            .withMaxHPBonus(30)
        );
        registerEquipment(new Equipment(
            "ARMOR_SILK_ROBE",
            "Mystic Silk Robe",
            "Woven with ancient chants, this robe absorbs part of the damage aimed at the wearer.",
            Equipment.EquipmentType.ARMOR)
            .withTier(ItemTier.HEROIC)
            .withDefenseBonus(20)
            .withMaxHPBonus(60)
            .withThornDamage(0.8f)
        );
        registerEquipment(new Equipment(
            "ARMOR_SILK_ARMOR",
            "Divine Silk Armor",
            "Armor worn by silk guardians. It's said each thread is blessed by the mountain spirits.",
            Equipment.EquipmentType.ARMOR)
            .withTier(ItemTier.LEGENDARY)
            .withDefenseBonus(35)
            .withMaxHPBonus(100)
            .withDefensePercentBonus(0.1f)
        );
        registerEquipment(new Equipment(
            "ARMOR_SILK_AEGIS",
            "Judgmental Silk Aegis",
            "A sacred silk armor that reflects harm back at foes who dare strike its bearer.",
            Equipment.EquipmentType.ARMOR)
            .withTier(ItemTier.GENESIS)
            .withDefenseBonus(50)
            .withMaxHPBonus(180)
            .withThornDamage(0.5f)
            .withDefensePercentBonus(0.15f)
        );
        
        // END Tier armor
        registerEquipment(new Equipment(
            "ARMOR_COSMIC_SILK_VESTMENT",
            "Cosmic Silk Vestment",
            "Fashioned from silk threads that have touched the stars. This vestment grants its wearer unparalleled protection.",
            Equipment.EquipmentType.ARMOR)
            .withTier(ItemTier.END)
            .withDefenseBonus(75)
            .withMaxHPBonus(300)
            .withThornDamage(0.8f)
            .withDefensePercentBonus(0.3f)
        );


        // Accessories
        registerEquipment(new Equipment(
            "ACCESSORY_SPIRIT_CHARM",
            "Spirit Charm",
            "A small charm with a faint glow. Keeps your mind and spirit energized.",
            Equipment.EquipmentType.ACCESSORY)
            .withTier(ItemTier.NORMAL)
            .withMaxMPBonus(10)
            .withDefenseBonus(2)
        );
        registerEquipment(new Equipment(
            "ACCESSORY_SILK_RING",
            "Woven Silk Ring",
            "Delicately crafted with silk threads. Boosts magical clarity and luck in battle.",
            Equipment.EquipmentType.ACCESSORY)
            .withTier(ItemTier.RARE)
            .withCritRateBonus(0.05f)
            .withMaxMPPercentBonus(0.05f) //5%
            .withDefenseBonus(5)
        );
        registerEquipment(new Equipment(
            "ACCESSORY_SILK_BRACELET",
            "Sacred Silk Bracelet",
            "Infused with sacred blessings, this bracelet protects both body and soul.",
            Equipment.EquipmentType.ACCESSORY)
            .withTier(ItemTier.HEROIC)
            .withMaxHPBonus(15)
            .withMaxMPBonus(20)
            .withDefenseBonus(10)
        );
        registerEquipment(new Equipment(
            "ACCESSORY_SILK_HEADBAND",
            "Spiritwoven Headband",
            "This enchanted headband channels energy to the mind, allowing for a single free spell cast each battle.",
            Equipment.EquipmentType.ACCESSORY)
            .withTier(ItemTier.LEGENDARY)
            .withMaxMPBonus(40)
            .withFreeSkillCast(true)
            .withDefenseBonus(15)
            .withDefensePercentBonus(0.05f)
        );
        registerEquipment(new Equipment(
            "ACCESSORY_SILK_TALISMAN",
            "Ancient Silk Talisman",
            "A relic of ancient monks. Its power aids healing and defies death once.",
            Equipment.EquipmentType.ACCESSORY)
            .withTier(ItemTier.GENESIS)
            .withMaxHPPercentBonus(0.20f) //20%
            .withMaxMPPercentBonus(0.10f) //10%
            .withDeathDefiance(true)
            .withDefenseBonus(25)
            .withDefensePercentBonus(0.10f)
        );
        
        // END Tier accessory
        registerEquipment(new Equipment(
            "ACCESSORY_TRANSCENDENT_SILK_CROWN",
            "Transcendent Silk Crown",
            "An ethereal crown woven from silk that transcends dimensions. Bestows godlike powers upon its wearer.",
            Equipment.EquipmentType.ACCESSORY)
            .withTier(ItemTier.END)
            .withMaxHPPercentBonus(0.35f) // 35%
            .withMaxMPPercentBonus(0.35f) // 35%
            .withDeathDefiance(true)
            .withFreeSkillCast(true)
            .withCritRateBonus(0.15f)
            .withDefenseBonus(40)
            .withDefensePercentBonus(0.15f)
        );
    }
    /**
     * Initialize all consumable items in the game
     */
    private void initializeConsumables() {
        registerConsumable(new ConsumableItem(
            "CONSUMABLE_HERBAL_DRINK",
            "Lamphun Herbal Drink",
            "A common restorative tonic brewed from local herbs. Restores vitality with a sweet tang.",
            ConsumableItem.ItemEffect.HEAL_HP,
            30
        ).withTier(ItemTier.NORMAL));

        registerConsumable(new ConsumableItem(
            "CONSUMABLE_MANA_POTION",
            "Lamphun Redbull",
            "A spiritual draught that restores mental energy.",
            ConsumableItem.ItemEffect.RESTORE_MP,
            10
        ).withTier(ItemTier.NORMAL));

        registerConsumable(new ConsumableItem(
            "CONSUMABLE_LONGAN_SNACK",
            "Dried Longan Snack",
            "A chewy, energizing treat made from sun-dried longan. A favorite among adventurers.",
            ConsumableItem.ItemEffect.HEAL_HP,
            60
        ).withTier(ItemTier.RARE));

        registerConsumable(new ConsumableItem(
            "CONSUMABLE_DOI_TI_HONEY",
            "Doi Ti Honey",
            "Pure honey from the Doi Ti highlands.\nRestores health and gently soothes magical fatigue.",
            ConsumableItem.ItemEffect.HEAL_HP,
            100
        ).withTier(ItemTier.HEROIC)
        .withSecondaryEffect(ConsumableItem.ItemEffect.RESTORE_MP, 20));

        registerConsumable(new ConsumableItem(
            "CONSUMABLE_ANCIENT_ELIXIR",
            "Ancient Lanna Elixir",
            "A mythical elixir said to have been crafted by Lanna alchemists. Revives the spirit and sharpens focus.",
            ConsumableItem.ItemEffect.FULL_HEAL,
            0
        ).withTier(ItemTier.LEGENDARY)
        .withSecondaryEffect(ConsumableItem.ItemEffect.RESTORE_MP, 50));

        registerConsumable(new ConsumableItem(
            "CONSUMABLE_DRAGON_SILK",
            "Dragon Silk Remedy",
            "This divine concoction, wrapped in silk, restores all and empowers the soul with dragon-like strength.",
            ConsumableItem.ItemEffect.FULL_RESTORE,
            0
        ).withTier(ItemTier.GENESIS)
        .withBuff(2, 15, 15)); // 2 turns, +15 ATK, +15 DEF
        
        // END Tier consumable
        registerConsumable(new ConsumableItem(
            "CONSUMABLE_ETERNAL_SILK_ELIXIR",
            "Eternal Silk Elixir",
            "The ultimate elixir, distilled from timeless silk essence. Completely restores the body and spirit while granting overwhelming power.",
            ConsumableItem.ItemEffect.FULL_RESTORE,
            0
        ).withTier(ItemTier.END)
        .withBuff(3, 30, 30)); // 3 turns, +30 ATK, +30 DEF
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
