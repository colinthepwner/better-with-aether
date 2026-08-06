package teamport.aether.block.skyroot;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicButtonPainted;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class BlockLogicPaintedButton extends BlockLogicButtonPainted {
    protected final int unpaintedBlockID;

    public BlockLogicPaintedButton(Block<?> block, int unpaintedBlockID) {
        super(block);
        this.unpaintedBlockID = unpaintedBlockID;
    }

    @Override
    public int tickDelay() {
        return 5;
    }

    @Override
    public void removeDye(World world, TilePosc pos) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();
        world.setBlockWithNotify(x, y, z, unpaintedBlockID);
    }
}
