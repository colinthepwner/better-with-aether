package teamport.aether.models;

import org.joml.primitives.AABBdc;
import net.minecraft.core.world.pos.TilePosc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.RenderBlocks;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelStandard;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.util.helper.Axis;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.phys.AABB;
import org.joml.primitives.AABBdc;
import net.minecraft.core.world.WorldSource;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.render.renderer.GLRenderer;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class BlockModelGrassAether<T extends BlockLogic> extends BlockModelStandard<T> {
    public static boolean useOverlay = false;
    private static final IconCoordinate[] overlayIndices = new IconCoordinate[]{
        null,
        null,
        TextureRegistry.getTexture("aether:block/grass_aether/side_overlay"),
        TextureRegistry.getTexture("aether:block/grass_aether/side_overlay"),
        TextureRegistry.getTexture("aether:block/grass_aether/side_overlay"),
        TextureRegistry.getTexture("aether:block/grass_aether/side_overlay")};
    protected IconCoordinate snowSide = TextureRegistry.getTexture("aether:block/grass_aether/snowy_side");
    protected IconCoordinate retroSnowSide = TextureRegistry.getTexture("aether:block/grass_aether/snowy_side_retro");

    public BlockModelGrassAether(Block<T> block) {
        super(block);
    }

    @Override
    public boolean render(TessellatorGeneral tessellator, WorldSource world, TilePosc pos) {
		int x = pos.x();
		int y = pos.y();
		int z = pos.z();
        AABBdc bounds = this.block.getBounds();
        boolean didRender = this.isRetro() ? renderBlocks.renderStandardBlock(tessellator, world, this, bounds, pos) : renderBlocks.renderStandardBlock(tessellator, world, this, bounds, pos);
        if (!this.blockTextures.hasTexture() || !this.isRetro()) {
            useOverlay = true;
            didRender |= renderBlocks.renderStandardBlock(tessellator, world, this, bounds, pos);
            useOverlay = false;
        }

        return didRender;
    }

    public void renderBlockOnInventory(TessellatorGeneral tessellator, int metadata, float brightness, float alpha, @Nullable Integer lightmapCoordinate) {
        GL11.glColor4f(brightness, brightness, brightness, alpha);
        float yOffset = 0.5F;
        AABBdc bounds = this.getBlockBoundsForItemRender();
        GLRenderer.modelM4f().translate(-0.5F, 0.0F - yOffset, -0.5F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderBlocks.renderBottomFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.BOTTOM, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderBlocks.renderNorthFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.NORTH, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderBlocks.renderSouthFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.SOUTH, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderBlocks.renderWestFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.WEST, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderBlocks.renderEastFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.EAST, metadata));
        tessellator.draw();
        if (renderBlocks.useInventoryTint && !this.isRetro()) {
            int l = BlockColorDispatcher.getInstance().getDispatch(this.block).getFallbackColor(metadata, 0);
            float f4 = (l >> 16 & 255) / 255.0F;
            float f8 = (l >> 8 & 255) / 255.0F;
            float f9 = (l & 255) / 255.0F;
            GL11.glColor4f(f4 * brightness, f8 * brightness, f9 * brightness, alpha);
        }

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderBlocks.renderTopFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.TOP, metadata));
        tessellator.draw();
        if (!this.isRetro()) {
            useOverlay = true;
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            renderBlocks.renderNorthFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.NORTH, metadata));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderBlocks.renderSouthFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.SOUTH, metadata));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(-1.0F, 0.0F, 0.0F);
            renderBlocks.renderWestFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.WEST, metadata));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderBlocks.renderEastFace(tessellator, bounds, 0.0F, 0.0F, 0.0F, this.getBlockTextureFromSideAndMetadata(Side.EAST, metadata));
            tessellator.draw();
            useOverlay = false;
        }

        GLRenderer.modelM4f().translate(0.5F, 0.5F, 0.5F);
    }

    @Override
    public IconCoordinate getBlockTexture(WorldSource blockAccess, TilePosc pos, Side side) {
        Material above = blockAccess.getBlockMaterial(pos.x(), pos.y() + 1, pos.z());
        boolean isSnowy = (above == Materials.TOP_SNOW || above == Materials.SNOW);

        if (isSnowy && side.axis() != Axis.Y) {
            return this.isRetro() ? retroSnowSide : snowSide;
        }

        return super.getBlockTexture(blockAccess, pos, side);
    }

    @Override
    public IconCoordinate getBlockTextureFromSideAndMetadata(Side side, int data) {
        return useOverlay ? overlayIndices[side.id] : super.getBlockTextureFromSideAndMetadata(side, data);
    }

    @Override
    public boolean shouldSideBeColored(WorldSource blockAccess, TilePosc pos, Side side, int meta) {
        Material material = blockAccess.getBlockMaterial(pos.x(), pos.y() + 1, pos.z());
        if (material != Materials.TOP_SNOW && material != Materials.SNOW) {
            return useOverlay || side.id == Side.TOP.id;
        } else {
            return false;
        }
    }
}
