package net.jarry.holybible.compat;

import net.jarry.holybible.HolyBible;
import net.minecraft.resources.@IDTYPE@;
import net.minecraft.resources.ResourceKey;

/** Minecraft @MC@ NeoForge bridge for resource ids. */
public final class Platform {
	public static final String MC_VERSION = "@MC@";

	private Platform() { }

	public static @IDTYPE@ id(String path) {
		return @IDTYPE@.fromNamespaceAndPath(HolyBible.MOD_ID, path);
	}

	public static String keyId(ResourceKey<?> key) {
		return key.@KEYGET@().toString();
	}
}
