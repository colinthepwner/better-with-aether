package teamport.aether.compat.model;

import net.minecraft.client.render.tessellator.TessellatorGeneral;

/**
 * Stands in for BTA 7.3's {@code net.minecraft.client.render.model.ModelBase} so ported models keep
 * their upstream shape — {@code public class ModelX extends ModelBase} with a
 * {@code render(limbSwing, ...)} / {@code setupAnimation(...)} pair — and only their import changes.
 *
 * <p>See {@link Cube} for why the legacy model system still has to exist for the accessory and cape
 * layers after BTA 8.0 deleted it.
 *
 * <p>{@code onGround} keeps its confusing upstream name: it is the swing progress of the current
 * attack, not a ground flag. Ported animation code reads it as such.
 */
public abstract class ModelBase {
	/** Attack swing progress, 0..1. Upstream name kept so ported animation code is unchanged. */
	public float onGround;

	public boolean isRiding;

	/**
	 * The tessellator legacy cubes draw with.
	 *
	 * <p>7.3 cubes reached for {@code Tessellator.instance}; 8.0 has no such global, so the render
	 * entry point binds one for the duration of a draw. This is deliberately thread-confined to the
	 * client render thread, which is the only place these models are used.
	 */
	private static TessellatorGeneral bound;

	/** Binds the tessellator legacy cubes draw with; pass null when the draw is finished. */
	public static void bind(TessellatorGeneral tessellator) {
		bound = tessellator;
	}

	static TessellatorGeneral boundTessellator() {
		return bound;
	}

	public abstract void render(float limbSwing, float limbYaw, float limbPitch, float headYaw, float headPitch, float scale);

	public void setupAnimation(float limbSwing, float limbYaw, float limbPitch, float headYaw, float headPitch, float scale) {
		// Overridden by models that animate; the base pose is the default.
	}
}
