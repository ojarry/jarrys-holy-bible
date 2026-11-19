package net.jarry.holybible.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

/**
 * The King James Version text, read from the mod jar. The small index is read once; each book's
 * gzipped chapter file is only read the first time that book is opened, and a few recent books stay cached.
 */
public final class BibleText {
	public static final String ROOT = "/assets/jarrys_holy_bible/kjv/";
	private static final int CACHE_BOOKS = 4;

	public enum Testament { OLD, NEW }

	public record Book(int index, String file, String name, Testament testament, int[] verseCounts) {
		public int chapters() { return verseCounts.length; }
	}

	private static volatile BibleText instance;

	/** Opens a resource path such as "/assets/.../index.json"; returns null when missing. */
	private final Function<String, InputStream> opener;
	private final List<Book> books;
	private final int totalVerses;
	private final Map<Integer, List<List<String>>> cache = new LinkedHashMap<>(8, 0.75f, true) {
		@Override protected boolean removeEldestEntry(Map.Entry<Integer, List<List<String>>> e) { return size() > CACHE_BOOKS; }
	};

	public static BibleText get() {
		BibleText b = instance;
		if (b == null) {
			synchronized (BibleText.class) {
				if (instance == null) instance = new BibleText(BibleText.class::getResourceAsStream);
				b = instance;
			}
		}
		return b;
	}

	public BibleText(Function<String, InputStream> opener) {
		this.opener = opener;
		JsonObject index = readJson(ROOT + "index.json", false).getAsJsonObject();
		List<Book> list = new ArrayList<>();
		JsonArray arr = index.getAsJsonArray("books");
		for (int i = 0; i < arr.size(); i++) {
			JsonObject o = arr.get(i).getAsJsonObject();
			JsonArray v = o.getAsJsonArray("verses");
			int[] counts = new int[v.size()];
			for (int c = 0; c < counts.length; c++) counts[c] = v.get(c).getAsInt();
			list.add(new Book(i, o.get("file").getAsString(), o.get("name").getAsString(),
				"new".equals(o.get("testament").getAsString()) ? Testament.NEW : Testament.OLD, counts));
		}
		this.books = Collections.unmodifiableList(list);
		this.totalVerses = index.get("totalVerses").getAsInt();
	}

	public List<Book> books() { return books; }
	public int totalVerses() { return totalVerses; }

	public List<Book> books(Testament t) {
		List<Book> out = new ArrayList<>();
		for (Book b : books) if (b.testament() == t) out.add(b);
		return out;
	}

	/** Verses of one chapter (1-based chapter number). */
	public List<String> chapter(Book book, int chapter) {
		if (chapter < 1 || chapter > book.chapters()) throw new IndexOutOfBoundsException(book.name() + " " + chapter);
		return loadBook(book).get(chapter - 1);
	}

	public synchronized boolean isCached(Book book) { return cache.containsKey(book.index()); }

	private synchronized List<List<String>> loadBook(Book book) {
		List<List<String>> chapters = cache.get(book.index());
		if (chapters != null) return chapters;
		JsonArray arr = readJson(ROOT + book.file() + ".json.gz", true).getAsJsonArray();
		chapters = new ArrayList<>(arr.size());
		for (JsonElement ch : arr) {
			JsonArray verses = ch.getAsJsonArray();
			List<String> list = new ArrayList<>(verses.size());
			for (JsonElement v : verses) list.add(v.getAsString());
			chapters.add(Collections.unmodifiableList(list));
		}
		if (chapters.size() != book.chapters()) throw new IllegalStateException("Corrupt Bible data for " + book.name());
		chapters = Collections.unmodifiableList(chapters);
		cache.put(book.index(), chapters);
		return chapters;
	}

	private JsonElement readJson(String path, boolean gzip) {
		InputStream raw = opener.apply(path);
		if (raw == null) throw new IllegalStateException("Missing Bible resource " + path);
		try (InputStream in = gzip ? new GZIPInputStream(raw) : raw;
			 Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			return new Gson().fromJson(r, JsonElement.class);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read " + path, e);
		}
	}
}
