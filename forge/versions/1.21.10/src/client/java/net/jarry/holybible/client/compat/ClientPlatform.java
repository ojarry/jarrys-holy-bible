package net.jarry.holybible.client.compat;

import net.jarry.holybible.client.BibleScreenBase;
import net.jarry.holybible.client.Painter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Minecraft 1.21.10 client bridge: GuiGraphics rendering. */
public final class ClientPlatform {
	private ClientPlatform() { }

	public static Screen newReader() {
		return new BibleScreen();
	}

	public static void openScreen(Screen screen) {
		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> mc.setScreen(screen));
	}

	static final class BibleScreen extends BibleScreenBase {
		@Override
		public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
			super.renderBackground(g, mouseX, mouseY, delta);
			paintPanel(painter(g));
		}

		@Override
		public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
			super.render(g, mouseX, mouseY, delta);
			paintText(painter(g));
		}

		private Painter painter(GuiGraphics g) {
			return new Painter() {
				@Override public void fill(int x1, int y1, int x2, int y2, int argb) { g.fill(x1, y1, x2, y2, argb); }
				@Override public void text(String s, int x, int y, int argb) { g.drawString(font, s, x, y, argb, false); }
				@Override public void centered(String s, int cx, int y, int argb) { g.drawString(font, s, cx - font.width(s) / 2, y, argb, false); }
				@Override public void item(net.minecraft.world.item.ItemStack stack, int x, int y) { g.renderItem(stack, x, y); }
			};
		}
	}
}
