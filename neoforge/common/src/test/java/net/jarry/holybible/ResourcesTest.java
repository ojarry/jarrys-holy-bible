package net.jarry.holybible;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResourcesTest {
	private static JsonObject json(String path) throws Exception {
		var in = ResourcesTest.class.getResourceAsStream(path);
		assertNotNull(in, path);
		try (var r = new InputStreamReader(in, StandardCharsets.UTF_8)) { return JsonParser.parseReader(r).getAsJsonObject(); }
	}

	private static String text(String path) throws Exception {
		InputStream in = ResourcesTest.class.getResourceAsStream(path);
		assertNotNull(in, path);
		try (in) { return new String(in.readAllBytes(), StandardCharsets.UTF_8); }
	}

	@Test void recipeIsBookPlusGold() throws Exception {
		JsonObject r = json("/data/jarrys_holy_bible/recipe/bible.json");
		assertEquals("minecraft:crafting_shapeless", r.get("type").getAsString());
		assertEquals("[\"minecraft:book\",\"minecraft:gold_ingot\"]", r.get("ingredients").toString());
		assertEquals("jarrys_holy_bible:bible", r.getAsJsonObject("result").get("id").getAsString());
	}

	@Test void modTomlDeclaresDependencies() throws Exception {
		String t = text("/META-INF/neoforge.mods.toml");
		assertTrue(t.contains("jarrys_holy_bible"), "mod id must be present");
		assertTrue(t.contains("neoforge") && t.contains("minecraft"), "must depend on neoforge and minecraft");
		assertFalse(t.contains("${"), "template properties must be expanded");
	}

	@Test void itemAssetsExist() throws Exception {
		json("/assets/jarrys_holy_bible/items/bible.json");
		json("/assets/jarrys_holy_bible/models/item/bible.json");
		assertNotNull(ResourcesTest.class.getResourceAsStream("/assets/jarrys_holy_bible/textures/item/bible.png"));
		assertEquals("Holy Bible", json("/assets/jarrys_holy_bible/lang/en_us.json").get("item.jarrys_holy_bible.bible").getAsString());
	}

	@Test void allSixteenVillageChestTables() {
		List<String> ids = VillageLoot.TABLES.stream().map(k -> net.jarry.holybible.compat.Platform.keyId(k)).sorted().toList();
		List<String> expected = List.of("armorer", "butcher", "cartographer", "desert_house", "fisher", "fletcher", "mason",
			"plains_house", "savanna_house", "shepherd", "snowy_house", "taiga_house", "tannery", "temple", "toolsmith", "weaponsmith")
			.stream().map(s -> "minecraft:chests/village/village_" + s).sorted().toList();
		assertEquals(expected, ids);
		assertEquals(0.001f, VillageLoot.CHANCE);
	}
}
