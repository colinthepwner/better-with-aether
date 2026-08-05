package teamport.aether.models;

import org.joml.primitives.AABBdc;
import net.minecraft.core.world.pos.TilePosc;
import net.minecraft.core.world.WorldSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelStandard;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.phys.AABB;
import org.joml.primitives.AABBdc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class BlockModelAetherStoneMossy<T extends BlockLogic> extends BlockModelStandard<T> {
    protected IconCoordinate mossOverlay = TextureRegistry.getTexture("aether:block/moss_overlay");

    public BlockModelAetherStoneMossy(Block<T> block) {
        super(block);
    }

    @Override
    public boolean render(TessellatorGeneral tessellator, WorldSource world, TilePosc pos) {
		int x = pos.x();
		int y = pos.y();
		int z = pos.z();
        org.joml.primitives.AABBdc bounds = this.block.getBlockBoundsFromState(world, pos.x(), pos.y(), pos.z());
        renderBlocks.renderStandardBlock(tessellator, world, this, bounds, pos.x(), pos.y(), pos.z(), 1.0F, 1.0F, 1.0F);
        renderBlocks.overrideBlockTexture = this.mossOverlay;
        renderBlocks.renderStandardBlock(tessellator, world, this, bounds, pos.x(), pos.y(), pos.z());
        renderBlocks.overrideBlockTexture = null;
        return true;
    }

    public void renderBlockOnInventory(TessellatorGeneral tessellator, int metadata, float brightness, float alpha, @Nullable Integer lightmapCoordinate) {
        renderBlocks.useInventoryTint = false;
        // removed
        renderBlocks.useInventoryTint = true;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        int color = (BlockColorDispatcher.getInstance().getDispatch(this.block)).getFallbackColor(metadata, 0);
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        GL11.glColor4f(r * brightness, g * brightness, b * brightness, alpha);
        AABBdc bounds = this.block.getBounds();
        IconCoordinate mossCoord = this.mossOverlay;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderBlocks.renderBottomFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, mossCoord);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderBlocks.renderTopFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, mossCoord);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderBlocks.renderNorthFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, mossCoord);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderBlocks.renderSouthFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, mossCoord);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderBlocks.renderWestFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, mossCoord);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderBlocks.renderEastFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, mossCoord);
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }
}
