package teamport.aether.mixin.accessory;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.InventoryAction;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IArmorItem;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemQuiver;
import net.minecraft.core.item.ItemQuiverEndless;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.player.inventory.container.ContainerCrafting;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import org.objectweb.asm.Opcodes;
import net.minecraft.core.player.inventory.menu.MenuInventory;
import net.minecraft.core.player.inventory.slot.Slot;
import net.minecraft.core.player.inventory.slot.SlotArmor;
import net.minecraft.core.player.inventory.slot.SlotResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import teamport.aether.AetherMod;
import teamport.aether.item.AetherItemTags;
import teamport.aether.item.accessory.IAccessory;
import teamport.aether.item.accessory.ItemAccessoryArmor;
import teamport.aether.item.accessory.SlotAccessory;
import teamport.aether.mixin.accessors.MenuAbstractAccessor;
import teamport.aether.mixin.accessors.SlotAccessor;
import teamport.aether.mixin.accessors.SlotArmorAccessor;

import java.util.ArrayList;
import java.util.List;

import static teamport.aether.item.accessory.SlotAccessory.*;

@Mixin(value = MenuInventory.class)
public abstract class MenuInventoryMixinAddSlotAdjSlot {
    @Shadow
    public ContainerInventory inventory;
    /// Keeps vanilla's armour-slot loop at the four shapes it actually has.
    ///
    /// `ContainerInventoryMixinAccessory` widens `ARMOR_INVENTORY_SIZE` to 8 so the backing array,
    /// the container size and the save bounds all cover the accessory slots. 8.0's `MenuInventory`
    /// constructor reads that same constant to decide how many `SlotArmor`s to build, and pairs each
    /// one with `HumanArmorShape.values()[i]` -- an enum with exactly four entries -- so the widened
    /// value walks straight off the end of it. Vanilla builds its four; the accessory slots are
    /// added separately below.
    @ModifyExpressionValue(method = "<init>(Lnet/minecraft/core/player/inventory/container/ContainerInventory;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/core/player/inventory/container/ContainerInventory;ARMOR_INVENTORY_SIZE:I", opcode = Opcodes.GETSTATIC))
    private int onlyVanillaArmorShapes(int original) {
        return HumanArmorShape.values().length;
    }

    @Inject(method = "<init>(Lnet/minecraft/core/player/inventory/container/ContainerInventory;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/player/inventory/menu/MenuInventory;slotsChanged(Lnet/minecraft/core/player/inventory/container/Container;)V"))
    private void addingAndAdjustingSlots(ContainerInventory inventory, CallbackInfo ci) {
        MenuInventory menu = (MenuInventory) (Object) this;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            Container contain = slot.getContainer();
            // fixing the crafting inventory
            if (contain instanceof ContainerCrafting) {
                slot.x += 12;
            }
            if (slot instanceof SlotResult) {
                slot.x += 9;
            }
            //because getContainerSize now returns 44, both slot and index need to be adjusted for armor slot to work.
            if (slot instanceof SlotArmor) {
                SlotArmor newArmorSlot = new SlotArmor(menu, slot.getContainer(), ((SlotAccessor) slot).getSlot() - 4, slot.x, slot.y, ((SlotArmorAccessor) slot).getArmorShape());
                newArmorSlot.index = i;
                menu.slots.set(menu.slots.indexOf(slot), newArmorSlot);
            }
        }
        // adding new accessories
        for (int i = 0; i < 4; ++i) {
            int armorPiece = 4 + i;
            // staring where armor ends
            ((MenuAbstractAccessor) menu).invokeAddSlot(new SlotAccessory(menu, inventory, inventory.getContainerSize() - 4 + i, 80, 8 + i * 18, armorPiece));
        }
    }
    /**
     * in the MAIN inventory (not including armor or crafting slots)
     * IDK what target does, but it always seems to be 0 for me - Colin
     * <br>
     * <br>
     * So the target is 0 because the ScreenContainerAbstract is not resolving the targeting correctly
     * as such the target is always the inventory. ScreenContainerAbstractMixinTargetAccessory
     * alters the target to be always 2 for accessories - Redart15
     */
    @ModifyReturnValue(method = "getTargetSlots", at = @At("RETURN"))
    private it.unimi.dsi.fastutil.ints.IntList accessoryTargets(it.unimi.dsi.fastutil.ints.IntList original, InventoryAction action, Slot slot, int target, Player player) {
        if (slot.index < 9 || slot.index > 44 || target == 1 || slot.getItemStack() == null || !(slot.getItemStack().getItem() instanceof IAccessory || slot.getItemStack().getItem().hasTag(AetherItemTags.TRINKET))) {
            return original;
        }
        Item accessory = slot.getItemStack().getItem();
        it.unimi.dsi.fastutil.ints.IntList ints = new it.unimi.dsi.fastutil.ints.IntArrayList();
        if (accessory instanceof ItemAccessoryArmor) {
            ints.add(AetherMod.ARMOR_START_INDEX + ((ItemAccessoryArmor) accessory).getSlotID());
        }
        if (accessory.hasTag(AetherItemTags.TRINKET)) {
            ints.add(AetherMod.ARMOR_START_INDEX + TRINKET_1_SLOT);
            ints.add(AetherMod.ARMOR_START_INDEX + TRINKET_2_SLOT);
        }
        return ints;
    }
    // allow quiver to be shift clicked in either the body or the cape slot
    @ModifyReturnValue(method = "getTargetSlots", at = @At("RETURN"))
    private it.unimi.dsi.fastutil.ints.IntList quiverTarget(it.unimi.dsi.fastutil.ints.IntList original, InventoryAction action, Slot slot, int target, Player player) {
        if (slot.getItemStack() != null && slot.getItemStack().getItem() instanceof IArmorItem) {
            Item armorItem = slot.getItemStack().getItem();
            if (armorItem instanceof ItemQuiver || armorItem instanceof ItemQuiverEndless) {
                if (original == null) {
                    original = new it.unimi.dsi.fastutil.ints.IntArrayList();
                } else {
                    original = new it.unimi.dsi.fastutil.ints.IntArrayList(original);
                }
                original.add(AetherMod.ARMOR_START_INDEX + CAPE_SLOT);
                return original;
            }
        }
        return original;
    }
}
