package teamport.aether.mixin.block;

import net.minecraft.core.world.pos.TilePosc;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.block.BlockLogicFluid;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import teamport.aether.block.AetherBlocks;

@Mixin(value = BlockLogicFluid.class)
public abstract class BlockCreatePortalMixin {
    @Inject(method = "onPlacedByWorld", at = @At("HEAD"), cancellable = true)
    private void onPlacedByWorldHead(World world, TilePosc pos, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    	int x = pos.x();
    	int y = pos.y();
    	int z = pos.z();
        if (world.getBlockMaterial(x, y, z) == Materials.WATER && world.getBlockId(x, y - 1, z) == Blocks.GLOWSTONE.id() && AetherBlocks.PORTAL_AETHER.getLogic().tryToCreatePortal(world, new net.minecraft.core.world.pos.TilePos(x, y, z), DyeColor.BLUE)) {
            ci.cancel();
        }
    }

    @Inject(method = "onNeighborChanged", at = @At("HEAD"), cancellable = true)
    private void onNeighborChangedHead(World world, TilePosc pos, net.minecraft.core.block.Block<?> block, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    	int x = pos.x();
    	int y = pos.y();
    	int z = pos.z();
        if (world.getBlockMaterial(x, y, z) == Materials.WATER && world.getBlockId(x, y - 1, z) == Blocks.GLOWSTONE.id() && AetherBlocks.PORTAL_AETHER.getLogic().tryToCreatePortal(world, new net.minecraft.core.world.pos.TilePos(x, y, z), DyeColor.BLUE)) {
            ci.cancel();
        }
    }
}
