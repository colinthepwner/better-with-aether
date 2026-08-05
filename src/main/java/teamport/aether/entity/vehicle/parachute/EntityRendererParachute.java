package teamport.aether.entity.vehicle.parachute;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.renderer.BlendFactor;
import org.lwjgl.opengl.GL11;
import org.useless.dragonfly.models.entity.StaticEntityModel;

/**
 * BTA 8.0 removed {@code ModelBase}/{@code Cube}, so the parachute's single 16x16x16 box moved to
 * {@code parachute.geo.json}. The GL setup around the draw is otherwise unchanged from 7.3.
 */
@Environment(EnvType.CLIENT)
public class EntityRendererParachute extends EntityRenderer<EntityParachute> {
	public EntityRendererParachute() {
	}

	/** 8.0 made the shadowSize field private; the parachute still casts no shadow. */
	@Override
	public float getShadowSize(EntityParachute entity) {
		return 0.0F;
	}

	@Override
	public void render(TessellatorGeneral tessellator, EntityParachute entity, double x, double y, double z, float yaw, float partialTick) {
		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate((float) x, (float) y, (float) z);

		this.bindTexture("/assets/aether/textures/entity/parachute.png");

		GLRenderer.enableState(State.DEPTH_TEST);
		GLRenderer.enableState(State.BLEND);
		GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, .75F);

		// The 7.3 renderer flipped both X and Y because the legacy model system drew Y-down.
		// DragonFly geometry is authored Y-up, so only the mirror across X is kept.
		GLRenderer.modelM4f().scale(-1.0F, 1.0F, 1.0F);
		StaticEntityModel model = this.getModel("main");
		model.resetBones();
		model.render();
		GLRenderer.popFrame();
	}
}
