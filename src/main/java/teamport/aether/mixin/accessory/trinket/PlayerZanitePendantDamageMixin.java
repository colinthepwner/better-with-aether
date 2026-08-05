package teamport.aether.mixin.accessory.trinket;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.util.helper.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import teamport.aether.entity.player.PlayerUtil;
import teamport.aether.item.AetherItems;

import static teamport.aether.item.accessory.SlotAccessory.TRINKET_1_SLOT;
import static teamport.aether.item.accessory.SlotAccessory.TRINKET_2_SLOT;

@Mixin(value = Player.class)
public abstract class PlayerZanitePendantDamageMixin {
    @Shadow public ContainerInventory inventory;

    @ModifyVariable(method = "attackTargetEntityWithCurrentItem", at = @At("STORE"), ordinal = 0)
    private int getPendantDamage(int damage) {
        ItemStack trinketOne = inventory.armorInventory[TRINKET_1_SLOT];
        ItemStack trinketTwo = inventory.armorInventory[TRINKET_2_SLOT];
        if (trinketOne != null && trinketOne.itemID == AetherItems.ARMOR_TALISMAN_ZANITE.id) {
            damage = addDamage(damage, trinketOne, TRINKET_1_SLOT);
        }
        if (trinketTwo != null && trinketTwo.itemID == AetherItems.ARMOR_TALISMAN_ZANITE.id) {
            damage = addDamage(damage, trinketTwo, TRINKET_2_SLOT);
        }
        return damage;
    }
    @Unique
    private int addDamage(int damage, ItemStack trinket, int slotID) {
        float damagePercent = (float) trinket.getMetadata() / trinket.getMaxDamage();
        float speed = MathHelper.lerp(0.0F, 3.0F, damagePercent);
        PlayerUtil.damageItemArmor((Player) (Object) this, trinket, slotID);
        damage += (int) Math.floor(speed);
        return damage;
    }
}
