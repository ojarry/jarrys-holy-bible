package net.jarry.holybible;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jarry.holybible.compat.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * Rolls one extra loot table and appends its items to the target table's generated loot,
 * without replacing or rewriting the vanilla table. Equivalent to NeoForge's built-in
 * neoforge:add_table; Forge has no built-in add_table, so this is the registered
 * jarrys_holy_bible:add_table codec used by the village-chest modifier JSON.
 */
public final class AddTableLootModifier extends LootModifier {
	public static final MapCodec<AddTableLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
		LootModifier.codecStart(inst)
			.and(ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(m -> m.table))
			.apply(inst, AddTableLootModifier::new));

	private final ResourceKey<LootTable> table;

	public AddTableLootModifier(LootItemCondition[] conditions, ResourceKey<LootTable> table) {
		super(conditions);
		this.table = table;
	}

	@NotNull
	@Override
	protected ObjectArrayList<ItemStack> doApply(LootTable ignored, ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		Platform.rollExtraTable(context, table, generatedLoot);
		return generatedLoot;
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
