package teamport.aether.models;

import net.minecraft.core.world.pos.TilePosc;
import net.minecraft.core.world.WorldSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelAxisAligned;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicAxisAligned;
import net.minecraft.core.util.helper.Axis;

@Environment(EnvType.CLIENT)
public class BlockModelAetherLog<T extends BlockLogic> extends BlockModelAxisAligned<T> {
    public BlockModelAetherLog(Block<T> block) {
        super(block);
    }

    @SuppressWarnings("java:S131")
    @Override
    public boolean render(TessellatorGeneral tessellator, WorldSource world, TilePosc pos) {
		int x = pos.x();
		int y = pos.y();
		int z = pos.z();
        int meta = world.getBlockMetadata(x, y, z);
        Axis axis = BlockLogicAxisAligned.metaToAxis(meta & 0b11);
        switch (axis) {
            case Y:
                renderBlocks.uvRotateEast = 0;
                renderBlocks.uvRotateWest = 0;
                renderBlocks.uvRotateSouth = 0;
                renderBlocks.uvRotateNorth = 0;
                break;
            case Z:
                renderBlocks.uvRotateSouth = 1;
                renderBlocks.uvRotateNorth = 1;
                break;
            case X:
                renderBlocks.uvRotateEast = 1;
                renderBlocks.uvRotateWest = 1;
                renderBlocks.uvRotateTop = 1;
                renderBlocks.uvRotateBottom = 1;
        }

        renderBlocks.renderStandardBlock(tessellator, world, this, this.block.getBlockBoundsFromState(world, x, y, z), x, y, z);
                return true;
    }
}
