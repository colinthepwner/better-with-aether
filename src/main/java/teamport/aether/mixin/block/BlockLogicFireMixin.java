package teamport.aether.mixin.block;

import net.minecraft.core.world.pos.TilePosc;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicFire;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import teamport.aether.world.AetherDimension;

@Mixin(value = BlockLogicFire.class)
public abstract class BlockLogicFireMixin extends BlockLogic {
    protected BlockLogicFireMixin(Block<?> block, Material material) {
        super(block, material);
    }

    @WrapMethod(method = "onPlacedByWorld")
    private void onBlockPlacedByWorld(World world, TilePosc pos, Operation<Void> original) {
    	int x = pos.x();
    	int y = pos.y();
    	int z = pos.z();
        if (world.dimension == AetherDimension.getAether()) {
            Block<?> below = world.getBlock(x, y - 1, z);
            boolean infiniteBurn = below != null && below.hasTag(BlockTags.INFINITE_BURN);

            if (!infiniteBurn) {
                world.setBlock(x, y, z, 0);
                return;
            }
        }

        original.call(world, pos);
    }
}
