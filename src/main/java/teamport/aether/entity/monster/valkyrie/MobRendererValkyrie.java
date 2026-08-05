package teamport.aether.entity.monster.valkyrie;

import net.minecraft.client.render.entity.MobRendererBiped;
import org.jspecify.annotations.NonNull;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

/**
 * The regular valkyrie. BTA 8.0 replaced hand-written {@code ModelBiped} subclasses with DragonFly
 * models, so the geometry now lives in {@code valkyrie.geo.json} and the base class drives the
 * standard biped animation; only the wing flap is valkyrie-specific.
 */
public class MobRendererValkyrie extends MobRendererBiped<MobValkyrie> {
	public MobRendererValkyrie(float shadowSize) {
		super(shadowSize);
	}

	@Override
	protected @NonNull StaticEntityModel getActiveModel(@NonNull MobValkyrie entity) {
		return this.getModel("main");
	}

	@Override
	protected StaticEntityModel setupAnimations(MobValkyrie entity, StaticEntityModel model, float partialTick, int layer) {
		super.setupAnimations(entity, model, partialTick, layer);

		// Carried over from the 7.3 ModelValkyrie: wings rest slightly outward and sweep on the
		// entity's own wingSpeed phase, damped while on the ground.
		float damping = entity.onGround ? 8.0F : 3.0F;
		BoneTransform wingLeft = model.getTransform("wingLeft");
		BoneTransform wingRight = model.getTransform("wingRight");
		if (wingLeft != null && wingRight != null) {
			wingLeft.rotY = (float) (-0.2F + Math.sin(entity.wingSpeed) / 6.0);
			wingRight.rotY = (float) (0.2F - Math.sin(entity.wingSpeed) / 6.0);
			wingLeft.rotZ = (float) (-0.125F + Math.cos(entity.wingSpeed) / damping);
			wingRight.rotZ = (float) (0.125F - Math.cos(entity.wingSpeed) / damping);
		}
		return model;
	}
}
