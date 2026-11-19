package net.jarry.holybible.gift;

import net.jarry.holybible.HolyBible;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Gives each player one Bible the first time they join a world. The "gifted" flag is a persistent
 * NeoForge data attachment on the player (saved in the player data, copied on death), so dying,
 * relogging or changing dimension never hands out a second copy.
 */
public final class GiftHandler {
	private GiftHandler() { }

	public static void register() {
		// PlayerLoggedInEvent runs on the server thread once the player is fully in the world.
		NeoForge.EVENT_BUS.addListener(GiftHandler::onLogin);
	}

	private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		GiftLogic.Outcome outcome = GiftLogic.give(new PlayerTarget(player));
		if (outcome != GiftLogic.Outcome.ALREADY_GIFTED) {
			HolyBible.LOG.info("Gave a Holy Bible to {} ({})", player.getName().getString(), outcome);
		}
	}

	static final class PlayerTarget implements GiftLogic.Target {
		private final ServerPlayer player;

		PlayerTarget(ServerPlayer player) { this.player = player; }

		@Override public boolean alreadyGifted() {
			return player.hasData(HolyBible.GIFTED) && Boolean.TRUE.equals(player.getData(HolyBible.GIFTED));
		}

		@Override public void markGifted() { player.setData(HolyBible.GIFTED, Boolean.TRUE); }

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
