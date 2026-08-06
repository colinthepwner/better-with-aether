package teamport.aether.block.skyroot;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicPressurePlatePainted;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class BlockLogicPaintedPressurePlate<T extends Entity> extends BlockLogicPressurePlatePainted<T> {
    protected final int unpaintedBlockID;

    public BlockLogicPaintedPressurePlate(Block<?> block, Class<T> mobType, Material material, int unpaintedBlockID) {
        super(block, mobType, material);
        this.unpaintedBlockID = unpaintedBlockID;
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public void removeDye(World world, TilePosc pos) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();
        world.setBlockWithNotify(x, y, z, unpaintedBlockID);
    }
}
