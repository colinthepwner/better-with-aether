package teamport.aether.block.skyroot;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicButtonPlanksOak;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class BlockLogicPaintableButton extends BlockLogicButtonPlanksOak {
    protected final Block<? extends BlockLogicPaintedButton> paintedBlock;

    public BlockLogicPaintableButton(Block<?> block, Block<? extends BlockLogicPaintedButton> paintedBlock) {
        super(block);
        this.paintedBlock = paintedBlock;
    }

    @Override
    public int tickDelay() {
        return 5;
    }

    @Override
    public boolean canBePainted() {
        return true;
    }

    @Override
    public void setColor(World world, TilePosc pos, DyeColor color) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockAndMetadataRaw(x, y, z, paintedBlock.id(), meta);
        world.setBlockMetadata(x, y, z, meta);
        paintedBlock.getLogic().setColor(world, x, y, z, color);
        if ((meta & 8) != 0) {
            world.scheduleBlockUpdate(x, y, z, paintedBlock.id(), this.tickDelay());
        }

    }
}
