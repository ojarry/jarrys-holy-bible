package net.jarry.holybible.client;

import net.jarry.holybible.data.BibleText;

/** Where the reader was left, kept for the rest of the game session (client memory only). */
final class ReaderState {
	static BibleText.Testament testament;
	static BibleText.Book book;
	static int chapter;
	static int scroll;

	private ReaderState() { }
}
