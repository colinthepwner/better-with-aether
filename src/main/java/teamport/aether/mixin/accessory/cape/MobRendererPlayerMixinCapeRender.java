package teamport.aether.mixin.accessory.cape;

import teamport.aether.util.AetherArmorSlot;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import teamport.aether.compat.model.ModelBase;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.entity.player.PlayerUtil;
import teamport.aether.item.accessory.ItemAccessoryArmor;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class)
public abstract class MobRendererPlayerMixinCapeRender extends MobRenderer<Player> {
    protected MobRendererPlayerMixinCapeRender(float shadowSize) {
        super(shadowSize);
    }

    // Follows the cape into renderAdditional, and onto GLRenderer: 8.0 routes colour through the
    // renderer rather than calling GL11 directly, and there is a single such call in that method, so
    // the old ordinal=4 into a list of raw glColor4f calls no longer has anything to count.
    @WrapOperation(method = "renderAdditional(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/player/Player;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/renderer/GLRenderer;setColor4f(FFFF)V"))
    private void renderPlayerSpecialsInvisible(float red, float green, float blue, float alpha, Operation<Void> original, @Local(argsOnly = true) Player player) {
        if (!PlayerUtil.isInvisible(player)) {
            original.call(red, blue, green, alpha);
            return;
        }
        original.call(red, blue, green, 0.0F);
        GLRenderer.enableState(State.BLEND);
    }

    ///  Afterward we need to restore the GL11 state back so rendering can resume as is
    @Inject(method = "renderSpecials(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/player/Player;DDD)V", at = @At("HEAD"))
    private void pushGL11AlphaTestRef(TessellatorGeneral tessellator, Player player, double d1, double d2, double d3, CallbackInfo ci, @Share("alphaTest") LocalFloatRef alphaTest) {
        alphaTest.set(GLRenderer.getAlphaTest());
        GLRenderer.setAlphaTest(0.0F);
    }

    @Inject(method = "renderSpecials(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/player/Player;DDD)V", at = @At("RETURN"))
    private void popGL11AlphaTestRef(TessellatorGeneral tessellator, Player player, double d1, double d2, double d3, CallbackInfo ci, @Share("alphaTest") LocalFloatRef alphaTest) {
        GLRenderer.setAlphaTest(alphaTest.get());
    }

    // 8.0 moved the gamemode constants out of Gamemode (now a plain final class) into a Gamemodes
    // holder, and renamed them to upper case.
    @Definition(id = "SPECTATOR", field = "Lnet/minecraft/core/player/gamemode/Gamemodes;SPECTATOR:Lnet/minecraft/core/player/gamemode/Gamemode;")
    @Expression("SPECTATOR")
    @ModifyExpressionValue(method = "renderSpecials(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/player/Player;DDD)V", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1))
    private Gamemode renderPlayerInvisNametag(Gamemode original, TessellatorGeneral tessellator, Player player, double d, double d1, double d2) {
        if (PlayerUtil.isInvisible(player)) {
            return player.getGamemode();
        }
        return original;
    }

    // 8.0 moved cape drawing out of renderSpecials and into renderAdditional, which is also where
    // bindDownloadableTexture is now called from. The descriptor is spelled out because
    // renderAdditional carries a synthetic (.., Mob, ..) bridge alongside the Player overload.
    @Definition(id = "bindDownloadableTexture", method = "Lnet/minecraft/client/render/entity/MobRendererPlayer;bindDownloadableTexture(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/client/render/ImageParser;)Z")
    @Expression("? = ?.bindDownloadableTexture(?, ?, ?)")
    @Inject(method = "renderAdditional(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/player/Player;F)V", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void renderAetherCape(TessellatorGeneral tessellator, Player player, float partialTick, CallbackInfo ci, @Local(name = "renderCape") LocalBooleanRef renderCape) {
        ItemStack itemStack = player.inventory.armorItemInSlot(AetherArmorSlot.of(5));
        if (itemStack == null) return;
        if (!(itemStack.getItem() instanceof ItemAccessoryArmor)) return;
        Item item = itemStack.getItem();
        String path = String.format("/assets/%s/textures/armor/cape/%s.png", item.namespaceID.namespace(), ((ItemAccessoryArmor) item).name());
        this.renderDispatcher.textureManager.loadTexture(path).bind();
        renderCape.set(true);
    }
}
