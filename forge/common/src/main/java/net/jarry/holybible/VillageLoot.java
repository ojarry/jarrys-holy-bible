package net.jarry.holybible;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

/** The 16 vanilla village chest tables that receive a 0.1% Holy Bible pool via a Forge global loot modifier. */
public final class VillageLoot {
	public static final float CHANCE = 0.001f;

	/** The 16 vanilla minecraft:chests/village/* chest tables. */
	public static final List<ResourceKey<LootTable>> TABLES = List.of(
		BuiltInLootTables.VILLAGE_PLAINS_HOUSE, BuiltInLootTables.VILLAGE_DESERT_HOUSE,
		BuiltInLootTables.VILLAGE_SAVANNA_HOUSE, BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
		BuiltInLootTables.VILLAGE_TAIGA_HOUSE,
		BuiltInLootTables.VILLAGE_WEAPONSMITH, BuiltInLootTables.VILLAGE_TOOLSMITH,
		BuiltInLootTables.VILLAGE_ARMORER, BuiltInLootTables.VILLAGE_CARTOGRAPHER,
		BuiltInLootTables.VILLAGE_MASON, BuiltInLootTables.VILLAGE_SHEPHERD,
		BuiltInLootTables.VILLAGE_BUTCHER, BuiltInLootTables.VILLAGE_FLETCHER,
		BuiltInLootTables.VILLAGE_FISHER, BuiltInLootTables.VILLAGE_TANNERY,
		BuiltInLootTables.VILLAGE_TEMPLE);

	private VillageLoot() { }
}
