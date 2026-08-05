package teamport.aether.entity.boss.valkyrie.queen;

import net.minecraft.client.render.entity.MobRendererBiped;
import org.jspecify.annotations.NonNull;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

/**
 * BTA 8.0 replaced the hand-written {@code ModelBiped} subclasses with DragonFly models, so the
 * geometry now lives in {@code valkyrie.geo.json} and the base class drives the standard biped
 * animation. Only the wing flap is specific to the valkyrie, so that is all this overrides.
 */
public class MobRendererBossValkyrie extends MobRendererBiped<MobBossValkyrie> {
	public MobRendererBossValkyrie(float shadowSize) {
		super(shadowSize);
	}

	@Override
	protected @NonNull StaticEntityModel getActiveModel(@NonNull MobBossValkyrie entity) {
		return this.getModel("main");
	}

	@Override
	protected StaticEntityModel setupAnimations(MobBossValkyrie entity, StaticEntityModel model, float partialTick, int layer) {
		super.setupAnimations(entity, model, partialTick, layer);
		ValkyrieWings.flap(model, entity.wingSpeed, entity.onGround);
		return model;
	}

	/**
	 * The wing flap, kept in one place because the queen and the regular valkyrie share it.
	 *
	 * <p>Carried over verbatim from the 7.3 {@code ModelValkyrie.setupAnimation}: the wings rest at
	 * a slight outward angle and then sweep on the entity's own {@code wingSpeed} phase. The flap is
	 * damped while the valkyrie is on the ground, which is what the old model expressed by feeding
	 * {@code onGround} into its {@code isRiding} flag.
	 */
	static final class ValkyrieWings {
		private ValkyrieWings() {}

		static void flap(StaticEntityModel model, float wingSpeed, boolean onGround) {
			float damping = onGround ? 8.0F : 3.0F;
			BoneTransform wingLeft = model.getTransform("wingLeft");
			BoneTransform wingRight = model.getTransform("wingRight");
			if (wingLeft == null || wingRight == null) return;

			wingLeft.rotY = (float) (-0.2F + Math.sin(wingSpeed) / 6.0);
			wingRight.rotY = (float) (0.2F - Math.sin(wingSpeed) / 6.0);
			wingLeft.rotZ = (float) (-0.125F + Math.cos(wingSpeed) / damping);
			wingRight.rotZ = (float) (0.125F - Math.cos(wingSpeed) / damping);
		}
	}
}
