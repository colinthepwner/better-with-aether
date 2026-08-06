package teamport.aether.block.terrain;

import net.minecraft.core.block.Block;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import teamport.aether.item.AetherItems;
import teamport.aether.item.item_tool.ItemToolAxeAether;
import turniplabs.halplibe.helper.EnvironmentHelper;
import net.minecraft.core.world.pos.TilePosc;

public class BlockLogicGoldenLogAether extends BlockLogicLogAether {

    public BlockLogicGoldenLogAether(Block<?> block) {
        super(block);
    }

    @Override
    public void onDestroyedByPlayer(World world, TilePosc pos, Side side, int meta, Player player, Item item) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();
        super.onDestroyedByPlayer(world, pos, side, meta, player, item);
        ItemStack heldItem = player.getHeldItem();
        if (heldItem != null && meta == 0 && player.getGamemode().hasBlockConsumption()) {
            if (!EnvironmentHelper.isClientWorld() && heldItem.getItem() instanceof ItemToolAxeAether) {
                world.dropItem(x, y, z, new ItemStack(AetherItems.AMBER, world.rand.nextInt(3) + 1));
            }
        }
    }

    @Override
    public ItemStack[] getAdditionalBreakResult(World world, Item tool, ItemStack[] results, int meta) {
        ItemStack[] additionalResults = super.getAdditionalBreakResult(world, tool, results, meta);
        ItemStack[] addedAmber = new ItemStack[additionalResults.length + results.length];
        System.arraycopy(additionalResults, 0, addedAmber, 0, additionalResults.length);
        for (int i = 0; i < results.length; i++) {
            addedAmber[additionalResults.length + i] = new ItemStack(AetherItems.AMBER, world.rand.nextInt(3) + 1);
        }
        return addedAmber;
    }
}
