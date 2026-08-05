package teamport.aether.mixin.block;

import net.minecraft.core.world.pos.TilePosc;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.block.BlockLogicSpikes;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.entity.AetherMobOtherImmunities;

@Mixin(value = BlockLogicSpikes.class)
public abstract class SpikeImmunitiesMixin {
    @ModifyExpressionValue(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/block/BlockLogicSpikes;isSpikesUp(I)Z"))
    private boolean monsterImmuneToSpikes(boolean original, World world, TilePosc pos, Entity entity) {
    	int x = pos.x();
    	int y = pos.y();
    	int z = pos.z();
        if (!(entity instanceof AetherMobOtherImmunities)) return original;
        AetherMobOtherImmunities immune = (AetherMobOtherImmunities) entity;
        if (immune.canTakeDamageFromSpikes()) return original;
        return false;
    }
}
