package net.jarry.holybible.client;

/** The few drawing calls the reader needs; each Minecraft version adapts its own graphics class. */
public interface Painter {
	void fill(int x1, int y1, int x2, int y2, int argb);
	void text(String text, int x, int y, int argb);
	void centered(String text, int centerX, int y, int argb);
	void item(net.minecraft.world.item.ItemStack stack, int x, int y);
}
