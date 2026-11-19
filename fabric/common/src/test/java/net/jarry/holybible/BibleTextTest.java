package net.jarry.holybible;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.jarry.holybible.data.BibleText;
import net.jarry.holybible.data.BibleText.Testament;
import org.junit.jupiter.api.Test;

class BibleTextTest {
	private final BibleText text = BibleText.get();

	@Test void wholeCanonIsPresent() {
		assertEquals(66, text.books().size());
		assertEquals(39, text.books(Testament.OLD).size());
		assertEquals(27, text.books(Testament.NEW).size());
		assertEquals(31102, text.totalVerses());
		int chapters = 0, verses = 0;
		for (BibleText.Book b : text.books()) {
			chapters += b.chapters();
			for (int c = 1; c <= b.chapters(); c++) {
				List<String> v = text.chapter(b, c);
				assertEquals(b.verseCounts()[c - 1], v.size(), b.name() + " " + c);
				for (String s : v) assertFalse(s.isBlank(), b.name() + " " + c);
				verses += v.size();
			}
		}
		assertEquals(1189, chapters);
		assertEquals(31102, verses);
	}

	@Test void knownVerses() {
		BibleText.Book gen = text.books().get(0), john = text.books().get(42), rev = text.books().get(65);
		assertEquals("Genesis", gen.name());
		assertEquals("In the beginning God created the heaven and the earth.", text.chapter(gen, 1).get(0));
		assertEquals("John", john.name());
		assertTrue(text.chapter(john, 3).get(15).startsWith("For God so loved the world"));
		assertEquals("Revelation", rev.name());
		assertEquals(22, rev.chapters());
		assertEquals(150, text.books().get(18).chapters());
		assertEquals(176, text.chapter(text.books().get(18), 119).size());
	}

	@Test void loadsLazilyPerBook() {
		AtomicInteger opens = new AtomicInteger();
		BibleText lazy = new BibleText(p -> { opens.incrementAndGet(); return BibleText.class.getResourceAsStream(p); });
		assertEquals(1, opens.get(), "only the index is read up front");
		BibleText.Book ruth = lazy.books().get(7);
		assertFalse(lazy.isCached(ruth));
		lazy.chapter(ruth, 1);
		lazy.chapter(ruth, 4);
		assertEquals(2, opens.get(), "one file per book, cached after first read");
		assertTrue(lazy.isCached(ruth));
		for (int i = 0; i < 6; i++) lazy.chapter(lazy.books().get(i), 1);
		assertFalse(lazy.isCached(ruth), "cache is bounded");
	}

	@Test void badChapterIsRejected() {
		BibleText.Book jude = text.books().get(64);
		assertThrows(IndexOutOfBoundsException.class, () -> text.chapter(jude, 2));
		assertThrows(IndexOutOfBoundsException.class, () -> text.chapter(jude, 0));
	}
}
