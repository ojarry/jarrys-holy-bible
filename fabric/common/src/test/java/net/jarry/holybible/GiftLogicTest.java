package net.jarry.holybible;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import net.jarry.holybible.gift.GiftLogic;
import net.jarry.holybible.gift.GiftLogic.Outcome;
import org.junit.jupiter.api.Test;

class GiftLogicTest {
	/** A fake player: a persistent flag, an inventory with N free slots, and a ground pile. */
	static class Fake implements GiftLogic.Target {
		boolean flag; int freeSlots; int inInventory; int onGround; final List<String> chat = new ArrayList<>();
		Fake(int freeSlots) { this.freeSlots = freeSlots; }
		@Override public boolean alreadyGifted() { return flag; }
		@Override public void markGifted() { flag = true; }
		@Override public boolean insertBible() { if (freeSlots == 0) return false; freeSlots--; inInventory++; return true; }
		@Override public void dropBible() { onGround++; }
		@Override public void tell(String m) { chat.add(m); }
	}

	@Test void firstJoinGivesOne() {
		Fake p = new Fake(36);
		assertEquals(Outcome.INSERTED, GiftLogic.give(p));
		assertEquals(1, p.inInventory); assertEquals(0, p.onGround); assertTrue(p.flag);
		assertEquals(List.of(GiftLogic.MSG_INSERTED), p.chat);
	}

	@Test void fullInventoryDropsAtFeet() {
		Fake p = new Fake(0);
		assertEquals(Outcome.DROPPED, GiftLogic.give(p));
		assertEquals(0, p.inInventory); assertEquals(1, p.onGround); assertTrue(p.flag);
		assertEquals(List.of(GiftLogic.MSG_DROPPED), p.chat);
	}

	@Test void rejoinAndDeathNeverDuplicate() {
		Fake p = new Fake(36);
		GiftLogic.give(p);
		p.inInventory = 0; // died and lost it; the flag is copied on death
		for (int i = 0; i < 5; i++) assertEquals(Outcome.ALREADY_GIFTED, GiftLogic.give(p));
		assertEquals(0, p.inInventory); assertEquals(0, p.onGround); assertEquals(1, p.chat.size());
	}

	@Test void flagIsSetBeforeHandingOver() {
		Fake p = new Fake(36) {
			@Override public boolean insertBible() { assertTrue(flag, "flag must be set first"); return super.insertBible(); }
		};
		GiftLogic.give(p);
	}
}
