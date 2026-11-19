package net.jarry.holybible.gift;

/** Pure first-join gift decision, kept free of Minecraft types so it can be unit tested. */
public final class GiftLogic {
	public enum Outcome { ALREADY_GIFTED, INSERTED, DROPPED }

	/** What the gift needs from a player. */
	public interface Target {
		boolean alreadyGifted();
		void markGifted();
		/** Try to put one Bible into the inventory; true if it fully fit. */
		boolean insertBible();
		/** Drop one Bible at the player's feet. */
		void dropBible();
		void tell(String message);
	}

	public static final String MSG_INSERTED = "You received a Holy Bible (King James Version). Right-click it to read.";
	public static final String MSG_DROPPED = "You received a Holy Bible (King James Version). Your inventory was full, so it was dropped at your feet.";

	private GiftLogic() { }

	/** The flag is set before the item is handed over, so a crash part-way can never produce a second copy. */
	public static Outcome give(Target target) {
		if (target.alreadyGifted()) return Outcome.ALREADY_GIFTED;
		target.markGifted();
		if (target.insertBible()) {
			target.tell(MSG_INSERTED);
			return Outcome.INSERTED;
		}
		target.dropBible();
		target.tell(MSG_DROPPED);
		return Outcome.DROPPED;
	}
}
