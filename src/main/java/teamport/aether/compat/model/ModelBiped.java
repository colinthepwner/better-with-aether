package teamport.aether.compat.model;

import net.minecraft.core.util.helper.MathHelper;

/**
 * Stands in for BTA 7.3's {@code net.minecraft.client.render.model.ModelBiped} so the accessory and
 * cape layers keep their upstream shape — {@code new ModelBiped(1.1F)} for an inflated overlay, then
 * {@code setupAnimation(...)} and per-part {@code render(scale)} — and only their import changes.
 *
 * <p>See {@link Cube} for why the legacy model system still has to exist after BTA 8.0 deleted it.
 *
 * <p>Part layout, UV offsets and the animation below are the vanilla biped ones, so an overlay drawn
 * through this class lines up with the player body BTA now renders from DragonFly geometry.
 */
public class ModelBiped extends ModelBase {
	public Cube head;
	public Cube hair;
	public Cube body;
	public Cube armRight;
	public Cube armLeft;
	public Cube legRight;
	public Cube legLeft;

	public boolean holdingLeftHand;
	public boolean holdingRightHand;
	public boolean holdingLarge;
	public boolean sneaking;

	public ModelBiped() {
		this(0.0F);
	}

	public ModelBiped(float expand) {
		this(expand, 0.0F);
	}

	public ModelBiped(float expand, float yOffset) {
		this.head = new Cube(0, 0);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, expand);
		this.head.setRotationPoint(0.0F, yOffset, 0.0F);

		this.hair = new Cube(32, 0);
		this.hair.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, expand + 0.5F);
		this.hair.setRotationPoint(0.0F, yOffset, 0.0F);

		this.body = new Cube(16, 16);
		this.body.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, expand);
		this.body.setRotationPoint(0.0F, yOffset, 0.0F);

		this.armRight = new Cube(40, 16);
		this.armRight.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, expand);
		this.armRight.setRotationPoint(-5.0F, 2.0F + yOffset, 0.0F);

		this.armLeft = new Cube(40, 16);
		this.armLeft.mirror = true;
		this.armLeft.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, expand);
		this.armLeft.setRotationPoint(5.0F, 2.0F + yOffset, 0.0F);

		this.legRight = new Cube(0, 16);
		this.legRight.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, expand);
		this.legRight.setRotationPoint(-2.0F, 12.0F + yOffset, 0.0F);

		this.legLeft = new Cube(0, 16);
		this.legLeft.mirror = true;
		this.legLeft.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, expand);
		this.legLeft.setRotationPoint(2.0F, 12.0F + yOffset, 0.0F);
	}

	@Override
	public void render(float limbSwing, float limbYaw, float limbPitch, float headYaw, float headPitch, float scale) {
		this.setupAnimation(limbSwing, limbYaw, limbPitch, headYaw, headPitch, scale);
		this.head.render(scale);
		this.body.render(scale);
		this.armRight.render(scale);
		this.armLeft.render(scale);
		this.legRight.render(scale);
		this.legLeft.render(scale);
	}

	@Override
	public void setupAnimation(float limbSwing, float limbYaw, float limbPitch, float headYaw, float headPitch, float scale) {
		this.head.yRot = headYaw / 57.29578F;
		this.head.xRot = headPitch / 57.29578F;
		this.hair.yRot = this.head.yRot;
		this.hair.xRot = this.head.xRot;

		this.armRight.xRot = MathHelper.cos(limbSwing * 0.6662F + 3.141593F) * 2.0F * limbYaw * 0.5F;
		this.armLeft.xRot = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbYaw * 0.5F;
		this.armRight.zRot = 0.0F;
		this.armLeft.zRot = 0.0F;

		this.legRight.xRot = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbYaw;
		this.legLeft.xRot = MathHelper.cos(limbSwing * 0.6662F + 3.141593F) * 1.4F * limbYaw;
		this.legRight.yRot = 0.0F;
		this.legLeft.yRot = 0.0F;

		if (this.isRiding) {
			this.armRight.xRot = -0.6283185F;
			this.armLeft.xRot = -0.6283185F;
			this.legRight.xRot = -1.256637F;
			this.legLeft.xRot = -1.256637F;
			this.legRight.yRot = 0.3141593F;
			this.legLeft.yRot = -0.3141593F;
		}

		if (this.holdingRightHand) this.armRight.xRot = this.armRight.xRot * 0.5F - 0.3141593F;
		if (this.holdingLeftHand) this.armLeft.xRot = this.armLeft.xRot * 0.5F - 0.3141593F;

		this.armRight.yRot = 0.0F;
		this.armLeft.yRot = 0.0F;

		if (this.onGround > -9990.0F) {
			float swing = this.onGround;
			this.body.yRot = MathHelper.sin(MathHelper.sqrt(swing) * 3.141593F * 2.0F) * 0.2F;
			this.armRight.yRot += this.body.yRot;
			this.armLeft.yRot += this.body.yRot;
			this.armLeft.xRot += this.body.yRot;

			swing = 1.0F - this.onGround;
			swing *= swing;
			swing *= swing;
			swing = 1.0F - swing;

			float lift = MathHelper.sin(swing * 3.141593F);
			float reach = MathHelper.sin(this.onGround * 3.141593F) * -(this.head.xRot - 0.7F) * 0.75F;
			this.armRight.xRot = (float) (this.armRight.xRot - (lift * 1.2 + reach));
			this.armRight.yRot += this.body.yRot * 2.0F;
			this.armRight.zRot = MathHelper.sin(this.onGround * 3.141593F) * -0.4F;
		}

		if (this.sneaking) {
			this.body.xRot = 0.5F;
			this.armRight.xRot += 0.4F;
			this.armLeft.xRot += 0.4F;
			this.legRight.z = 4.0F;
			this.legLeft.z = 4.0F;
			this.legRight.y = 9.0F;
			this.legLeft.y = 9.0F;
			this.head.y = 1.0F;
		} else {
			this.body.xRot = 0.0F;
			this.legRight.z = 0.0F;
			this.legLeft.z = 0.0F;
			this.legRight.y = 12.0F;
			this.legLeft.y = 12.0F;
			this.head.y = 0.0F;
		}

		// Idle arm sway.
		this.armRight.zRot += MathHelper.cos(limbPitch * 0.09F) * 0.05F + 0.05F;
		this.armLeft.zRot -= MathHelper.cos(limbPitch * 0.09F) * 0.05F + 0.05F;
		this.armRight.xRot += MathHelper.sin(limbPitch * 0.067F) * 0.05F;
		this.armLeft.xRot -= MathHelper.sin(limbPitch * 0.067F) * 0.05F;

		if (this.holdingLarge) {
			// Two-handed grip: both arms forward and level, as upstream did.
			this.armRight.xRot = -1.256637F;
			this.armLeft.xRot = -1.256637F;
			this.armRight.yRot = 0.3141593F;
			this.armLeft.yRot = -0.3141593F;
		}
	}
}
