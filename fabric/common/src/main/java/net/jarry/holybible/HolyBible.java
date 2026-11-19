package net.jarry.holybible;

import net.fabricmc.api.ModInitializer;
import net.jarry.holybible.compat.Platform;
import net.jarry.holybible.gift.GiftHandler;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common (both sides) entrypoint: item, creative tab, village loot and the first-join gift. */
public final class HolyBible implements ModInitializer {
	public static final String MOD_ID = "jarrys_holy_bible";
	public static final Logger LOG = LoggerFactory.getLogger("Jarry's Holy Bible");

	private static Item bible;

	public static Item bible() {
		if (bible == null) throw new IllegalStateException("Holy Bible item is not registered yet");
		return bible;
	}

	@Override
	public void onInitialize() {
		bible = Platform.registerItem("bible", BibleItem::new);
		Platform.addToCreativeTab(bible);
		VillageLoot.register();
		GiftHandler.register();
		if (Boolean.getBoolean("jarrys_holy_bible.selftest")) SelfTest.register();
		LOG.info("Jarry's Holy Bible loaded (KJV, {} on {})", Platform.MC_VERSION, "Fabric");
	}
}
