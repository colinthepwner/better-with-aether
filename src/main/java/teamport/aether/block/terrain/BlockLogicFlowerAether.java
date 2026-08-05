package teamport.aether.block.terrain;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicFlowerStackable;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.tag.BlockTags;
import teamport.aether.block.AetherBlockTags;

public class BlockLogicFlowerAether extends BlockLogicFlowerStackable {
    public BlockLogicFlowerAether(Block<?> block) {
        super(block);
    }

    @Override
    protected boolean mayPlaceOn(Block<?> placedOn) {
		int blockId = placedOn == null ? 0 : placedOn.id();
        Block<?> block = Blocks.blocksList[blockId];
        return block != null
            && (block.hasTag(BlockTags.GROWS_FLOWERS)
            || block.hasTag(AetherBlockTags.GROWS_AETHER_FLOWERS));
    }
}
