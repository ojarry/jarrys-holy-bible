package net.jarry.holybible.client;

import net.jarry.holybible.HolyBible;
import net.jarry.holybible.data.BibleText.Testament;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;

/**
 * Dev-only client smoke test (-Djarrys_holy_bible.clientselftest=true): opens the reader from the
 * title screen, walks Testament, Book, Chapter and the verse view, checks scrolling and the
 * Malachi to Matthew chapter step, holding each view for a few seconds, then quits the game.
 */
final class ClientSelfTest {
	private static final int START = 100, HOLD = 80;
	private static int ticks;
	private static boolean ok = true;
	private static BibleScreenBase screen;

	private ClientSelfTest() { }

	static void register() {
		TickEvent.ClientTickEvent.Post.BUS.addListener(ClientSelfTest::tick);
	}

	private static void check(boolean cond, String what) {
		if (!cond) ok = false;
		HolyBible.LOG.info("CLIENTTEST {} {} | {}", cond ? "ok  " : "FAIL", what, screen == null ? "-" : screen.testState());
	}

	private static void tick(TickEvent.ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return; // wait for the quick-play world
		ticks++;
		if (ticks < START || (ticks - START) % HOLD != 0) return;
		int step = (ticks - START) / HOLD;
		try {
			switch (step) {
				case 0 -> {
					var inv = mc.player.getInventory();
					int bibles = 0, slot = -1;
					for (int i = 0; i < inv.getContainerSize(); i++) {
						if (inv.getItem(i).is(HolyBible.bible())) { bibles += inv.getItem(i).getCount(); if (slot < 0) slot = i; }
					}
					check(bibles == 1, "player holds exactly one gifted Bible (found " + bibles + ", slot " + slot + ")");
					// Use the item through the normal Item.use path, as a right-click would.
					net.minecraft.world.item.ItemStack stack = slot >= 0 ? inv.getItem(slot) : new net.minecraft.world.item.ItemStack(HolyBible.bible());
					var result = stack.use(mc.level, mc.player, net.minecraft.world.InteractionHand.MAIN_HAND);
					HolyBible.LOG.info("CLIENTTEST use() -> {}", result);
				}
				case 1 -> { screen = BibleScreenBase.lastOpened; check(screen != null, "right-click opened the reader"); screen.testHome(); check(screen.testState().startsWith("HOME"), "home"); }
				case 2 -> { screen.testBooks(Testament.OLD); check(screen.testState().startsWith("BOOKS"), "old testament books"); }
				case 3 -> { screen.testBooks(Testament.NEW); check(screen.testState().startsWith("BOOKS"), "new testament books"); }
				case 4 -> { screen.testChapters(18); check(screen.testState().startsWith("CHAPTERS Psalms"), "psalms chapter grid"); }
				case 5 -> { screen.testRead(0, 1); check(screen.testLines() >= 31, "genesis 1 verses"); }
				case 6 -> { screen.testRead(18, 119); int before = screen.testLines(); screen.testScroll(25); check(before > 176 && screen.testScrollPos() == 25, "psalm 119 scroll"); }
				case 7 -> { screen.testRead(38, 4); screen.testStep(1); check(screen.testBookName().equals("Matthew") && screen.testChapter() == 1, "malachi 4 -> matthew 1"); }
				case 8 -> { screen.testRead(65, 22); screen.testStep(1); check(screen.testBookName().equals("Revelation") && screen.testChapter() == 22, "no step past revelation 22"); }
				case 9 -> {
					HolyBible.LOG.info(ok ? "CLIENTTEST PASS" : "CLIENTTEST FAIL");
					mc.stop();
				}
				default -> { }
			}
		} catch (RuntimeException e) {
			ok = false;
			HolyBible.LOG.error("CLIENTTEST FAIL step {}", step, e);
		}
	}
}
