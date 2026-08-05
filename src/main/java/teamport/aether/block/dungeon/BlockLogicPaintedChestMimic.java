package teamport.aether.block.dungeon;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.IPainted;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;

public class BlockLogicPaintedChestMimic extends BlockLogicChestMimic implements IPainted {
    protected final int unpaintedBlockID;

    public BlockLogicPaintedChestMimic(Block<?> block, Material material, int unpaintedBlockID) {
        super(block, material);
        this.unpaintedBlockID = unpaintedBlockID;
    }

    @Override
    public void removeDye(World world, net.minecraft.core.world.pos.TilePosc pos) {
        int meta = world.getBlockMetadata(pos.x(), pos.y(), pos.z());
        world.setBlockAndMetadataWithNotify(pos.x(), pos.y(), pos.z(), unpaintedBlockID, meta & -241);
    }

    @Override
    public DyeColor fromMetadata(int meta) {
        return DyeColor.colorFromBlockMeta((meta & 240) >> 4);
    }

    @Override
    public int toMetadata(DyeColor dyeColor) {
        return dyeColor.blockMeta << 4;
    }

    @Override
    public int stripColorFromMetadata(int meta) {
        return meta & -241;
    }

}
