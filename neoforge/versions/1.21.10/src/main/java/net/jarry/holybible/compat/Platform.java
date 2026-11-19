package net.jarry.holybible.compat;

import net.jarry.holybible.HolyBible;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;

/** Minecraft 1.21.10 NeoForge bridge for resource ids. */
public final class Platform {
	public static final String MC_VERSION = "1.21.10";

	private Platform() { }

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(HolyBible.MOD_ID, path);
	}

	public static String keyId(ResourceKey<?> key) {
		return key.location().toString();
	}
}
