package net.jarry.holybible.client;

import net.fabricmc.api.ClientModInitializer;
import net.jarry.holybible.BibleItem;
import net.jarry.holybible.client.compat.ClientPlatform;

/** Client entrypoint: hooks the item's right-click to the reader screen. */
public final class HolyBibleClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BibleItem.setReaderOpener(() -> ClientPlatform.openScreen(ClientPlatform.newReader()));
		if (Boolean.getBoolean("jarrys_holy_bible.clientselftest")) ClientSelfTest.register();
	}
}
