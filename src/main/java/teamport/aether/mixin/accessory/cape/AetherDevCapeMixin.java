package teamport.aether.mixin.accessory.cape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.AetherGlobals;

@Environment(EnvType.CLIENT)
@Mixin(MobRendererPlayer.class)
public abstract class AetherDevCapeMixin {
    // 8.0 threads the tessellator through rendering and passes the interpolated position instead of
    // a partial tick, so the handler mirrors the new descriptor. Spelled out in full rather than
    // wildcarded because MobRendererPlayer also carries a synthetic renderSpecials(.., Mob, ..) bridge.
    @SuppressWarnings("java:S131")
    @Inject(method = "renderSpecials(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/player/Player;DDD)V", at = @At("HEAD"))
    private void injectCapeOverride(TessellatorGeneral tessellator, Player player, double x, double y, double z, CallbackInfo ci) {
        switch (player.uuid.toString()) {
            case AetherGlobals.UUID_LUKEISSTUFF: // LukeisStuff
            case AetherGlobals.UUID_OLYPOLYU: // Olypolyu / Kheprep
            case AetherGlobals.UUID_TOCININ: // Tocinin
            case AetherGlobals.UUID_REDART15: // Redart15
            case AetherGlobals.UUID_SMUSHYTACO: // SmushyTaco
            case AetherGlobals.UUID_TOUFOUMASTER: //ToufouMaster
            case AetherGlobals.UUID_RIN: //Rin
                player.capeURL = "https://raw.githubusercontent.com/bta-team-port/better-with-aether/refs/heads/7.3/src/main/resources/assets/aether/textures/armor/cape/aether.png";
                break;
        }
    }
}
