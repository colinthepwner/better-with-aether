package teamport.aether.item;

import org.joml.primitives.AABBd;
import teamport.aether.util.HitResults;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.animal.MobCow;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.phys.AABB;
import org.joml.primitives.AABBdc;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import teamport.aether.entity.animal.phow.MobPhow;

import java.util.Objects;
import java.util.Random;

public class ItemBucketSkyrootEmpty extends Item {
    public ItemBucketSkyrootEmpty(String name, String namespaceId, int id) {
        super(name, namespaceId, id);
    }

    @Override
    public ItemStack onUse(ItemStack itemstack, World world, Player entityplayer) {
        double reachDistance = entityplayer.getGamemode().getBlockReachDistance();
        HitResult hitResult = entityplayer.rayCast(reachDistance, 1.0F, true, false, false);
        if (hitResult != null && HitResults.isTile(hitResult)) {
            int i = HitResults.x(hitResult);
            int j = HitResults.y(hitResult);
            int k = HitResults.z(hitResult);
            if (!world.canMineBlock(entityplayer, i, j, k)) {
                return itemstack;
            }

            if (world.getBlockMaterial(i, j, k) == Materials.WATER && world.getBlockMetadata(i, j, k) == 0 && useBucket(entityplayer, new ItemStack(AetherItems.BUCKET_SKYROOT_WATER))) {
                world.setBlockWithNotify(i, j, k, 0);
                entityplayer.swingItem();
            }
        }

        return itemstack;
    }

    @Override
    public boolean useOnEntity(ItemStack itemstack, Player entityplayer, net.minecraft.core.entity.Mob entitymob) {
        if (entitymob instanceof MobCow || entitymob instanceof MobPhow) {
            useBucket(entityplayer, new ItemStack(AetherItems.BUCKET_SKYROOT_MILK));
            return true;
        }
        return false;
    }

    @Override
    public void onUseByActivator(ItemStack itemStack, World world, TileEntityActivator activatorBlock, Random random, net.minecraft.core.world.pos.TilePosc pos, Direction direction, double offX, double offY, double offZ) {
        if (itemStack.stackSize <= 1) {
            int x = pos.x() + direction.offsetX();
            int y = pos.y() + direction.offsetY();
            int z = pos.z() + direction.offsetZ();
            if (world.getBlockMaterial(x, y, z) == Materials.WATER && world.getBlockMetadata(x, y, z) == 0) {
                world.setBlockWithNotify(x, y, z, 0);
                itemStack.itemID = AetherItems.BUCKET_SKYROOT_WATER.id;
            }

            AABBd box = new AABBd(x, y, z, x + 0.5, y + 1.0, z + 0.5);

            boolean hasCow = !world.getEntitiesWithinAABB(MobCow.class, box).isEmpty();
            boolean hasPhow = !world.getEntitiesWithinAABB(MobPhow.class, box).isEmpty();

            if (hasCow || hasPhow) {
                itemStack.itemID = AetherItems.BUCKET_SKYROOT_MILK.id;
            }
        }
    }

    public static boolean useBucket(Player player, ItemStack itemToGive) {
        if (Objects.requireNonNull(player.inventory.getCurrentItem()).stackSize <= 1) {
            player.inventory.setItem(player.inventory.getCurrentSlot(), itemToGive);
            return true;
        } else {
            player.inventory.insertItem(itemToGive, true);
            if (itemToGive.stackSize < 1) {
                player.inventory.getCurrentItem().consumeItem(player);
                return true;
            } else {
                return false;
            }
        }
    }
}
