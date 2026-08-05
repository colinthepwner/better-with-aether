package teamport.aether.helper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.renderer.BlendFactor;
import org.lwjgl.opengl.GL11;

/// Rendering helpers for client-only mixins.
///
/// Kept apart from [MixinHelper] on purpose. That class is reached from common code -- notably
/// `WorldGetCubesMixin`, which runs on every entity move -- and holding a single method that
/// touches `TessellatorGeneral` was enough to make the dedicated server throw
/// "Cannot load class ... in environment type SERVER" the first time an entity moved, because
/// linking the helper resolves every type it mentions.
@Environment(EnvType.CLIENT)
public final class ClientRenderHelper {
    private ClientRenderHelper() {
    }

    public static void renderShieldVignette(TextureManager textureManager, int xSize, int ySize) {
        GLRenderer.pushFrame();
        GLRenderer.enableState(State.BLEND);
        GLRenderer.disableState(State.DEPTH_TEST);
        GLRenderer.setDepthMask(false);
        GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
        GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // 8.0's context has no fixed-function alpha test; the threshold lives on GLRenderer, and 0
        // is "accept everything", which is what glDisable(GL_ALPHA_TEST) meant here.
        float previousAlphaTest = GLRenderer.getAlphaTest();
        GLRenderer.setAlphaTest(0.0F);

        textureManager.loadTexture("/assets/aether/textures/other/shieldvignette.png").bind();

        TessellatorGeneral tessellator = GLRenderer.getTessellator();
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0.0, ySize, -90.0, 0.0, 1.0);
        tessellator.addVertexWithUV(xSize, ySize, -90.0, 1.0, 1.0);
        tessellator.addVertexWithUV(xSize, 0.0, -90.0, 1.0, 0.0);
        tessellator.addVertexWithUV(0.0, 0.0, -90.0, 0.0, 0.0);
        tessellator.draw();

        GLRenderer.setDepthMask(true);
        GLRenderer.enableState(State.DEPTH_TEST);
        GLRenderer.setAlphaTest(previousAlphaTest);
        GLRenderer.disableState(State.BLEND);
        GLRenderer.popFrame();
        GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
