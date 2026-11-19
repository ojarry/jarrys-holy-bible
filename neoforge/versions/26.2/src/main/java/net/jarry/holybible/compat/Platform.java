package net.jarry.holybible.compat;

import net.jarry.holybible.HolyBible;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/** Minecraft 26.2 NeoForge bridge for resource ids. */
public final class Platform {
	public static final String MC_VERSION = "26.2";

	private Platform() { }

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(HolyBible.MOD_ID, path);
	}

	public static String keyId(ResourceKey<?> key) {
		return key.identifier().toString();
	}
}
