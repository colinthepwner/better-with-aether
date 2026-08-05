package teamport.aether.mixin.accessory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.nbt.tags.ListTag;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import teamport.aether.item.accessory.IAccessoryEffects;

@Mixin(value = ContainerInventory.class)
public abstract class ContainerInventoryMixinAccessory {
    @Shadow
    public Player player;
    @Shadow
    public ItemStack[] armorInventory;
    @Shadow
    public ItemStack[] mainInventory;

    @Mutable
    @Shadow
    public static int ARMOR_INVENTORY_SIZE;

    /// Widens the armour inventory by the four accessory slots.
    ///
    /// 7.3 hardcoded `4` in three separate places -- the constructor's array allocation, the bounds
    /// check in `load`, and `getContainerSize` -- so this needed three hooks that each had to be
    /// kept in step. 8.0 derives all three from a single `ARMOR_INVENTORY_SIZE`, initialised in
    /// `<clinit>` to `HumanArmorShape.values().length`. Widening that one field at the tail of
    /// `<clinit>` sizes the array, the save bounds and the container size together, so the other
    /// two hooks are gone rather than ported.
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void expandArmorInventoryForAccessories(CallbackInfo ci) {
        ARMOR_INVENTORY_SIZE += 4;
    }

    @Inject(method = "decrementAnimations", at = @At("TAIL"))
    private void addArmorAnimations(CallbackInfo ci) {
        ContainerInventory inv = (ContainerInventory) (Object) this;
        for (int slot = 0; slot < inv.armorInventory.length; slot++) {
            if (inv.armorInventory[slot] != null && inv.player.world != null) {
                inv.armorInventory[slot].updateAnimation(inv.player.world, inv.player, slot + inv.mainInventory.length, inv.getCurrentSlot() == slot);
            }
        }
    }
    /**
     * @reason 7.3_04 currently handles left click and drop differently from shift clicking.
     * To guarantee that the effect of the accessories is correctly remove on left click and drop
     * a mixin is needed into removeItem. - Redart15
     */
    @Inject(method = "removeItem", at = @At("HEAD"))
    private void updateEffects(int index, int takeAmount, CallbackInfoReturnable<ItemStack> cir) {
        if (index < this.mainInventory.length) {
            return;
        }
        ItemStack itemStack = this.armorInventory[index - this.mainInventory.length];
        if (itemStack != null && itemStack.getItem() instanceof IAccessoryEffects) {
            ((IAccessoryEffects) itemStack.getItem()).removeEffect(player, itemStack);
        }
    }


    /**
     * @reason 7.3_04 currently handles left click and drop differently from shift clicking.
     * To guarantee that the effect of the accessories is correctly remove on shift clicking
     * a mixin is needed into setItem. - Redart15
     */
    @Inject(method = "setItem", at = @At("HEAD"))
    private void updateEffects(int index, ItemStack newItem, CallbackInfo ci) {
        if (index < this.mainInventory.length) {
            return;
        }

        ItemStack oldItem = this.armorInventory[index - this.mainInventory.length];
        // this is only called when we SWAP an item
        if (oldItem != null && oldItem.getItem() instanceof IAccessoryEffects) {
            ((IAccessoryEffects) oldItem.getItem()).removeEffect(player, oldItem);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    public void activateAccessories(ListTag nbttaglist, CallbackInfo ci) {
        ContainerInventory inv = (ContainerInventory) (Object) this;
        for (ItemStack item : inv.armorInventory) {
            if (item != null && item.getItem() instanceof IAccessoryEffects) {
                ((IAccessoryEffects) item.getItem()).addEffect(inv.player, item);
            }
        }
    }
}
