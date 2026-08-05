package teamport.aether.entity.monster.swet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.core.util.helper.MathHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.useless.dragonfly.models.entity.StaticEntityModel;

/**
 * BTA 8.0 removed {@code ModelBase}/{@code ModelSlime} and the {@code prepareArmor} overlay pass, so
 * the swet's two-pass render (translucent outer shell over an inner body) is now expressed as two
 * numbered render layers over the DragonFly slime geometry.
 */
@Environment(EnvType.CLIENT)
public class MobRendererSwet extends MobRenderer<MobSwet> {

	public MobRendererSwet(float shadowSize) {
		super(shadowSize);
	}

	/** Layer 0 is the inner body, layer 1 the translucent outer shell. */
	@Override
	protected int maxRenderLayer(MobSwet swet) {
		return 2;
	}

	@Override
	protected @Nullable StaticEntityModel getAndSetupModelForLayer(@NonNull MobSwet swet, float brightness, float partialTick, int layer) {
		if (layer == 1) {
			// The old renderSlimePassModel enabled blending for the outer shell and turned it back
			// off afterwards; the layer split gives the same ordering.
			GL11.glEnable(GL11.GL_NORMALIZE);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		} else {
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		StaticEntityModel model = this.getModel(layer == 1 ? "shell" : "main");
		if (model != null) model.resetBones();
		return model;
	}

	/** The squash-and-stretch on jump; unchanged apart from 8.0's renamed hook. */
	@Override
	protected void preRenderTransform(MobSwet swet, double x, double y, double z, float yaw, float partialTick) {
		super.preRenderTransform(swet, x, y, z, yaw, partialTick);

		float stretchY = 1.0F;
		float stretchXZ = 1.0F;
		float scale = 1.5F;
		double yd = MathHelper.lerp(swet.getYdO(), swet.yd, partialTick);
		if (!swet.onGround) {
			if (yd > 0.85) {
				stretchY = 1.425F;
				stretchXZ = 0.575F;
			} else if (yd < -0.85) {
				stretchY = 0.575F;
				stretchXZ = 1.425F;
			} else {
				float delta = (float) yd * 0.5F;
				stretchY += delta;
				stretchXZ -= delta;
			}
		}

		if (swet.passenger != null) {
			scale = 1.5F + (swet.passenger.bbWidth + swet.passenger.bbHeight) * 0.75F;
		}

		stretchY = MathHelper.clamp(stretchY, 0.1F, 10.0F);
		stretchXZ = MathHelper.clamp(stretchXZ, 0.1F, 10.0F);
		scale = MathHelper.clamp(scale, 0.1F, 10.0F);

		GL11.glScalef(stretchXZ * scale, stretchY * scale, stretchXZ * scale);
	}
}
