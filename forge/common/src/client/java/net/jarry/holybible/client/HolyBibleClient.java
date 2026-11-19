package net.jarry.holybible.client;

import net.jarry.holybible.BibleItem;
import net.jarry.holybible.HolyBible;
import net.jarry.holybible.client.compat.ClientPlatform;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client entrypoint: hooks the item's right-click to the reader screen. */
@Mod.EventBusSubscriber(modid = HolyBible.MOD_ID, value = Dist.CLIENT)
public final class HolyBibleClient {
	private HolyBibleClient() { }

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		BibleItem.setReaderOpener(() -> ClientPlatform.openScreen(ClientPlatform.newReader()));
		if (Boolean.getBoolean("jarrys_holy_bible.clientselftest")) ClientSelfTest.register();
	}
}
