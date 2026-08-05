package teamport.aether.mixin.accessory.quiver;

import teamport.aether.util.AetherArmorSlot;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.enums.HumanArmorShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Player.class)
public abstract class PlayerMixinNextArrow {
    @WrapOperation(method = "getNextArrow", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/player/Player;getItemInArmorSlot(Lnet/minecraft/core/enums/HumanArmorShape;)Lnet/minecraft/core/item/ItemStack;"))
    private ItemStack checkAdditionalSlots(Player instance, HumanArmorShape shape, Operation<ItemStack> original) {
        ItemStack bodyItem = original.call(instance, shape);
        ItemStack capeItem = instance.inventory.armorItemInSlot(AetherArmorSlot.of(5));
        if (bodyItem == null || (bodyItem.itemID != Items.ARMOR_QUIVER_GOLD.id && 0 >= bodyItem.getMaxDamage() - bodyItem.getMetadata())) {
            return capeItem;
        }
        return bodyItem;
    }
}
