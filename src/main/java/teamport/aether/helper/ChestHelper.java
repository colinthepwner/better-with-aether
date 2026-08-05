package teamport.aether.helper;

import net.minecraft.core.block.BlockLogicChest;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

/// Null-safe access to a chest's inventory.
///
/// 8.0's `BlockLogicChest.getInventory` opens with `Objects.requireNonNull(world.getTileEntity(pos))`
/// where 7.3 returned null for an empty position. Worldgen asks about positions before it has
/// placed anything there -- `WorldFeatureAetherTreasureChest` inspects a spot to decide whether it
/// is replacing a locked chest -- so calling it directly aborts dungeon generation.
///
/// The check is delegated rather than reimplemented: `getInventory` also resolves double chests by
/// looking at neighbours, and duplicating that here would drift.
public final class ChestHelper {
    private ChestHelper() {
    }

    /// The container at `pos`, or null if nothing is there.
    public static Container inventoryAt(World world, TilePosc pos) {
        if (world.getTileEntity(pos) == null) {
            return null;
        }
        return BlockLogicChest.getInventory(world, pos);
    }
}
