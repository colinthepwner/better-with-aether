package teamport.aether.mixin.dimension;

import net.minecraft.core.world.Dimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.world.AetherDimension;

/// Hooks the Aether's dimension registration onto the tail of vanilla's, so it always lands after
/// the Overworld, Nether and Drift. See `AetherDimension.registerAetherDimension` for why the order
/// matters in 8.0.
@Mixin(value = Dimension.class)
public abstract class DimensionMixinRegisterAether {
    @Inject(method = "init", at = @At("TAIL"))
    private static void registerAether(CallbackInfo ci) {
        AetherDimension.registerAetherDimension();
    }
}
