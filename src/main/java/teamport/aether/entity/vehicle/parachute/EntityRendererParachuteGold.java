package teamport.aether.entity.vehicle.parachute;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.lwjgl.opengl.GL11;
import org.useless.dragonfly.models.entity.StaticEntityModel;

/**
 * Gold variant of the parachute; shares {@code parachute.geo.json} with the plain one. See
 * {@link EntityRendererParachute} for why the geometry left Java in BTA 8.0.
 */
@Environment(EnvType.CLIENT)
public class EntityRendererParachuteGold extends EntityRenderer<EntityParachuteGold> {
	public EntityRendererParachuteGold() {
	}

	/** 8.0 made the shadowSize field private; the parachute still casts no shadow. */
	@Override
	public float getShadowSize(EntityParachuteGold entity) {
		return 0.0F;
	}

	@Override
	public void render(TessellatorGeneral tessellator, EntityParachuteGold entity, double x, double y, double z, float yaw, float partialTick) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);

		this.bindTexture("/assets/aether/textures/entity/parachute_gold.png");

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, .75F);

		GL11.glScalef(-1.0F, 1.0F, 1.0F);
		StaticEntityModel model = this.getModel("main");
		model.resetBones();
		model.render();
		GL11.glPopMatrix();
	}
}
