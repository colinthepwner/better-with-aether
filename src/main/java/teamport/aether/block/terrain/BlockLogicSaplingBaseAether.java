package teamport.aether.block.terrain;

import net.minecraft.core.world.pos.TilePosc;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSaplingBase;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.world.World;
import teamport.aether.block.AetherBlockTags;
import teamport.aether.block.AetherBlocks;

import java.util.Random;

public abstract class BlockLogicSaplingBaseAether extends BlockLogicSaplingBase {

    protected BlockLogicSaplingBaseAether(Block<?> block) {
        super(block);
    }

    @Override
    public boolean mayPlaceOn(Block<?> placedOn) {
		int blockId = placedOn == null ? 0 : placedOn.id();
        Block<?> block = Blocks.blocksList[blockId];
        return block != null
            && (block.hasTag(BlockTags.GROWS_FLOWERS)
            || blockId == AetherBlocks.QUICKSOIL.id()
            || blockId == Blocks.SAND.id()
            || block.hasTag(BlockTags.GROWS_TREES)
            || block.hasTag(AetherBlockTags.GROWS_AETHER_FLOWERS)
            || block.hasTag(AetherBlockTags.GROWS_AETHER_TREES)
            || super.mayPlaceOn(placedOn));
    }

    @Override
    public void updateTick(World world, TilePosc pos, Random rand, boolean isRandomTick) {
		int x = pos.x();
		int y = pos.y();
		int z = pos.z();
        if (!world.isClientSide) {
            if (world.getBlockId(x, y - 1, z) == AetherBlocks.QUICKSOIL.id() || world.getBlockId(x, y - 1, z) == Blocks.SAND.id()) {
                world.setBlockWithNotify(x, y, z, AetherBlocks.DEADBUSH_AETHER.id());
            }

            super.updateTick(world, pos, rand, isRandomTick);
            int growthRate = 30;

            if (world.getBlockLightValue(x, y + 1, z) >= 9 && rand.nextInt(growthRate) == 0) {
                int l = world.getBlockMetadata(x, y, z);
                if ((l & 8) == 0) {
                    world.setBlockMetadataWithNotify(x, y, z, l | 8);
                } else {
                    this.growTree(world, pos, rand);
                }
            }

        }
    }

}
