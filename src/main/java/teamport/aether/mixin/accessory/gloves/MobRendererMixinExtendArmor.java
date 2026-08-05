package teamport.aether.mixin.accessory.gloves;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRendererBipedArmored;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// Renders the accessory slots as extra armour layers.
///
/// 7.3 did this by rewriting a hardcoded `4` inside `MobRenderer.render` and reading the layer count
/// off `mc.thePlayer` -- which meant shadowing a `Minecraft mc` field that 8.0's `MobRenderer` no
/// longer has, and applying the local player's inventory size to every mob being drawn. 8.0 pulls
/// that count out into `MobRendererBipedArmored.maxRenderLayer`, so the override goes there instead
/// and can be keyed off the entity actually being rendered.
@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererBipedArmored.class)
public abstract class MobRendererMixinExtendArmor {
    @ModifyReturnValue(method = "maxRenderLayer(Lnet/minecraft/core/entity/Mob;)I", at = @At("RETURN"))
    private int extendLayersForAccessories(int original, Mob entity) {
        if (entity instanceof Player) {
            return ((Player) entity).inventory.armorInventory.length;
        }
        return original;
    }
}
