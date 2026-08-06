package teamport.aether.block.skyroot;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSlabPaintable;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class BlockLogicPaintableSlab extends BlockLogicSlabPaintable {
    protected final Block<? extends BlockLogicPaintedSlab> paintedBlock;

    public BlockLogicPaintableSlab(Block<?> block, Block<?> modelBlock, Block<? extends BlockLogicPaintedSlab> paintedBlock) {
        super(block, modelBlock);
        this.paintedBlock = paintedBlock;
    }

    @Override
    public void setColor(World world, TilePosc pos, DyeColor color) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockAndMetadata(x, y, z, paintedBlock.id(), meta);
        paintedBlock.getLogic().setColor(world, x, y, z, color);
    }
}
