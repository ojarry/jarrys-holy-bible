package net.jarry.holybible;

import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

/** Adds one extra pool (1 roll, 0.1% chance, 1 Bible) to every vanilla village chest table. */
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

	private static final Set<ResourceKey<LootTable>> TABLE_SET = Set.copyOf(TABLES);

	private VillageLoot() { }

	static void register() {
		LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
			// Matched by id, so a data pack or mod that replaces a village table still gets the pool.
			if (!TABLE_SET.contains(key)) return;
			// A new pool rolls once by default.
			builder.withPool(LootPool.lootPool()
				.when(LootItemRandomChanceCondition.randomChance(CHANCE))
				.add(LootItem.lootTableItem(HolyBible.bible())));
		});
	}
}
