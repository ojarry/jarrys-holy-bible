package net.jarry.holybible;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

	@Test void recipeIsBookPlusGold() throws Exception {
		JsonObject r = json("/data/jarrys_holy_bible/recipe/bible.json");
		assertEquals("minecraft:crafting_shapeless", r.get("type").getAsString());
		assertEquals("[\"minecraft:book\",\"minecraft:gold_ingot\"]", r.get("ingredients").toString());
		assertEquals("jarrys_holy_bible:bible", r.getAsJsonObject("result").get("id").getAsString());
	}

	@Test void modJsonDeclaresDependencies() throws Exception {
		JsonObject m = json("/fabric.mod.json");
		assertEquals("jarrys_holy_bible", m.get("id").getAsString());
		JsonObject d = m.getAsJsonObject("depends");
		assertTrue(d.has("minecraft") && d.has("fabric-api") && d.has("fabricloader"));
		assertFalse(m.get("version").getAsString().contains("$"), "version must be expanded");
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
