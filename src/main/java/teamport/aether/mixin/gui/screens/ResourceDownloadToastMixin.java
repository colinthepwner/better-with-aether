package teamport.aether.mixin.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiElementToastsHud;
import net.minecraft.core.lang.I18n;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.AetherRemoteResourceDownloaderThread;
import teamport.aether.helper.MixinHelper;

import static teamport.aether.AetherClient.resourceDownloaderThread;

@Environment(EnvType.CLIENT)
@Mixin(value = GuiElementToastsHud.class)
public abstract class ResourceDownloadToastMixin extends net.minecraft.client.gui.Gui {

    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderResourceToast(float partialTicks, CallbackInfo ci) {
        if (resourceDownloaderThread.getTheState() == AetherRemoteResourceDownloaderThread.State.DOWNLOADING) {
            int screenWidth = mc.resolution.getScaledWidthScreenCoords();
            int padding = 5;
            String message = String.format(
                I18n.getInstance().translateKey("aether.download_resources"),
                resourceDownloaderThread.getProgress().get(), resourceDownloaderThread.getToDownload()
            );
            float progress = (float) Math.sin(((double) ((mc.ticksRan + mc.timer.partialTicks) % MixinHelper.ANIMATION_LENGTH) / MixinHelper.ANIMATION_LENGTH) * Math.PI);
            this.drawStringShadow(
                mc.font,
                message,
                screenWidth - mc.font.stringWidth(message) - padding,
                padding,
                MixinHelper.mixColor(0xFFFFFF, 0xC0C0C0, progress)
            );
        }
    }
}
