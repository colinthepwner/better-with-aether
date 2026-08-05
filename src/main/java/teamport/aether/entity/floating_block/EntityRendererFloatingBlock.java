package teamport.aether.entity.floating_block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Lighting;
import net.minecraft.client.render.block.model.RenderBlocks;
import net.minecraft.client.render.TileEntityRenderDispatcher;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.client.render.tileentity.TileEntityRenderer;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.BlocksContainer;
import net.minecraft.client.render.renderer.GLRenderer;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class EntityRendererFloatingBlock extends EntityRenderer<EntityFloatingBlock> {
    private final Minecraft mc = Minecraft.getMinecraft();
    private BlocksContainer container = null;
    private RenderBlocks containerRenderBlock = null;

    public EntityRendererFloatingBlock() {
    }

    public void render(TessellatorGeneral tessellator, EntityFloatingBlock floatingBlock, double x, double y, double z, float yaw, float partialTick) {
        if (this.container == null || this.container.world != floatingBlock.world) {
            this.container = new BlocksContainer(floatingBlock.world);
            
        }

        GLRenderer.pushFrame();
        GL11.glTranslated(x, y, z);
        net.minecraft.client.render.texture.stitcher.TextureRegistry.worldAtlas.bind();
        Lighting.disable();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        if (net.minecraft.client.option.GameSettings.AMBIENT_OCCLUSION.value) {
            GL11.glShadeModel(7425);
        } else {
            GL11.glShadeModel(7424);
        }

        int blockX = MathHelper.floor(floatingBlock.x);
        int blockY = MathHelper.floor(floatingBlock.y);
        int blockZ = MathHelper.floor(floatingBlock.z);

        tessellator.startDrawingQuads();
        tessellator.setTranslation((-blockX) - 0.5, (-blockY) - 0.5, (-blockZ) - 0.5);
        

        this.container.setLightReferenceEntity(floatingBlock);
        this.container.setBlock(blockX, blockY, blockZ, floatingBlock.getCarriedBlock().blockId, floatingBlock.getCarriedBlock().metadata, floatingBlock.getCarriedBlock().entity);

        net.minecraft.client.render.block.model.BlockModelDispatcher.getInstance().getDispatch(net.minecraft.core.block.Blocks.getBlock(floatingBlock.getCarriedBlock().blockId)).renderNoCulling(tessellator, this.container, new net.minecraft.core.world.pos.TilePos(blockX, blockY, blockZ));

        this.container.setLightReferenceEntity(null);
        this.container.clear();

        tessellator.setTranslation(0.0, 0.0, 0.0);
        tessellator.draw();
        Lighting.enableLight();
        GLRenderer.popFrame();
        TileEntityRenderer<TileEntity> renderer = TileEntityRenderDispatcher.instance.getRenderer(floatingBlock.getCarriedBlock().entity);

        if (renderer != null) {
            GLRenderer.pushFrame();
            renderer.doRender(tessellator, floatingBlock.getCarriedBlock().entity, x - 0.5, y - 0.5, z - 0.5, partialTick);
            GLRenderer.popFrame();
        }
    }
}
