package teamport.aether.mixin.dimension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.world.settings.WorldConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.world.AetherDimension;

/**
 * Resets the Aether's dimension and world data when a world is created, so a fresh world does not
 * inherit the previous one's state.
 *
 * <p>BTA 8.0 collapsed {@code startWorld(String, String, long, WorldTypeGroups.Group)} into
 * {@link Minecraft#createAndStartWorld(WorldConfiguration)}, which carries the same name, seed and
 * world type in one settings object. That is still the create-a-world path, so this hooks it — the
 * remaining {@code startWorld(String)} in 8.0 is the load-existing path, which should keep its saved
 * data rather than have it reset.
 */
@Environment(EnvType.CLIENT)
@Mixin(value = Minecraft.class)
public class ClearLevelDataMixin {
	@Inject(method = "createAndStartWorld(Lnet/minecraft/core/world/settings/WorldConfiguration;)V", at = @At("HEAD"))
	public void clearData(WorldConfiguration configuration, CallbackInfo ci) {
		AetherDimension.setDimensionDataDefaults();
		AetherDimension.setWorldDataDefaults();
	}
}
