package teamport.aether.mixin.armor.player.zanite;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.entity.IArmorWearing;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.item.AetherArmorMaterial;

/// Zanite armour's protection decays as the piece wears out, interpolating towards ZANITE_BROKEN.
///
/// This used to sit on `ContainerInventory.getTotalArmorPoints`, which in 7.3 both summed armour
/// points and applied per-damage-type protection. 8.0 split those: `getTotalArmorPoints` now only
/// computes a durability percentage for the armour bar, and the protection sum moved to
/// `IArmorWearing.getTotalProtectionAmount`, a default method on the interface. The wrapped call is
/// the same `ArmorMaterial.getProtection`, so only the host moved.
@Mixin(value = IArmorWearing.class)
public interface IArmorWearingMixinZanite {
    @WrapOperation(method = "getTotalProtectionAmount", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/item/material/ArmorMaterial;getProtection(Lnet/minecraft/core/util/helper/DamageType;)F"))
    private float modifyProtectionAmount(ArmorMaterial instance, DamageType damageType, Operation<Float> original, @Local ItemStack itemStack) {
        if (instance != AetherArmorMaterial.ZANITE) {
            return original.call(instance, damageType);
        }
        float durabilityProgress = (float) itemStack.getMetadata() / instance.durability;
        return MathHelper.lerp(instance.getProtection(damageType), AetherArmorMaterial.ZANITE_BROKEN.getProtection(damageType), durabilityProgress);
    }
}
