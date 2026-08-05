package teamport.aether.mixin.gui.hud;

import teamport.aether.util.AetherArmorSlot;
import net.minecraft.client.option.GameSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.hud.HudIngame;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.util.helper.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.helper.ClientRenderHelper;
import teamport.aether.helper.MixinHelper;
import teamport.aether.item.AetherItems;

@Environment(EnvType.CLIENT)
@Mixin(value = HudIngame.class)
public abstract class ArmorOverlayMixin extends Gui {
    @Shadow
    protected Minecraft mc;

    /// Draws the equipped accessories down the side of the HUD.
    ///
    /// 7.3 anchored this to the sixth `GL11.glDisable` in `renderGameOverlay`. 8.0 routes GL state
    /// through `GLRenderer.disableState`, so there are no raw `glDisable` calls left to count --
    /// and an ordinal into vanilla's GL calls was brittle anyway. TAIL keeps the accessories drawn
    /// last, on top of the rest of the HUD, without depending on vanilla's internal call sequence.
    @Inject(method = "renderGameOverlay(FZII)V", at = @At("TAIL"))
    private void renderAetherArmour(float partialTicks, boolean flag, int mouseX, int mouseY, CallbackInfo ci) {
        Player player = this.mc.thePlayer;
        ContainerInventory inv = player.inventory;

        int height = this.mc.resolution.getScaledHeightScreenCoords();
        int sp = (int) (GameSettings.SCREEN_PADDING.value * height / 8.0F);

        FontRenderer font = this.mc.font;

        for (int i = 0; i < inv.armorInventory.length - 4; i++) {
            ItemStack stack = inv.armorInventory[inv.armorInventory.length - 1 - i];
            if (stack != null) {
                int x = 2 + 48 + sp;
                int y = height - sp - 16 - i * 16;

                ItemModelDispatcher.getInstance().getDispatch(stack).renderGui(net.minecraft.client.render.renderer.GLRenderer.getTessellator(), this.mc.thePlayer, stack, x, y, (byte) 1, 1.0F);

                if (stack.isItemStackDamageable()) {
                    float durability = (float) (stack.getMaxDamage() - stack.getMetadata()) / (float) stack.getMaxDamage();
                    int l = (int) (durability * 255.0F);
                    int color = 255 - l << 16 | l << 8;

                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    this.drawStringShadow(font, String.valueOf(stack.getMaxDamage() - stack.getMetadata() + 1), x + 20, y + 4, color);
                }
            }
        }
    }

    @Inject(method = "renderGameOverlay(FZII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupScaledResolution()V", shift = At.Shift.AFTER))
    private void renderGameOverlay(float partialTicks, boolean flag, int mouseX, int mouseY, CallbackInfo ci) {
        int width = this.mc.resolution.getScaledWidthScreenCoords();
        int height = this.mc.resolution.getScaledHeightScreenCoords();

        ItemStack trinketOneSlotItem = this.mc.thePlayer.inventory.armorItemInSlot(AetherArmorSlot.of(6));
        ItemStack trinketTwoSlotItem = this.mc.thePlayer.inventory.armorItemInSlot(AetherArmorSlot.of(7));
        double velocity = MathHelper.sqrt(this.mc.thePlayer.xd * this.mc.thePlayer.xd + this.mc.thePlayer.zd * this.mc.thePlayer.zd);

        if (GameSettings.THIRD_PERSON_VIEW.value == 0 &&
            ((trinketOneSlotItem != null && trinketOneSlotItem.itemID == AetherItems.ARMOR_SHIELD_REPULSION.id) ||
                (trinketTwoSlotItem != null && trinketTwoSlotItem.itemID == AetherItems.ARMOR_SHIELD_REPULSION.id)) &&
            (this.mc.thePlayer.isSneaking() ||
                (this.mc.thePlayer.onGround && velocity < 0.075D))) {
            ClientRenderHelper.renderShieldVignette(mc.textureManager, width, height);
        }
    }
}
