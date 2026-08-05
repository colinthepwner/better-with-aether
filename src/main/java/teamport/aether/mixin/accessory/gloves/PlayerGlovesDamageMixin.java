package teamport.aether.mixin.accessory.gloves;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.util.helper.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import teamport.aether.item.AetherArmorMaterial;
import teamport.aether.item.accessory.ItemGloves;

import static teamport.aether.AetherMod.ZANITE_MULTIPLIER;
import static teamport.aether.item.accessory.SlotAccessory.GLOVES_SLOT;

@Mixin(value = Player.class)
public abstract class PlayerGlovesDamageMixin {
    @Shadow public ContainerInventory inventory;

    @ModifyVariable(method = "attackTargetEntityWithCurrentItem", at = @At("STORE"), ordinal = 0)
    private int getGloveDamage(int damage) {
        if (damage > 1) return damage;
        ItemStack stack = inventory.armorInventory[GLOVES_SLOT];
        if (stack == null || !(stack.getItem() instanceof ItemGloves)) {
            return damage;
        }
        ItemGloves gloves = (ItemGloves) stack.getItem();
        ArmorMaterial material = gloves.getArmorMaterial();
        if (material!= null && material == AetherArmorMaterial.ZANITE) {
            float durabilityProgress = (float) stack.getMetadata() / material.durability;
            float endingDamage = gloves.getDamage() * ZANITE_MULTIPLIER;
            return Math.round(MathHelper.lerp(gloves.getDamage(), endingDamage, durabilityProgress));
        }
        return Math.max(gloves.getDamage(), damage);
    }
}
