package teamport.aether.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.core.entity.Entity;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.entity.boss.slider.MobBossSlider;

/// The slider's debug outline is drawn un-grown, because its hitbox is already flush with the block
/// it occupies and the usual outward nudge makes it look wrong.
///
/// 8.0 moved the growth out of `AABB.grow` (that class is gone along with the rest of the old physics
/// types) and into the static `MathHelper.aabbGrow`, which writes into a caller-supplied `AABBd`
/// rather than allocating. Declining the growth therefore means copying the source box into that
/// destination instead of just returning the receiver.
@Environment(EnvType.CLIENT)
@Mixin(value = RenderGlobal.class)
public abstract class RenderSliderBoundingBoxMixin {
    @WrapOperation(method = "drawInterpolatedEntityBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/util/helper/MathHelper;aabbGrow(Lorg/joml/primitives/AABBdc;DDDLorg/joml/primitives/AABBd;)Lorg/joml/primitives/AABBd;"))
    private AABBd undoGrow(AABBdc source, double growX, double growY, double growZ, AABBd dest, Operation<AABBd> original,
                           Entity entity, AABBdc box, ICamera camera, float partialTicks) {
        if (entity instanceof MobBossSlider) {
            dest.setMin(source.minX(), source.minY(), source.minZ());
            dest.setMax(source.maxX(), source.maxY(), source.maxZ());
            return dest;
        }
        return original.call(source, growX, growY, growZ, dest);
    }
}
