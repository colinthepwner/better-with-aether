package teamport.aether.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.block.Block;
import net.minecraft.core.item.tool.ItemToolShovel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import teamport.aether.block.AetherBlocks;

@Mixin(value = ItemToolShovel.class)
public abstract class ItemToolShovelMixin {
    @Inject(method = "shovelBlock", at = @At("HEAD"), cancellable = true)
    private void shovelAetherDirt(net.minecraft.core.item.ItemStack itemstack, net.minecraft.core.world.World world, net.minecraft.core.entity.player.Player player, net.minecraft.core.world.pos.TilePosc pos, net.minecraft.core.util.helper.Side side, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        Block block = world.getBlockType(pos);
        if (side != net.minecraft.core.util.helper.Side.BOTTOM && world.getBlockType(pos.up(new net.minecraft.core.world.pos.TilePos())) == net.minecraft.core.block.Blocks.AIR && (block == AetherBlocks.GRASS_AETHER || block == AetherBlocks.DIRT_AETHER)) {
            world.playBlockSoundEffect(player, (double)pos.x() + 0.5, (double)pos.y() + 0.5, (double)pos.z() + 0.5, block, net.minecraft.core.enums.EnumBlockSoundEffectType.PLACE);
            if (!world.isClientSide) {
                world.setBlockTypeNotify(pos, AetherBlocks.PATH_DIRT_AETHER);
                itemstack.damageItem(1, player);
            }
            cir.setReturnValue(true);
        }
    }
}
