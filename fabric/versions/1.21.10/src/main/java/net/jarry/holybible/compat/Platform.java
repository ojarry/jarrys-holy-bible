package net.jarry.holybible.compat;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.jarry.holybible.HolyBible;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/** Minecraft 1.21.10/1.21.11 bridge: Fabric ItemGroupEvents; ResourceLocation ids. */
public final class Platform {
	public static final String MC_VERSION = "1.21.10";

	private Platform() { }

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(HolyBible.MOD_ID, path);
	}

	public static String keyId(ResourceKey<?> key) {
		return key.location().toString();
	}

	public static Item registerItem(String path, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(path));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
	}

	public static void addToCreativeTab(Item item) {
		ResourceKey<CreativeModeTab> tools = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
			ResourceLocation.withDefaultNamespace("tools_and_utilities"));
		ItemGroupEvents.modifyEntriesEvent(tools).register(entries -> entries.accept(item));
	}

	public static AttachmentType<Boolean> persistentFlag(String path) {
		// Saved with the player data and kept through death; never synced, the client does not need it.
		return AttachmentRegistry.create(id(path), builder -> builder.persistent(Codec.BOOL).copyOnDeath());
	}
}
