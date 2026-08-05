package teamport.aether.mixin.light;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.RenderBlocks;
import net.minecraft.core.block.Block;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.ducks.IBlockAether;

@Environment(EnvType.CLIENT)
@Mixin(value = RenderBlocks.class)
public abstract class RenderBlocksMixin {
    /// 8.0 reshaped `setupLighting` -- it gained a leading `WorldSource`, collapsed the x/y/z ints
    /// into a `TilePosc` and the int side into a `Side`, so mirroring its parameter list would mean
    /// spelling out 21 of them just to reach `block`. Taking only the modified value and pulling the
    /// block out with `@Local` keeps this handler independent of the rest of that signature.
    @ModifyExpressionValue(method = "setupLighting", at = @At(value = "FIELD", target = "Lnet/minecraft/core/block/Block;emission:I", opcode = Opcodes.GETFIELD))
    private int fixAO(int original, @Local(argsOnly = true) Block<?> block) {
        return ((IBlockAether) (Object) block).better_with_aether$getEmissionOverride();
    }
}
