package net.jarry.holybible.gift;

import net.jarry.holybible.HolyBible;
import net.jarry.holybible.compat.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Gives each player one Bible the first time they join a world. The "gifted" flag is stored in
 * the player's persistent Forge data (saved with the player), and is copied on death via
 * PlayerEvent.Clone, so dying, relogging or changing dimension never hands out a second copy.
 */
public final class GiftHandler {
	/** Key inside Entity#getPersistentData() (the official Forge per-entity NBT bag). */
	public static final String GIFT_KEY = "jarrys_holy_bible_gifted";

	private GiftHandler() { }

	public static void register() {
		PlayerEvent.PlayerLoggedInEvent.BUS.addListener(GiftHandler::onLogin);
		PlayerEvent.Clone.BUS.addListener(GiftHandler::onClone);
	}

	private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		GiftLogic.Outcome outcome = GiftLogic.give(new PlayerTarget(player));
		if (outcome != GiftLogic.Outcome.ALREADY_GIFTED) {
			HolyBible.LOG.info("Gave a Holy Bible to {} ({})", player.getName().getString(), outcome);
		}
	}

	/** Copy the gifted flag onto the new player entity after death (and after returning from the End). */
	private static void onClone(PlayerEvent.Clone event) {
		if (Platform.nbtGetBoolean(event.getOriginal().getPersistentData(), GIFT_KEY)) {
			Platform.nbtPutBoolean(event.getEntity().getPersistentData(), GIFT_KEY, true);
		}
	}

	static final class PlayerTarget implements GiftLogic.Target {
		private final ServerPlayer player;

		PlayerTarget(ServerPlayer player) { this.player = player; }

		@Override public boolean alreadyGifted() {
			return Platform.nbtGetBoolean(player.getPersistentData(), GIFT_KEY);
		}

		@Override public void markGifted() {
			Platform.nbtPutBoolean(player.getPersistentData(), GIFT_KEY, true);
		}

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
