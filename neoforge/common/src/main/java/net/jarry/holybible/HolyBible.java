package net.jarry.holybible;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.jarry.holybible.compat.Platform;
import net.jarry.holybible.gift.GiftHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common (both sides) entrypoint: item, creative tab, village loot and the first-join gift. */
@Mod(HolyBible.MOD_ID)
public final class HolyBible {
	public static final String MOD_ID = "jarrys_holy_bible";
	public static final Logger LOG = LoggerFactory.getLogger("Jarry's Holy Bible");

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
	public static final DeferredItem<Item> BIBLE = ITEMS.registerItem("bible", BibleItem::new, p -> p.stacksTo(1));

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
		DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

	/** Persistent per-player flag, copied on death, so the first-join Bible is never handed out twice. */
	public static final Supplier<AttachmentType<Boolean>> GIFTED = ATTACHMENTS.register("gifted",
		() -> AttachmentType.builder(() -> Boolean.FALSE)
			.serialize(Codec.BOOL.fieldOf("gifted"))
			.copyOnDeath()
			.build());

	public static Item bible() {
		return BIBLE.get();
	}

	public HolyBible(IEventBus modBus) {
		ITEMS.register(modBus);
		ATTACHMENTS.register(modBus);
		modBus.addListener(this::addCreative);
		GiftHandler.register();
		if (Boolean.getBoolean("jarrys_holy_bible.selftest")) SelfTest.register();
		LOG.info("Jarry's Holy Bible loaded (KJV, {} on {})", Platform.MC_VERSION, "NeoForge");
	}

	private void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(BIBLE);
		}
	}
}
