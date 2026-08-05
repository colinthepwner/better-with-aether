package teamport.aether.mixin.accessory;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.container.ScreenInventory;
import net.minecraft.client.gui.container.ScreenInventoryCreative;
import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.client.render.renderer.GLRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.entity.animal.aerbunny.MobAerbunny;

/// Wraps the whole of a player render.
///
/// In 7.3 these three injectors lived in `MobRendererPlayerMixinAccessoryRender`, because
/// `MobRendererPlayer` overrode `render`. 8.0 removed that override, so the only declaration is the
/// one on `MobRenderer` -- and mixin cannot inject into an inherited method. Targeting the
/// superclass and guarding on `Player` keeps the original player-only scope.
@Environment(EnvType.CLIENT)
@Mixin(value = MobRenderer.class)
public abstract class MobRendererMixinPlayerRender<T extends Mob> {

    @SuppressWarnings("java:S107")
    @Inject(method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;DDDFF)V", at = @At("HEAD"))
    private void pushGL11AlphaTestRef(TessellatorGeneral tessellator, T entity, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci, @Share("alphaTest") LocalFloatRef alphaTest) {
        if (!(entity instanceof Player)) return;
        alphaTest.set(GLRenderer.getAlphaTest());
        GLRenderer.setAlphaTest(0.0F);
    }

    @SuppressWarnings("java:S107")
    @Inject(method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;DDDFF)V", at = @At("RETURN"))
    private void popGL11AlphaTestRef(TessellatorGeneral tessellator, T entity, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci, @Share("alphaTest") LocalFloatRef alphaTest) {
        if (!(entity instanceof Player)) return;
        GLRenderer.setAlphaTest(alphaTest.get());
    }

    /// The aerbunny rides on the player's head, but only draws in the inventory preview -- in the
    /// world it renders itself as an ordinary passenger entity.
    @SuppressWarnings("java:S107")
    @Inject(method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;DDDFF)V", at = @At("TAIL"))
    public void renderBunny(TessellatorGeneral tessellator, T entity, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci) {
        if (entity != Minecraft.getMinecraft().thePlayer || !(entity instanceof Player)) return;

        Screen currScreen = Minecraft.getMinecraft().currentScreen;
        final boolean isInInventory = currScreen instanceof ScreenInventory || currScreen instanceof ScreenInventoryCreative;

        if (((Player) entity).passenger instanceof MobAerbunny && isInInventory) {
            MobAerbunny bunny = (MobAerbunny) ((Player) entity).passenger;
            boolean hasHelmet = ((Player) entity).inventory.armorInventory[3] != null;

            GLRenderer.pushFrame();
            GL11.glColor4f(1F, 1F, 1F, 1F);
            GLRenderer.modelM4f().scale(0.80F, 0.80F, 0.80F);
            if (hasHelmet) {
                GLRenderer.modelM4f().translate(0, 0.1875F, 0);
            } else {
                GLRenderer.modelM4f().translate(0, 0.0625F, 0);
            }
            EntityRendererDispatcher.instance.renderEntityWithPosYaw(tessellator, bunny, x, y + 0.25F, z, yaw, partialTick);
            GLRenderer.popFrame();
        }
    }
}
