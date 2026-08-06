package teamport.aether.block.skyroot;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicPressurePlate;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class BlockLogicPaintablePressurePlate<T extends Entity> extends BlockLogicPressurePlate<T> {
    protected final Block<? extends BlockLogicPaintedPressurePlate<T>> paintedBlock;

    public BlockLogicPaintablePressurePlate(Block<?> block, Class<T> mobType, Material material, Block<? extends BlockLogicPaintedPressurePlate<T>> paintedBlock) {
        super(block, mobType, material);
        this.paintedBlock = paintedBlock;
    }

    @Override
    public int tickDelay() {
        return 10;
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
        if (isPressed(meta)) {
            world.scheduleBlockUpdate(x, y, z, paintedBlock.id(), this.tickDelay());
        }

    }
}
