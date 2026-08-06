package teamport.aether.mixin.accessory.trinket;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.entity.IArmorWearing;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IArmorItem;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.util.helper.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.item.accessory.SlotAccessory;

/// Accessories that are armour (gloves, combat/gravitite pendants) contribute damage protection.
///
/// 7.3's `ContainerInventory.getTotalProtectionAmount` looped the whole `armorInventory`, which
/// `ContainerInventoryMixinAccessory` widens from 4 to 8, so the accessory slots were in range; a
/// separate mixin defeated the `getArmorPiece() == i` slot check that would otherwise have skipped
/// them. 8.0 moved the sum to `IArmorWearing.getTotalProtectionAmount` and bounded it by
/// `getNumArmorSlots()`, which `Player` derives from `HumanArmorShape.values().length` — a hard 4.
/// The accessory slots are therefore never visited any more and their protection was silently lost.
///
/// Only players have accessory slots; the other `IArmorWearing` implementors (wolves, armoured
/// monsters, statues) are left alone.
@Mixin(value = IArmorWearing.class)
public interface IArmorWearingMixinAccessoryProtection {

    @ModifyReturnValue(method = "getTotalProtectionAmount", at = @At("RETURN"))
    private float addAccessoryProtection(float original, DamageType damageType) {
        if (!(this instanceof Player player)) return original;

        ItemStack[] armor = player.inventory.armorInventory;
        float extra = 0.0F;
        for (int i = SlotAccessory.GLOVES_SLOT; i < armor.length; i++) {
            ItemStack stack = armor[i];
            if (stack == null || !(stack.getItem() instanceof IArmorItem<?> accessory)) continue;
            ArmorMaterial material = accessory.getArmorMaterial();
            if (material == null) continue;
            extra += material.getProtection(damageType) * accessory.getArmorPieceProtectionPercentage();
        }
        return original + extra;
    }
}
