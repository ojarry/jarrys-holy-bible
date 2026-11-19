package net.jarry.holybible;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.jarry.holybible.data.TextWrap;
import org.junit.jupiter.api.Test;

class TextWrapTest {
	@Test void wrapsOnWords() {
		List<String> l = TextWrap.wrap("In the beginning God created the heaven and the earth.", 20, String::length);
		for (String s : l) assertTrue(s.length() <= 20, s);
		assertEquals("In the beginning God created the heaven and the earth.", String.join(" ", l));
	}

	@Test void splitsOverlongWord() {
		List<String> l = TextWrap.wrap("Mahershalalhashbaz", 5, String::length);
		assertEquals("Mahershalalhashbaz", String.join("", l));
		for (String s : l) assertTrue(s.length() <= 5);
	}

	@Test void emptyGivesOneLine() {
		assertEquals(List.of(""), TextWrap.wrap("", 10, String::length));
	}
}
