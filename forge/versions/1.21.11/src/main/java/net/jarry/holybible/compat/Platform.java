package net.jarry.holybible.compat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jarry.holybible.HolyBible;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

/** Minecraft 1.21.11 Forge bridge for resource ids, persistent NBT and extra loot-table rolls. */
public final class Platform {
	public static final String MC_VERSION = "1.21.11";

	private Platform() { }

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(HolyBible.MOD_ID, path);
	}

	public static String keyId(ResourceKey<?> key) {
		return key.identifier().toString();
	}

	public static boolean nbtGetBoolean(CompoundTag tag, String key) {
		return tag.getBooleanOr(key, false);
	}

	public static void nbtPutBoolean(CompoundTag tag, String key, boolean value) {
		tag.putBoolean(key, value);
	}

	/**
	 * Rolls {@code table} and appends the items to {@code out} without running global loot
	 * modifiers on the subtable (so this modifier cannot apply to itself).
	 */
	public static void rollExtraTable(LootContext context, ResourceKey<LootTable> table, ObjectArrayList<ItemStack> out) {
		var holder = context.getResolver().get(table);
		if (holder.isEmpty()) return;
		holder.get().value().getRandomItemsRaw(context, out::add);
	}
}
