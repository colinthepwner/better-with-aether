package teamport.aether.mixin.accessory.cape.invisibility_cape.render;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderGlobal;
import org.joml.primitives.AABBdc;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.core.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import teamport.aether.entity.player.PlayerUtil;

@Environment(EnvType.CLIENT)
@Mixin(value = RenderGlobal.class)
public class HideDebugBoxes {
    // 8.0 passes the box itself in as a second argument rather than deriving it from the entity.
    @WrapMethod(method = "drawInterpolatedEntityBoundingBox")
    private void hideInvisibleBoundingBox(Entity entity, AABBdc box, ICamera camera, float partialTicks, Operation<Void> original) {
        if (PlayerUtil.isInvisible(entity)) return;
        original.call(entity, box, camera, partialTicks);
    }
}
