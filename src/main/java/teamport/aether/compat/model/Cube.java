package teamport.aether.compat.model;

import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.renderer.GLRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Stands in for BTA 7.3's {@code net.minecraft.client.render.model.Cube} so ported models keep their
 * upstream shape — {@code new Cube(texU, texV)}, {@code addBox(...)}, {@code setRotationPoint(...)},
 * {@code render(scale)} — and only their import changes.
 *
 * <p>BTA 8.0 deleted the whole legacy model package in favour of DragonFly geometry. Most of the
 * Aether's models moved to {@code .geo.json} with it, but the accessory and cape layers build their
 * overlay models in Java at render time and cannot: they need a biped sized to the wearer, and
 * DragonFly models are registered per-entity up front. This class is what lets that code survive.
 *
 * <p>The box layout and UV assignment are the classic Minecraft {@code ModelBox} ones, so texture
 * sheets authored for the 7.3 models still map correctly.
 *
 * <p>One thing genuinely changed: 8.0 removed the global {@code Tessellator.instance}, so a cube can
 * no longer fetch a tessellator on its own. Callers that already have one should use
 * {@link #render(TessellatorGeneral, float)}; {@link #render(float)} exists for ported call sites and
 * draws with whatever {@link ModelBase#bind} last supplied.
 */
public class Cube {
	/** Rotation point, in model space. Upstream names — ported code assigns these directly. */
	public float x;
	public float y;
	public float z;

	public float xRot;
	public float yRot;
	public float zRot;

	public boolean mirror;
	public boolean visible = true;

	private final int texU;
	private final int texV;
	private int textureWidth = 64;
	private int textureHeight = 32;

	/** Each quad is 4 vertices of (x, y, z, u, v), already expanded and UV-assigned. */
	private float[][] quads;

	public Cube(int texU, int texV) {
		this.texU = texU;
		this.texV = texV;
	}

	public Cube(int texU, int texV, int textureWidth, int textureHeight) {
		this(texU, texV);
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	public Cube addBox(float originX, float originY, float originZ, int width, int height, int depth) {
		return this.addBox(originX, originY, originZ, width, height, depth, 0.0F);
	}

	public Cube addBox(float originX, float originY, float originZ, int width, int height, int depth, float expand, boolean mirrored) {
		this.mirror = mirrored;
		return this.addBox(originX, originY, originZ, width, height, depth, expand);
	}

	/**
	 * Builds the box. {@code expand} inflates it evenly on all axes, which is how the armor and
	 * accessory layers sit outside the body without z-fighting.
	 */
	public Cube addBox(float originX, float originY, float originZ, int width, int height, int depth, float expand) {
		float x0 = originX - expand;
		float y0 = originY - expand;
		float z0 = originZ - expand;
		float x1 = originX + width + expand;
		float y1 = originY + height + expand;
		float z1 = originZ + depth + expand;

		if (this.mirror) {
			float swap = x1;
			x1 = x0;
			x0 = swap;
		}

		// Corners, in the same order as the vanilla ModelBox so the UV table below lines up.
		float[] c000 = {x0, y0, z0};
		float[] c100 = {x1, y0, z0};
		float[] c110 = {x1, y1, z0};
		float[] c010 = {x0, y1, z0};
		float[] c001 = {x0, y0, z1};
		float[] c101 = {x1, y0, z1};
		float[] c111 = {x1, y1, z1};
		float[] c011 = {x0, y1, z1};

		int u = this.texU;
		int v = this.texV;
		this.quads = new float[][] {
			// +X, -X
			quad(c101, c100, c110, c111, u + depth + width, v + depth, u + depth + width + depth, v + depth + height),
			quad(c000, c001, c011, c010, u, v + depth, u + depth, v + depth + height),
			// top, bottom
			quad(c101, c001, c000, c100, u + depth, v, u + depth + width, v + depth),
			quad(c110, c010, c011, c111, u + depth + width, v + depth, u + depth + width + width, v),
			// -Z, +Z
			quad(c100, c000, c010, c110, u + depth, v + depth, u + depth + width, v + depth + height),
			quad(c001, c101, c111, c011, u + depth + width + depth, v + depth, u + depth + width + depth + width, v + depth + height),
		};

		if (this.mirror) {
			for (int i = 0; i < this.quads.length; i++) this.quads[i] = flip(this.quads[i]);
		}
		return this;
	}

	private float[] quad(float[] a, float[] b, float[] c, float[] d, int uMin, int vMin, int uMax, int vMax) {
		float uL = (float) uMin / this.textureWidth;
		float uR = (float) uMax / this.textureWidth;
		float vT = (float) vMin / this.textureHeight;
		float vB = (float) vMax / this.textureHeight;
		return new float[] {
			a[0], a[1], a[2], uR, vT,
			b[0], b[1], b[2], uL, vT,
			c[0], c[1], c[2], uL, vB,
			d[0], d[1], d[2], uR, vB,
		};
	}

	/** Reverses winding so a mirrored cube still faces outwards. */
	private static float[] flip(float[] quad) {
		float[] out = new float[quad.length];
		for (int i = 0; i < 4; i++) {
			System.arraycopy(quad, (3 - i) * 5, out, i * 5, 5);
		}
		return out;
	}

	public void setRotationPoint(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setRotationAngle(float xRot, float yRot, float zRot) {
		this.xRot = xRot;
		this.yRot = yRot;
		this.zRot = zRot;
	}

	/** Draws with the tessellator bound by {@link ModelBase#bind}; see the class note. */
	public void render(float scale) {
		this.render(ModelBase.boundTessellator(), scale);
	}

	public void render(TessellatorGeneral tessellator, float scale) {
		if (!this.visible || this.quads == null || tessellator == null) return;

		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate(this.x * scale, this.y * scale, this.z * scale);
		if (this.zRot != 0.0F) GLRenderer.modelM4f().rotate(org.joml.Math.toRadians((float) (this.zRot * 57.295776F)), 0.0F, 0.0F, 1.0F);
		if (this.yRot != 0.0F) GLRenderer.modelM4f().rotate(org.joml.Math.toRadians((float) (this.yRot * 57.295776F)), 0.0F, 1.0F, 0.0F);
		if (this.xRot != 0.0F) GLRenderer.modelM4f().rotate(org.joml.Math.toRadians((float) (this.xRot * 57.295776F)), 1.0F, 0.0F, 0.0F);

		tessellator.startDrawingQuads();
		for (float[] quad : this.quads) {
			setNormalFor(tessellator, quad);
			for (int i = 0; i < 4; i++) {
				int o = i * 5;
				tessellator.addVertexWithUV(quad[o] * scale, quad[o + 1] * scale, quad[o + 2] * scale, quad[o + 3], quad[o + 4]);
			}
		}
		tessellator.draw();

		GLRenderer.popFrame();
	}

	/** Face normal from the first three vertices, so lighting matches the 7.3 output. */
	private static void setNormalFor(TessellatorGeneral tessellator, float[] quad) {
		float ax = quad[5] - quad[0];
		float ay = quad[6] - quad[1];
		float az = quad[7] - quad[2];
		float bx = quad[10] - quad[5];
		float by = quad[11] - quad[6];
		float bz = quad[12] - quad[7];

		float nx = ay * bz - az * by;
		float ny = az * bx - ax * bz;
		float nz = ax * by - ay * bx;
		float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		if (len < 1.0E-4F) return;
		tessellator.setNormal(nx / len, ny / len, nz / len);
	}
}
