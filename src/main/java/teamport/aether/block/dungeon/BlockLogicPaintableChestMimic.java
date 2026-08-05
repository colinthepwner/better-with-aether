package teamport.aether.block.dungeon;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.IPaintable;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;

public class BlockLogicPaintableChestMimic extends BlockLogicChestMimic implements IPaintable {
    protected final Block<? extends BlockLogicPaintedChestMimic> paintedBlock;

    public BlockLogicPaintableChestMimic(Block<?> block, Material material, Block<? extends BlockLogicPaintedChestMimic> paintedBlock) {
        super(block, material);
        this.paintedBlock = paintedBlock;
    }

    @Override
    public boolean canBePainted() {
        return true;
    }

    @Override
    public void setColor(World world, net.minecraft.core.world.pos.TilePosc pos, DyeColor dyeColor) {
        int meta = world.getBlockMetadata(pos.x(), pos.y(), pos.z()); // frank
        world.setBlockAndMetadataRaw(pos.x(), pos.y(), pos.z(), paintedBlock.id(), meta);
        world.setBlockAndMetadataWithNotify(pos.x(), pos.y(), pos.z(), paintedBlock.id(), meta);
        paintedBlock.getLogic().setColor(world, pos, dyeColor); // no frank

    }
}
