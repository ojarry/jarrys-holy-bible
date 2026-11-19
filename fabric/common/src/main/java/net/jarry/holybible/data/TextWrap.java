package net.jarry.holybible.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/** Greedy word wrap against any width measure (the game font in-game, a fixed width in tests). */
public final class TextWrap {
	private TextWrap() { }

	public static List<String> wrap(String text, int maxWidth, ToIntFunction<String> width) {
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split(" ")) {
			if (word.isEmpty()) continue;
			String candidate = line.isEmpty() ? word : line + " " + word;
			if (width.applyAsInt(candidate) <= maxWidth) {
				line.setLength(0);
				line.append(candidate);
				continue;
			}
			if (!line.isEmpty()) lines.add(line.toString());
			line.setLength(0);
			// A single word wider than the page is split by characters.
			while (width.applyAsInt(word) > maxWidth && word.length() > 1) {
				int cut = word.length() - 1;
				while (cut > 1 && width.applyAsInt(word.substring(0, cut)) > maxWidth) cut--;
				lines.add(word.substring(0, cut));
				word = word.substring(cut);
			}
			line.append(word);
		}
		if (!line.isEmpty() || lines.isEmpty()) lines.add(line.toString());
		return lines;
	}
}
