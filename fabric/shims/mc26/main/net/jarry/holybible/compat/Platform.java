package net.jarry.holybible.compat;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.jarry.holybible.HolyBible;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/** Minecraft 26.x bridge: Identifier and Fabric's CreativeModeTabEvents. */
public final class Platform {
	public static final String MC_VERSION = "@MC@";

	private Platform() { }

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(HolyBible.MOD_ID, path);
	}

	public static String keyId(ResourceKey<?> key) {
		return key.identifier().toString();
	}

	public static Item registerItem(String path, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(path));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
	}

	public static void addToCreativeTab(Item item) {
		ResourceKey<CreativeModeTab> tools = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
			Identifier.withDefaultNamespace("tools_and_utilities"));
		CreativeModeTabEvents.modifyOutputEvent(tools).register(output -> output.accept(item));
	}

	public static AttachmentType<Boolean> persistentFlag(String path) {
		// Saved with the player data and kept through death; never synced, the client does not need it.
		return AttachmentRegistry.create(id(path), builder -> builder.persistent(Codec.BOOL).copyOnDeath());
	}
}
