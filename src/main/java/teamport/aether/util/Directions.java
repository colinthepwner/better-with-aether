package teamport.aether.util;

import net.minecraft.core.util.helper.Direction;

/**
 * Direction helpers for APIs BTA 8.0 removed.
 *
 * <p>BTA 7.3 had a {@code getHorizontalIndex()} on Direction that returned a 0-3 index used as block
 * metadata for rotatable blocks (doors, dungeon rooms). 8.0 dropped the method but kept the ordered
 * {@link Direction#horizontal} array, so the index is recovered by position in that array.
 */
public final class Directions {
	private Directions() {}

	/**
	 * The 0-3 index of a horizontal direction, replacing the 7.3 method.
	 *
	 * <p>Returns 0 for anything non-horizontal, matching the old behaviour of falling back to the
	 * first facing rather than throwing during worldgen.
	 */
	public static int horizontalIndex(Direction direction) {
		Direction[] horizontal = Direction.horizontal;
		for (int i = 0; i < horizontal.length; i++) {
			if (horizontal[i] == direction) return i;
		}
		return 0;
	}

	/** The horizontal direction for a 0-3 metadata index; inverse of {@link #horizontalIndex}. */
	public static Direction fromHorizontalIndex(int index) {
		Direction[] horizontal = Direction.horizontal;
		return horizontal[Math.floorMod(index, horizontal.length)];
	}
}
