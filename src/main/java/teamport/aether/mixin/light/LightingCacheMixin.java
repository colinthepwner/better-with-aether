package teamport.aether.mixin.light;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightingCache;
import net.minecraft.core.block.Block;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.ducks.IBlockAether;

/// Lets the carved light blocks emit light without *looking* lit.
///
/// `AetherClient` sets an emission override of 0 on them, so they still light the room from their
/// real `emission` while rendering at the ambient level. `RenderBlocksMixin` covers the same
/// substitution in `setupLighting`.
///
/// 8.0 renamed `RenderBlockCache` to `LightingCache` and split the single `getLightmapCoord` that
/// 7.3 patched into two emission reads -- `calcBrightness` for the shading and `getLightIndex` for
/// the lightmap coordinate -- so both need the override or the block ends up lit through one path
/// and unlit through the other.
@Environment(EnvType.CLIENT)
@Mixin(value = LightingCache.class)
public abstract class LightingCacheMixin {
    @Shadow
    private Block<?> block;

    @ModifyExpressionValue(method = "calcBrightness", at = @At(value = "FIELD", target = "Lnet/minecraft/core/block/Block;emission:I", opcode = Opcodes.GETFIELD))
    private int overrideEmissionForBrightness(int original) {
        return emissionOverride(original);
    }

    @ModifyExpressionValue(method = "getLightIndex", at = @At(value = "FIELD", target = "Lnet/minecraft/core/block/Block;emission:I", opcode = Opcodes.GETFIELD))
    private int overrideEmissionForLightIndex(int original) {
        return emissionOverride(original);
    }

    @org.spongepowered.asm.mixin.Unique
    private int emissionOverride(int original) {
        if (this.block == null) {
            return original;
        }
        return ((IBlockAether) (Object) this.block).better_with_aether$getEmissionOverride();
    }
}
