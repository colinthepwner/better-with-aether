package teamport.aether.mixin.accessory.cape.invisibility_cape.render;

import net.minecraft.client.option.GameSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.entity.player.PlayerUtil;

@Environment(EnvType.CLIENT)
@Mixin(value = ItemModelStandard.class)
public abstract class MakeHeldItemInvisible {
    @Inject(method = "renderSingle", at = @At("HEAD"), cancellable = true)
    private void makeItemInvisible(TessellatorGeneral tessellator, Entity entity, ItemStack itemStack, boolean handheldTransform, byte brightness, int metadata, float partialTicks, boolean gui, CallbackInfo ci) {
        if (entity instanceof Player
            && (entity != Minecraft.getMinecraft().thePlayer || GameSettings.THIRD_PERSON_VIEW.value != 0)
            && PlayerUtil.isInvisible(entity)
        ) {
            ci.cancel();
        }
    }
}
