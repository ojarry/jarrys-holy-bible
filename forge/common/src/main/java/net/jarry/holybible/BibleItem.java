package net.jarry.holybible;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** The Holy Bible. Right-click opens the reader on the client; nothing happens on the server. */
public class BibleItem extends Item {
	/** Set by the client entrypoint. Stays a no-op on a dedicated server, so no client class is ever loaded there. */
	private static volatile Runnable openReader = () -> { };

	public static void setReaderOpener(Runnable opener) {
		openReader = opener == null ? () -> { } : opener;
	}

	public BibleItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) openReader.run();
		return InteractionResult.SUCCESS;
	}
}
