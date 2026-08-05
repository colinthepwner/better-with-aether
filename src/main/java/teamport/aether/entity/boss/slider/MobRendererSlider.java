package teamport.aether.entity.boss.slider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapHelper;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.useless.dragonfly.models.entity.StaticEntityModel;

/**
 * BTA 8.0 moved entity geometry to DragonFly ({@code slider.geo.json}) and replaced the old
 * {@code prepareArmor}/{@code setArmorModel} overlay pass with numbered render layers, so the glow
 * that used to be drawn by the armor pass is now layer 1.
 */
@Environment(EnvType.CLIENT)
public class MobRendererSlider extends MobRenderer<MobBossSlider> {

	public MobRendererSlider(float shadowSize) {
		super(shadowSize);
	}

	/** Layer 0 is the body, layer 1 the emissive eyes. */
	@Override
	protected int maxRenderLayer(MobBossSlider slider) {
		return 2;
	}

	@Override
	protected @Nullable StaticEntityModel getAndSetupModelForLayer(@NonNull MobBossSlider slider, float brightness, float partialTick, int layer) {
		if (layer == 1) {
			this.bindTexture(glowTexture(slider));
			net.minecraft.client.render.renderer.GLRenderer.setLightmapCoord2i(255, 255);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		StaticEntityModel model = this.getModel("main");
		model.resetBones();
		return model;
	}

	private static String glowTexture(MobBossSlider slider) {
		String state = slider.isAwake() && !slider.doingSlam() ? "awake" : "sleep";
		String tint = slider.isAngry() ? "_red" : "";
		return "/assets/aether/textures/entity/boss_slider/slider_" + state + tint + "_glow.png";
	}

	@Override
	public void renderPreview(TessellatorGeneral tessellator, MobBossSlider slider, double x, double y, double z, float yaw, float partialTick) {
		GL11.glPushMatrix();
		GL11.glScalef(0.75F, 0.75F, 0.75F);
		this.bindTexture("/assets/aether/textures/entity/boss_slider/slider_awake.png");
		super.renderPreview(tessellator, slider, x, y + 0.5, z, yaw, partialTick);
		GL11.glPopMatrix();
	}

	/** 8.0 renamed the pre-render hook; the squash-on-slam tilt is unchanged. */
	@Override
	protected void preRenderTransform(MobBossSlider slider, double x, double y, double z, float yaw, float partialTick) {
		super.preRenderTransform(slider, x, y, z, yaw, partialTick);
		if (slider.getDeformX() > 0.01F) {
			GL11.glRotatef(slider.getDeformX() * -30.0F, slider.getDeformY(), 0.0F, slider.getDeformZ());
		}
	}
}
