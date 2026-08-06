package teamport.aether.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicCobble;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import teamport.aether.item.AetherItems;

import java.util.function.Supplier;
import net.minecraft.core.world.pos.TilePosc;

public class BlockLogicDouble extends BlockLogicCobble {
    public BlockLogicDouble(Block<?> block, Material material, @Nullable Supplier<? extends IItemConvertible> crushResult) {
        super(block, material, crushResult);
    }

    @Override
    public void onPlacedByMob(World world, TilePosc pos, @NonNull Side side, Mob mob, double xPlaced, double yPlaced) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();
        world.setBlockMetadataWithNotify(x, y, z, 1);
    }

    @Override
    public int getPlacedData(@Nullable Player player, ItemStack stack, World world, TilePosc pos, Side side, double xPlaced, double yPlaced) {
        return 1;
    }

    @Override
    public void onDestroyedByPlayer(World world, TilePosc pos, Side side, int meta, Player player, Item item) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();
        ItemStack heldItem = player.getHeldItem();
        if (heldItem != null && heldItem.getItem().equals(AetherItems.TOOL_PICKAXE_SKYROOT) && meta == 0 && player.getGamemode().hasBlockConsumption()) {
            this.harvestBlock(world, player, x, y, z, 1, world.getTileEntity(x, y, z));
        }
    }

}
