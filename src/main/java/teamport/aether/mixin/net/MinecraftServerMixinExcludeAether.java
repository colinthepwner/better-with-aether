package teamport.aether.mixin.net;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.net.PropertyManager;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.type.WorldType;
import net.minecraft.core.world.type.WorldTypeGroups;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import teamport.aether.world.AetherDimension;

@Environment(EnvType.SERVER)
@Mixin(value = MinecraftServer.class)
public abstract class MinecraftServerMixinExcludeAether {
    @Shadow
    public PropertyManager propertyManager;

    /// Gates the Aether behind an `allow-aether` server property, by riding along on the check
    /// vanilla already performs for its own Drift dimension.
    ///
    /// The polarity flipped in 8.0. 7.3 read `if (world.dimension != PARADISE || allowParadise)
    /// { keep }`, so both hooks narrowed a keep-condition with `&&`. 8.0 reads
    /// `if (world.dimension == DRIFT && !allowDrift) { drop }`, a drop-condition -- so the first
    /// hook has to widen the dimension test to also match the Aether, and the second has to answer
    /// the *Aether's* property once it does.
    @Definition(id = "dimension", field = "Lnet/minecraft/server/world/WorldServer;dimension:Lnet/minecraft/core/world/Dimension;")
    @Definition(id = "DRIFT", field = "Lnet/minecraft/core/world/Dimension;DRIFT:Lnet/minecraft/core/world/Dimension;")
    @Expression("?.dimension == DRIFT")
    @ModifyExpressionValue(method = "initWorld", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean disableAetherGenerationOne(boolean original, @Local(ordinal = 1) WorldServer worldServer) {
        return original || worldServer.dimension == AetherDimension.getAether();
    }

    @Definition(id = "getBooleanProperty", method = "Lnet/minecraft/core/net/PropertyManager;getBooleanProperty(Ljava/lang/String;Z)Z")
    @Expression("?.getBooleanProperty('allow-drift', false)")
    @ModifyExpressionValue(method = "initWorld", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean disableAetherGenerationTwo(boolean original, @Local(ordinal = 1) WorldServer worldServer) {
        if (worldServer.dimension == AetherDimension.getAether()) {
            return this.propertyManager.getBooleanProperty("allow-aether", true);
        }
        return original;
    }

    @Definition(id = "dimension", field = "Lnet/minecraft/server/world/WorldServer;dimension:Lnet/minecraft/core/world/Dimension;")
    @Definition(id = "DRIFT", field = "Lnet/minecraft/core/world/Dimension;DRIFT:Lnet/minecraft/core/world/Dimension;")
    @Expression("?.dimension == DRIFT")
    @ModifyExpressionValue(method = "doTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean disableAetherTravelOne(boolean original, @Local WorldServer worldServer) {
        return original || worldServer.dimension == AetherDimension.getAether();
    }

    @Definition(id = "getBooleanProperty", method = "Lnet/minecraft/core/net/PropertyManager;getBooleanProperty(Ljava/lang/String;Z)Z")
    @Expression("?.getBooleanProperty('allow-drift', false)")
    @ModifyExpressionValue(method = "doTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean disableAetherTravelTwo(boolean original, @Local WorldServer worldServer) {
        if (worldServer.dimension == AetherDimension.getAether()) {
            return this.propertyManager.getBooleanProperty("allow-aether", true);
        }
        return original;
    }

    // fixWorldType is deliberately gone. It used to pick a sub-dimension's WorldType out of the
    // matching WorldTypeGroups group, by modifying the WorldType argument of the WorldServerMulti
    // constructor. 8.0 removed WorldServerMulti (sub-dimensions are plain WorldServers now), moved
    // WorldType into WorldConfiguration, and folded the group lookup into the engine as
    // WorldConfiguration.getWorldType(Dimension). WorldTypeGroupsMixin already registers the Aether
    // type into every group, so vanilla now resolves it natively and there is nothing to patch.
}
