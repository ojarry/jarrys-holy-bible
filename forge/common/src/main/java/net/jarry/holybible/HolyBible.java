package net.jarry.holybible;

import com.mojang.serialization.MapCodec;
import net.jarry.holybible.compat.Platform;
import net.jarry.holybible.gift.GiftHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common (both sides) entrypoint: item, creative tab, village loot and the first-join gift. */
@Mod(HolyBible.MOD_ID)
public final class HolyBible {
	public static final String MOD_ID = "jarrys_holy_bible";
	public static final Logger LOG = LoggerFactory.getLogger("Jarry's Holy Bible");

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	public static final RegistryObject<Item> BIBLE = ITEMS.register("bible",
		() -> new BibleItem(new Item.Properties().setId(ITEMS.key("bible")).stacksTo(1)));

	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
		DeferredRegister.create(ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
	public static final RegistryObject<MapCodec<? extends IGlobalLootModifier>> ADD_TABLE =
		LOOT_MODIFIERS.register("add_table", () -> AddTableLootModifier.CODEC);

	public static Item bible() {
		return BIBLE.get();
	}

	public HolyBible(FMLJavaModLoadingContext context) {
		var modBusGroup = context.getModBusGroup();
		ITEMS.register(modBusGroup);
		LOOT_MODIFIERS.register(modBusGroup);
		BuildCreativeModeTabContentsEvent.BUS.addListener(HolyBible::addCreative);
		GiftHandler.register();
		if (Boolean.getBoolean("jarrys_holy_bible.selftest")) SelfTest.register();
		LOG.info("Jarry's Holy Bible loaded (KJV, {} on {})", Platform.MC_VERSION, "Forge");
	}

	private static void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(BIBLE);
		}
	}
}
