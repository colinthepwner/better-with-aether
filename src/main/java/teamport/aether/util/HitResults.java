package teamport.aether.util;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.pos.TilePosc;

/**
 * Accessors for the BTA 8.0 {@link HitResult} hierarchy.
 *
 * <p>Up to BTA 7.3 a {@code HitResult} was one flat class carrying {@code hitType}, {@code entity},
 * {@code x}/{@code y}/{@code z} and {@code side} together, so callers read whichever fields their
 * {@code hitType} implied. BTA 8.0 split it into {@link HitResult.Tile} (tile position + side),
 * {@link HitResult.Entity} (the entity) and {@link HitResult.Clip} (side only), which means every
 * old field read now needs a type test first.
 *
 * <p>These helpers do that test and return {@code null} when the hit is of another kind, which
 * matches how the 7.3 code already behaved: on a tile hit {@code entity} was null, and callers
 * null-checked it before use.
 */
public final class HitResults {
	private HitResults() {}

	/** The entity hit, or null when this is not an entity hit. */
	public static Entity entity(HitResult hit) {
		return hit instanceof HitResult.Entity entityHit ? entityHit.entity : null;
	}

	/** True when this is a tile hit — replaces {@code hitType == HitType.TILE}. */
	public static boolean isTile(HitResult hit) {
		return hit instanceof HitResult.Tile;
	}

	/** True when this is an entity hit — replaces {@code hitType == HitType.ENTITY}. */
	public static boolean isEntity(HitResult hit) {
		return hit instanceof HitResult.Entity;
	}

	/** The tile position hit, or null when this is not a tile hit. */
	public static TilePosc tilePos(HitResult hit) {
		return hit instanceof HitResult.Tile tileHit ? tileHit.tilePos : null;
	}

	public static int x(HitResult hit) {
		TilePosc pos = tilePos(hit);
		return pos == null ? 0 : pos.x();
	}

	public static int y(HitResult hit) {
		TilePosc pos = tilePos(hit);
		return pos == null ? 0 : pos.y();
	}

	public static int z(HitResult hit) {
		TilePosc pos = tilePos(hit);
		return pos == null ? 0 : pos.z();
	}

	/**
	 * The side hit. Both tile and clip hits carry one; anything else has no side, so this returns
	 * null rather than inventing a direction.
	 */
	public static Side side(HitResult hit) {
		if (hit instanceof HitResult.Tile tileHit) return tileHit.side;
		if (hit instanceof HitResult.Clip clipHit) return clipHit.side;
		return null;
	}
}
