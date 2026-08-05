package teamport.aether.mixin.accessory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.helper.GLManager;

/// Restores GL state after each model layer is drawn, so accessory rendering cannot leak state into
/// the layer that follows.
///
/// 7.3 pinned this to `ModelBase.render(FFFFFF)` at ordinals 2, 5 and 8, because that version
/// unrolled one call per armour layer. 8.0 deleted `ModelBase` outright -- models are DragonFly
/// `StaticEntityModel`s now -- and draws the layers from a loop, so there is a single call site to
/// match. The ordinal is left off deliberately: this attaches to every model draw inside `render`,
/// which is what "restore after each layer" meant in the first place, and it survives the layer
/// count changing again.
@Environment(EnvType.CLIENT)
@Mixin(value = MobRenderer.class)
public abstract class MobRendererGLCleanUpMixin<T extends Mob> {
    @SuppressWarnings("java:S107")
    @Inject(
        method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;DDDFF)V",
        at = @At(value = "INVOKE", target = "Lorg/useless/dragonfly/models/entity/StaticEntityModel;render()V", shift = At.Shift.AFTER))
    private void restoreGLStateAfterModelDraw(TessellatorGeneral tessellator, T entity, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci) {
        GLManager.restore();
    }
}
