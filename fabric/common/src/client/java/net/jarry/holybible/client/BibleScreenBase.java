package net.jarry.holybible.client;

import java.util.ArrayList;
import java.util.List;
import net.jarry.holybible.HolyBible;
import net.jarry.holybible.data.BibleText;
import net.jarry.holybible.data.BibleText.Book;
import net.jarry.holybible.data.BibleText.Testament;
import net.jarry.holybible.data.TextWrap;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Version-independent reader: Testament, then Book, then Chapter, then the verses with wheel
 * scrolling, page buttons and previous/next chapter. Subclasses only bridge the render calls.
 */
public abstract class BibleScreenBase extends Screen {
	private enum Mode { HOME, BOOKS, CHAPTERS, READ, ERROR }

	private static final int PAPER = 0xF0F3E9D2, EDGE = 0xFF5A3A1E, INK = 0xFF2B1D0E, MUTED = 0xFF7A5C3A, RED = 0xFF8B1A1A;
	private static final int BTN_H = 20, GAP = 4;

	private Mode mode = Mode.HOME;
	private BibleText text;
	private String error;
	private int gridPage;
	private int panelX, panelY, panelW, panelH, bodyTop, bodyBottom;
	private final List<Line> lines = new ArrayList<>();
	private int scroll, visibleLines;
	private net.minecraft.world.item.ItemStack icon;
	static volatile BibleScreenBase lastOpened;

	private record Line(String text, boolean verseStart, int verse) { }

	protected BibleScreenBase() {
		super(Component.literal("Holy Bible"));
	}

	// ---- layout -------------------------------------------------------------------------------

	@Override
	protected void init() {
		lastOpened = this;
		if (text == null && error == null) {
			try {
				text = BibleText.get();
				if (ReaderState.book != null) { mode = Mode.READ; }
			} catch (RuntimeException e) {
				HolyBible.LOG.error("Could not load the Bible text", e);
				error = "The Bible text could not be loaded.";
				mode = Mode.ERROR;
			}
		}
		panelW = Math.min(width - 16, 340);
		panelH = Math.min(height - 16, 250);
		panelX = (width - panelW) / 2;
		panelY = (height - panelH) / 2;
		bodyTop = panelY + 26;
		bodyBottom = panelY + panelH - BTN_H - 10;
		clearWidgets();
		switch (mode) {
			case HOME -> buildHome();
			case BOOKS -> buildBooks();
			case CHAPTERS -> buildChapters();
			case READ -> buildRead();
			case ERROR -> { }
		}
		buildFooter();
	}

	private void go(Mode next) {
		mode = next;
		gridPage = 0;
		rebuildWidgets();
	}

	private Button button(String label, int x, int y, int w, Button.OnPress onPress) {
		return addRenderableWidget(Button.builder(Component.literal(label), onPress).bounds(x, y, w, BTN_H).build());
	}

	private void buildFooter() {
		int y = panelY + panelH - BTN_H - 6;
		int w = 70;
		if (mode != Mode.HOME && mode != Mode.ERROR) {
			button("< Back", panelX + 6, y, w, b -> {
				switch (mode) {
					case READ -> go(Mode.CHAPTERS);
					case CHAPTERS -> go(Mode.BOOKS);
					default -> go(Mode.HOME);
				}
			});
		}
		button("Close", panelX + panelW - w - 6, y, w, b -> onClose());
	}

	private void buildHome() {
		int w = Math.min(200, panelW - 20);
		int x = panelX + (panelW - w) / 2;
		int y = bodyTop + 30;
		button("Old Testament (39 books)", x, y, w, b -> { ReaderState.testament = Testament.OLD; go(Mode.BOOKS); });
		button("New Testament (27 books)", x, y + BTN_H + GAP, w, b -> { ReaderState.testament = Testament.NEW; go(Mode.BOOKS); });
		if (ReaderState.book != null) {
			button("Continue: " + ReaderState.book.name() + " " + ReaderState.chapter, x, y + 2 * (BTN_H + GAP) + 10, w, b -> go(Mode.READ));
		}
	}

	/** Lays out buttons in a grid with paging; returns nothing, adds widgets. */
	private void grid(int count, int cellW, java.util.function.IntFunction<String> label, java.util.function.IntConsumer click) {
		int cols = Math.max(1, (panelW - 12 + GAP) / (cellW + GAP));
		int rows = Math.max(1, (bodyBottom - bodyTop - BTN_H - GAP + GAP) / (BTN_H + GAP));
		int perPage = cols * rows;
		int pages = Math.max(1, (count + perPage - 1) / perPage);
		gridPage = Math.max(0, Math.min(gridPage, pages - 1));
		int gridW = cols * cellW + (cols - 1) * GAP;
		int x0 = panelX + (panelW - gridW) / 2;
		int start = gridPage * perPage;
		for (int i = start; i < Math.min(count, start + perPage); i++) {
			int slot = i - start;
			final int idx = i;
			button(label.apply(i), x0 + (slot % cols) * (cellW + GAP), bodyTop + (slot / cols) * (BTN_H + GAP), cellW, b -> click.accept(idx));
		}
		if (pages > 1) {
			int y = bodyBottom - BTN_H;
			Button prev = button("<", panelX + panelW / 2 - 50, y, 30, b -> { gridPage--; rebuildWidgets(); });
			Button next = button(">", panelX + panelW / 2 + 20, y, 30, b -> { gridPage++; rebuildWidgets(); });
			prev.active = gridPage > 0;
			next.active = gridPage < pages - 1;
		}
	}

	private void buildBooks() {
		List<Book> books = text.books(ReaderState.testament == null ? Testament.OLD : ReaderState.testament);
		int cellW = Math.min(104, (panelW - 12 - 2 * GAP) / 3);
		grid(books.size(), cellW, i -> fit(books.get(i).name(), cellW - 8), i -> {
			ReaderState.book = books.get(i);
			if (ReaderState.book.chapters() == 1) openChapter(1); else go(Mode.CHAPTERS);
		});
	}

	private void buildChapters() {
		Book book = ReaderState.book;
		grid(book.chapters(), 28, i -> Integer.toString(i + 1), i -> openChapter(i + 1));
	}

	private void openChapter(int chapter) {
		ReaderState.chapter = chapter;
		ReaderState.scroll = 0;
		go(Mode.READ);
	}

	private void buildRead() {
		Book book = ReaderState.book;
		lines.clear();
		int textW = panelW - 24;
		try {
			List<String> verses = text.chapter(book, ReaderState.chapter);
			for (int v = 0; v < verses.size(); v++) {
				List<String> wrapped = TextWrap.wrap((v + 1) + "  " + verses.get(v), textW, font::width);
				for (int i = 0; i < wrapped.size(); i++) lines.add(new Line(wrapped.get(i), i == 0, v + 1));
			}
		} catch (RuntimeException e) {
			HolyBible.LOG.error("Could not load {} {}", book.name(), ReaderState.chapter, e);
			lines.add(new Line("This chapter could not be loaded.", false, 0));
		}
		int lineH = font.lineHeight + 2;
		int navY = bodyBottom - BTN_H;
		visibleLines = Math.max(1, (navY - 4 - bodyTop) / lineH);
		scroll = clampScroll(ReaderState.scroll);

		int y = navY;
		Button prev = button("< Prev", panelX + 6, y, 60, b -> stepChapter(-1));
		Button up = button("Page up", panelX + panelW / 2 - 62, y, 60, b -> scrollBy(-(visibleLines - 1)));
		Button down = button("Page down", panelX + panelW / 2 + 2, y, 60, b -> scrollBy(visibleLines - 1));
		Button next = button("Next >", panelX + panelW - 66, y, 60, b -> stepChapter(1));
		prev.active = !(book.index() == 0 && ReaderState.chapter == 1);
		next.active = !(book.index() == text.books().size() - 1 && ReaderState.chapter == book.chapters());
		up.active = lines.size() > visibleLines;
		down.active = lines.size() > visibleLines;
	}

	/** Moves across chapter and book boundaries, Genesis 1 to Revelation 22. */
	private void stepChapter(int dir) {
		Book book = ReaderState.book;
		int ch = ReaderState.chapter + dir;
		if (ch < 1) {
			if (book.index() == 0) return;
			book = text.books().get(book.index() - 1);
			ch = book.chapters();
		} else if (ch > book.chapters()) {
			if (book.index() == text.books().size() - 1) return;
			book = text.books().get(book.index() + 1);
			ch = 1;
		}
		ReaderState.book = book;
		ReaderState.testament = book.testament();
		openChapter(ch);
	}

	private int clampScroll(int s) {
		return Math.max(0, Math.min(s, Math.max(0, lines.size() - visibleLines)));
	}

	private void scrollBy(int delta) {
		scroll = clampScroll(scroll + delta);
		ReaderState.scroll = scroll;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (mode == Mode.READ && scrollY != 0) {
			scrollBy(scrollY > 0 ? -3 : 3);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private String fit(String s, int max) {
		if (font.width(s) <= max) return s;
		while (s.length() > 1 && font.width(s + ".") > max) s = s.substring(0, s.length() - 1);
		return s + ".";
	}

	// ---- dev self-test hooks (only called by ClientSelfTest) ---------------------------------

	String testState() {
		return mode + (ReaderState.book == null ? "" : " " + ReaderState.book.name() + " " + ReaderState.chapter)
			+ " lines=" + lines.size() + " visible=" + visibleLines + " scroll=" + scroll + " widgets=" + children().size();
	}

	void testHome() { go(Mode.HOME); }
	void testBooks(Testament t) { ReaderState.testament = t; go(Mode.BOOKS); }
	void testChapters(int bookIndex) { ReaderState.book = text.books().get(bookIndex); go(Mode.CHAPTERS); }
	void testRead(int bookIndex, int chapter) { ReaderState.book = text.books().get(bookIndex); openChapter(chapter); }
	void testScroll(int delta) { scrollBy(delta); }
	void testStep(int dir) { stepChapter(dir); }
	int testLines() { return lines.size(); }
	int testScrollPos() { return scroll; }
	String testBookName() { return ReaderState.book.name(); }
	int testChapter() { return ReaderState.chapter; }

	// ---- painting (called by the version bridge) -------------------------------------------

	protected final void paintPanel(Painter p) {
		p.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, EDGE);
		p.fill(panelX, panelY, panelX + panelW, panelY + panelH, PAPER);
		p.fill(panelX + 8, panelY + 21, panelX + panelW - 8, panelY + 22, MUTED);
		// Item stacks need a world's registries; the reader only opens in a world, but never crash if not.
		if (minecraft != null && minecraft.level != null) {
			if (icon == null) icon = new net.minecraft.world.item.ItemStack(HolyBible.bible());
			p.item(icon, panelX + 6, panelY + 3);
		}
	}

	protected final void paintText(Painter p) {
		int cx = panelX + panelW / 2;
		switch (mode) {
			case HOME -> {
				p.centered("Holy Bible", cx, panelY + 8, RED);
				p.centered("King James Version", cx, bodyTop + 8, MUTED);
				p.centered(text.totalVerses() + " verses in 66 books", cx, bodyTop + 18, MUTED);
			}
			case BOOKS -> p.centered(ReaderState.testament == Testament.NEW ? "New Testament" : "Old Testament", cx, panelY + 8, RED);
			case CHAPTERS -> p.centered(ReaderState.book.name() + " - choose a chapter", cx, panelY + 8, RED);
			case READ -> {
				p.centered(ReaderState.book.name() + " " + ReaderState.chapter, cx, panelY + 8, RED);
				int lineH = font.lineHeight + 2;
				int y = bodyTop + 2;
				for (int i = scroll; i < Math.min(lines.size(), scroll + visibleLines); i++) {
					Line l = lines.get(i);
					if (l.verseStart() && l.verse() > 0) {
						String num = Integer.toString(l.verse());
						p.text(num, panelX + 12, y, RED);
						p.text(l.text().substring(num.length()), panelX + 12 + font.width(num), y, INK);
					} else {
						p.text(l.text(), panelX + 12, y, INK);
					}
					y += lineH;
				}
				if (lines.size() > visibleLines) {
					int max = lines.size() - visibleLines;
					int trackTop = bodyTop, trackBottom = bodyTop + visibleLines * lineH;
					int thumbH = Math.max(8, (trackBottom - trackTop) * visibleLines / lines.size());
					int thumbY = trackTop + (trackBottom - trackTop - thumbH) * scroll / Math.max(1, max);
					p.fill(panelX + panelW - 7, trackTop, panelX + panelW - 5, trackBottom, 0x40000000);
					p.fill(panelX + panelW - 7, thumbY, panelX + panelW - 5, thumbY + thumbH, EDGE);
				}
			}
			case ERROR -> {
				p.centered("Holy Bible", cx, panelY + 8, RED);
				p.centered(error, cx, bodyTop + 20, INK);
			}
		}
	}
}
