package teamport.aether.entity.boss.sunspirit;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRendererBiped;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.useless.dragonfly.models.entity.StaticEntityModel;

/**
 * BTA 8.0 removed the hand-written {@code ModelBiped} subclasses, so the sunspirit's geometry moved
 * to {@code sunspirit.geo.json}. The old {@code ModelSunspirit.setupAnimation} was stock biped
 * animation (its {@code onGround} field was ModelBiped's swing progress, not a ground flag), and the
 * hair now sits inside the {@code head} bone rather than tracking it by hand, so the base class
 * covers everything that model used to do.
 */
@Environment(EnvType.CLIENT)
public class MobRendererSunspirit extends MobRendererBiped<MobBossSunspirit> {
	public MobRendererSunspirit() {
		super(0.8f);
	}

	@Override
	protected @NonNull StaticEntityModel getActiveModel(@NonNull MobBossSunspirit entity) {
		return this.getModel("main");
	}

	@Override
	public void renderPreview(TessellatorGeneral tessellator, MobBossSunspirit sunspirit, double x, double y, double z, float yaw, float partialTick) {
		GL11.glPushMatrix();
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		super.renderPreview(tessellator, sunspirit, x, y + 1, z, yaw, partialTick);
		GL11.glPopMatrix();
	}
}
