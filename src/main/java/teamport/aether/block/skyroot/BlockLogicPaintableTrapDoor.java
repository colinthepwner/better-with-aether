package teamport.aether.block.skyroot;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicTrapDoor;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class BlockLogicPaintableTrapDoor extends BlockLogicTrapDoor {
    protected final Block<? extends BlockLogicPaintedTrapDoor> paintedBlock;

    public BlockLogicPaintableTrapDoor(Block<?> block, Material material, Block<? extends BlockLogicPaintedTrapDoor> paintedBlock) {
        super(block, material);
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
