package teamport.aether.mixin.gui;

import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.MenuInventory;
import net.minecraft.core.player.inventory.menu.MenuInventoryCreative;
import net.minecraft.core.util.helper.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.block.AetherBlocks;
import teamport.aether.item.AetherItems;

import java.util.ArrayList;
import java.util.List;

/// Lists every dye variant of the painted skyroot blocks in the creative menu, not just the undyed
/// one.
///
/// 7.3 rewrote the static item list from `<clinit>` and kept a parallel count field in step. Neither
/// survives: 8.0 renamed the list to `creativeContents`, dropped the count in favour of `size()`,
/// and -- the dangerous part -- its `<clinit>` now only assigns an *empty* `ArrayList`, with the
/// contents filled in afterwards. Injecting at `<clinit>` would replace the real menu with an empty
/// list. Expanding from the menu constructor instead guarantees the list is populated by the time
/// this runs, and the guard makes it one-shot so reopening the menu cannot compound the variants.
@Mixin(value = MenuInventoryCreative.class)
public abstract class MenuInventoryAddCreativeItemsMixin extends MenuInventory {
    protected MenuInventoryAddCreativeItemsMixin(ContainerInventory inventory) {
        super(inventory);
    }

    @Unique
    private static boolean aether$expandedPaintedVariants = false;

    @Inject(method = "<init>(Lnet/minecraft/core/player/inventory/container/ContainerInventory;)V", at = @At("TAIL"))
    private void aether$expandPaintedVariants(ContainerInventory inventory, CallbackInfo ci) {
        List<ItemStack> contents = MenuInventoryCreative.creativeContents;
        if (aether$expandedPaintedVariants || contents == null || contents.isEmpty()) {
            return;
        }
        aether$expandedPaintedVariants = true;

        List<ItemStack> expanded = new ArrayList<>();
        for (ItemStack item : contents) {
            if (item.getMetadata() == 0
                && (item.itemID == AetherBlocks.PLANKS_SKYROOT_PAINTED.id() || item.itemID == AetherBlocks.FENCE_PLANKS_SKYROOT_PAINTED.id())) {
                for (DyeColor dyeColor : DyeColor.blockOrderedColors()) {
                    expanded.add(new ItemStack(item.itemID, 1, dyeColor.blockMeta));
                }
            } else if (item.getMetadata() == 0
                && (item.itemID == AetherBlocks.SLAB_PLANKS_SKYROOT_PAINTED.id()
                || item.itemID == AetherBlocks.STAIRS_PLANKS_SKYROOT_PAINTED.id()
                || item.itemID == AetherBlocks.FENCEGATE_PLANKS_SKYROOT_PAINTED.id()
                || item.itemID == AetherBlocks.BUTTON_PLANKS_SKYROOT_PAINTED.id()
                || item.itemID == AetherBlocks.PRESSURE_PLATE_PLANKS_SKYROOT_PAINTED.id()
                || item.itemID == AetherBlocks.CHEST_PLANKS_SKYROOT_PAINTED.id()
                || item.itemID == AetherBlocks.TRAPDOOR_PLANKS_SKYROOT_PAINTED.id()
                || item.itemID == AetherBlocks.CHEST_MIMIC_OAK_PAINTED.id()
                || item.itemID == AetherBlocks.CHEST_MIMIC_SKYROOT_PAINTED.id()
            )) {
                for (DyeColor dyeColor : DyeColor.blockOrderedColors()) {
                    expanded.add(new ItemStack(item.itemID, 1, dyeColor.blockMeta << 4));
                }
            } else if (item.getMetadata() == 0
                && (item.itemID == AetherItems.SIGN_SKYROOT_PAINTED.id || item.itemID == AetherItems.DOOR_SKYROOT_PAINTED.id)
            ) {
                for (DyeColor dyeColor : DyeColor.itemOrderedColors()) {
                    expanded.add(new ItemStack(item.itemID, 1, dyeColor.itemMeta));
                }
            } else {
                expanded.add(item);
            }
        }

        MenuInventoryCreative.creativeContents = expanded;
    }
}
