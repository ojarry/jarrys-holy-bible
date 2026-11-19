package net.jarry.holybible;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.jarry.holybible.compat.Platform;
import net.jarry.holybible.gift.GiftHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * Dev-only smoke test (enabled with -Djarrys_holy_bible.selftest=true): after a dedicated server
 * starts, rolls every village chest table many times, checks the Bible shows up at about 0.1%,
 * then stops the server. Never runs in a normal game.
 */
final class SelfTest {
	private static final int ROLLS_PER_TABLE = 20_000;

	private SelfTest() { }

	static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(SelfTest::run);
	}

	private static void run(MinecraftServer server) {
		boolean ok = true;
		int total = 0;
		java.util.SplittableRandom seeds = new java.util.SplittableRandom(0x5eedL);
		StringBuilder report = new StringBuilder();
		for (ResourceKey<LootTable> key : VillageLoot.TABLES) {
			LootTable table = server.reloadableRegistries().getLootTable(key);
			LootParams params = new LootParams.Builder(server.overworld())
				.withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
				.create(LootContextParamSets.CHEST);
			int found = 0;
			for (int i = 0; i < ROLLS_PER_TABLE; i++) {
				for (ItemStack stack : table.getRandomItems(params, seeds.nextLong())) {
					if (stack.is(HolyBible.bible())) found += stack.getCount();
				}
			}
			total += found;
			report.append(' ').append(Platform.keyId(key)).append('=').append(found);
			if (found == 0) ok = false;
		}
		int rolls = ROLLS_PER_TABLE * VillageLoot.TABLES.size();
		double rate = total / (double) rolls;
		// Expected 320 of 320,000; +-5 sigma is roughly 230..410.
		if (total < 230 || total > 410) ok = false;
		boolean attachment = GiftHandler.GIFTED != null && GiftHandler.GIFTED.isPersistent();
		ok &= attachment;
		boolean recipe = server.getRecipeManager()
			.byKey(ResourceKey.create(Registries.RECIPE, Platform.id("bible"))).isPresent();
		ok &= recipe;
		boolean advancement = server.getAdvancements().getAllAdvancements().stream()
			.anyMatch(a -> a.id().toString().equals("jarrys_holy_bible:recipes/tools/bible"));
		ok &= advancement;
		HolyBible.LOG.info("SELFTEST recipe jarrys_holy_bible:bible loaded={} unlockAdvancement={}", recipe, advancement);
		HolyBible.LOG.info("SELFTEST item={} attachmentPersistent={} rolls={} bibles={} rate={}{}",
			BuiltInRegistries.ITEM.getKey(HolyBible.bible()), attachment, rolls, total, String.format("%.5f", rate), report);
		HolyBible.LOG.info(ok ? "SELFTEST PASS" : "SELFTEST FAIL");
		server.halt(false);
	}
}
