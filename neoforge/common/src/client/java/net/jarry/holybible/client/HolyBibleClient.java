package net.jarry.holybible.client;

import net.jarry.holybible.BibleItem;
import net.jarry.holybible.HolyBible;
import net.jarry.holybible.client.compat.ClientPlatform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Client entrypoint: hooks the item's right-click to the reader screen. */
@Mod(value = HolyBible.MOD_ID, dist = Dist.CLIENT)
public final class HolyBibleClient {
	public HolyBibleClient() {
		BibleItem.setReaderOpener(() -> ClientPlatform.openScreen(ClientPlatform.newReader()));
		if (Boolean.getBoolean("jarrys_holy_bible.clientselftest")) ClientSelfTest.register();
	}
}
