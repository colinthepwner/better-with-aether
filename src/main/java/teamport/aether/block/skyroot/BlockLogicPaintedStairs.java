package teamport.aether.block.skyroot;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicStairsPainted;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class BlockLogicPaintedStairs extends BlockLogicStairsPainted {
    protected final int unpaintedBlockID;

    public BlockLogicPaintedStairs(Block<?> block, Block<?> modelBlock, int unpaintedBlockID) {
        super(block, modelBlock);
        this.unpaintedBlockID = unpaintedBlockID;
    }

    @Override
    public void removeDye(World world, TilePosc pos) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockAndMetadataWithNotify(x, y, z, unpaintedBlockID, this.stripColorFromMetadata(meta));
    }
}
