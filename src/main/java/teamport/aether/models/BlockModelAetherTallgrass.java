package teamport.aether.models;

import net.minecraft.core.world.pos.TilePosc;
import net.minecraft.core.world.WorldSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapHelper;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelCrossedSquares;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.Side;
import teamport.aether.block.AetherBlocks;

@Environment(EnvType.CLIENT)
public class BlockModelAetherTallgrass<T extends BlockLogic> extends BlockModelCrossedSquares<T> {
    public BlockModelAetherTallgrass(Block<T> block) {
        super(block);
    }

    @Override
    public boolean render(TessellatorGeneral tessellator, WorldSource world, TilePosc pos) {
		int x = pos.x();
		int y = pos.y();
		int z = pos.z();
        float brightness = 1.0F;
        brightness = 1.0F;
            tessellator.setLightmapCoord1i(this.block.getLightmapCoord(world, pos.x(), pos.y(), pos.z()));

        int color = BlockColorDispatcher.getInstance().getDispatch(this.block).getWorldColor(world, pos, world.getBlockMetadata(pos.x(), pos.y(), pos.z()));
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        tessellator.setColorOpaque3f(brightness * r, brightness * g, brightness * b);
        double xd = x;
        double yd = y;
        double zd = z;
        if (this.block == AetherBlocks.TALLGRASS_AETHER) {
            long dRandom = x * 3129871L ^ z * 116129781L ^ y;
            dRandom = dRandom * dRandom * 42317861L + dRandom * 11L;
            xd += (((dRandom >> 16 & 15L) / 15.0F) - 0.5) * 0.5;
            yd += (((dRandom >> 20 & 15L) / 15.0F) - 1.0) * 0.2;
            zd += (((dRandom >> 24 & 15L) / 15.0F) - 0.5) * 0.5;
        }

        int metadata = world.getBlockMetadata(x, y, z);
        IconCoordinate texIndex = this.getBlockTextureFromSideAndMetadata(Side.BOTTOM, metadata);
        if (renderBlocks.overrideBlockTexture != null) {
            texIndex = renderBlocks.overrideBlockTexture;
        }

        double minU = texIndex.getIconUMin();
        double maxU = texIndex.getIconUMax();
        double minV = texIndex.getIconVMin();
        double maxV = texIndex.getIconVMax();
        double minX = xd + 0.5 - 0.45;
        double maxX = xd + 0.5 + 0.45;
        double minZ = zd + 0.5 - 0.45;
        double maxZ = zd + 0.5 + 0.45;
        tessellator.addVertexWithUV(minX, yd + 1.0 + 0.0, minZ, minU, minV);
        tessellator.addVertexWithUV(minX, yd + 0.0, minZ, minU, maxV);
        tessellator.addVertexWithUV(maxX, yd + 0.0, maxZ, maxU, maxV);
        tessellator.addVertexWithUV(maxX, yd + 1.0 + 0.0, maxZ, maxU, minV);
        tessellator.addVertexWithUV(maxX, yd + 1.0 + 0.0, maxZ, minU, minV);
        tessellator.addVertexWithUV(maxX, yd + 0.0, maxZ, minU, maxV);
        tessellator.addVertexWithUV(minX, yd + 0.0, minZ, maxU, maxV);
        tessellator.addVertexWithUV(minX, yd + 1.0 + 0.0, minZ, maxU, minV);
        tessellator.addVertexWithUV(minX, yd + 1.0 + 0.0, maxZ, minU, minV);
        tessellator.addVertexWithUV(minX, yd + 0.0, maxZ, minU, maxV);
        tessellator.addVertexWithUV(maxX, yd + 0.0, minZ, maxU, maxV);
        tessellator.addVertexWithUV(maxX, yd + 1.0 + 0.0, minZ, maxU, minV);
        tessellator.addVertexWithUV(maxX, yd + 1.0 + 0.0, minZ, minU, minV);
        tessellator.addVertexWithUV(maxX, yd + 0.0, minZ, minU, maxV);
        tessellator.addVertexWithUV(minX, yd + 0.0, maxZ, maxU, maxV);
        tessellator.addVertexWithUV(minX, yd + 1.0 + 0.0, maxZ, maxU, minV);
        return true;
    }
}

