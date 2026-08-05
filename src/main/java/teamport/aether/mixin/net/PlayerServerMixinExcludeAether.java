package teamport.aether.mixin.net;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.world.Dimension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.world.AetherDimension;

@Environment(EnvType.SERVER)
@Mixin(value = PlayerServer.class)
public abstract class PlayerServerMixinExcludeAether {
    @Shadow
    public MinecraftServer mcServer;

    /// Gates portal travel into the Aether behind `allow-aether`, riding on the checks vanilla runs
    /// for the Nether and Drift.
    ///
    /// 8.0 moved this whole block out of `onUpdateEntity` and into `onLivingUpdate`, which is why
    /// the selectors changed. The surrounding shape is unchanged --
    ///     `netherAllowed && dim == NETHER || driftAllowed && dim == DRIFT
    ///      || dim != NETHER && dim != DRIFT`
    /// -- so an Aether portal falls through the final clause and would otherwise always be allowed.
    @Definition(id = "driftAllowed", local = @Local(type = boolean.class, ordinal = 1))
    @Expression("driftAllowed")
    @ModifyExpressionValue(method = "onLivingUpdate", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean checkForValidDimensionDenyAether(boolean original, @Share("deny") LocalBooleanRef deny, @Local Dimension targetDim) {

        deny.set(mcServer.propertyManager.getBooleanProperty("allow-aether", true) && targetDim == AetherDimension.getAether());
        return original || deny.get();
    }

    @Definition(id = "DRIFT", field = "Lnet/minecraft/core/world/Dimension;DRIFT:Lnet/minecraft/core/world/Dimension;")
    @Expression("? == DRIFT")
    @ModifyExpressionValue(method = "onLivingUpdate", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private boolean checkForValidDimensionFirstHalf(boolean original, @Share("deny") LocalBooleanRef deny, @Local(ordinal = 1) boolean driftAllowed) {
        return original && driftAllowed || deny.get();
    }

    // ordinal 0, not 1: 8.0 leaves a single `!= DRIFT` comparison in the fall-through clause.
    @Definition(id = "DRIFT", field = "Lnet/minecraft/core/world/Dimension;DRIFT:Lnet/minecraft/core/world/Dimension;")
    @Expression("? != DRIFT")
    @ModifyExpressionValue(method = "onLivingUpdate", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private boolean checkForValidDimensionSecondHalf(boolean original, @Local Dimension targetDim) {
        return original && targetDim != AetherDimension.getAether();
    }

}
