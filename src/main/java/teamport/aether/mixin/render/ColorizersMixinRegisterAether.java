package teamport.aether.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.colorizer.Colorizers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.AetherColorizers;

/**
 * {@code registerColorizers} clears the colorizer list before re-adding the vanilla entries, and
 * {@code reload} calls it on every world change and texture pack refresh. Re-add the mod's
 * colorizers here so they are present for the {@code setup}/{@code update} pass that follows.
 */
@Environment(EnvType.CLIENT)
@Mixin(Colorizers.class)
public class ColorizersMixinRegisterAether {

    @Inject(method = "registerColorizers", at = @At("TAIL"))
    private static void registerAetherColorizers(CallbackInfo ci) {
        AetherColorizers.register();
    }

}
