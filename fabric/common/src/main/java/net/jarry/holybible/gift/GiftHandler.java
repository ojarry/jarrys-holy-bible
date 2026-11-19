package net.jarry.holybible.gift;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.jarry.holybible.HolyBible;
import net.jarry.holybible.compat.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Gives each player one Bible the first time they join a world. The "gifted" flag is a persistent
 * Fabric data attachment on the player (saved in the player data, copied on death), so dying,
 * relogging or changing dimension never hands out a second copy.
 */
public final class GiftHandler {
	public static final AttachmentType<Boolean> GIFTED = Platform.persistentFlag("gifted");

	private GiftHandler() { }

	public static void register() {
		// JOIN runs on the server thread once the player is fully in the world.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.player;
			GiftLogic.Outcome outcome = GiftLogic.give(new PlayerTarget(player));
			if (outcome != GiftLogic.Outcome.ALREADY_GIFTED) {
				HolyBible.LOG.info("Gave a Holy Bible to {} ({})", player.getName().getString(), outcome);
			}
		});
	}

	static final class PlayerTarget implements GiftLogic.Target {
		private final ServerPlayer player;

		PlayerTarget(ServerPlayer player) { this.player = player; }

		@Override public boolean alreadyGifted() { return Boolean.TRUE.equals(player.getAttached(GIFTED)); }
		@Override public void markGifted() { player.setAttached(GIFTED, Boolean.TRUE); }

		@Override public boolean insertBible() {
			ItemStack stack = new ItemStack(HolyBible.bible());
			// Inventory.add shrinks the stack by what fit; a stack of one either fits or stays.
			return player.getInventory().add(stack) && stack.isEmpty();
		}

		@Override public void dropBible() {
			// Placed exactly at the feet, pickable at once and exempt from the 5-minute despawn.
			ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(),
				new ItemStack(HolyBible.bible()), 0.0, 0.0, 0.0);
			entity.setNoPickUpDelay();
			entity.setUnlimitedLifetime();
			player.level().addFreshEntity(entity);
		}

		@Override public void tell(String message) {
			player.sendSystemMessage(Component.literal(message));
		}
	}
}
