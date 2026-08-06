package teamport.aether.mixin.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.piston.BlockLogicPistonBase;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.block.terrain.BlockLogicOreGravitite;
import teamport.aether.entity.floating_block.EntityFloatingBlock;

/// A steel piston flinging gravitite should launch it as a floating block, not a falling one.
///
/// 7.3 patched `BlockLogicPistonBaseSteel.canPushLine`. Neither that class nor that method exists in
/// 8.0 — `PISTON_BASE_STEEL` runs the shared `BlockLogicPistonBase`, and the fling path is
/// `flingBlock`, reached only when `flingStrength` is not NaN (which is what makes a piston a steel
/// one), so there is no piston-type check to make here.
///
/// `EntityFloatingBlock extends Entity`, not `EntityFallingBlock`, so the constructor call cannot
/// simply be wrapped — the whole body is replaced for gravitite instead, mirroring vanilla's
/// remove-then-fling order.
@Mixin(BlockLogicPistonBase.class)
public abstract class BlockLogicPistonBaseMixinFlingGravitite {

    @Shadow
    @Final
    double flingStrength;

    @Inject(method = "flingBlock", at = @At("HEAD"), cancellable = true)
    private void flingGravititeAsFloatingBlock(World world, TilePosc headPos, Direction dir, CallbackInfo ci) {
        Block<?> block = world.getBlockType(headPos);
        if (block == null || block == Blocks.AIR) return;
        if (!(block.getLogic() instanceof BlockLogicOreGravitite)) return;

        int blockMeta = world.getBlockData(headPos);
        TileEntity tileEntity = world.getTileEntity(headPos);
        world.removeTileEntity(headPos);
        world.setBlockTypeRaw(headPos, Blocks.AIR);
        world.notifyBlocksOfNeighborChange(headPos, Blocks.AIR);

        if (!world.isClientSide) {
            EntityFloatingBlock flung = new EntityFloatingBlock(
                world, headPos.x() + 0.5, headPos.y() + 0.5, headPos.z() + 0.5,
                block.id(), blockMeta, tileEntity);
            flung.setHasRemovedBlock(true);
            world.entityJoinedWorld(flung);
            flung.fling(dir.offsetX() * this.flingStrength,
                dir.offsetY() * this.flingStrength,
                dir.offsetZ() * this.flingStrength, 1.0F);
        }

        ci.cancel();
    }
}
