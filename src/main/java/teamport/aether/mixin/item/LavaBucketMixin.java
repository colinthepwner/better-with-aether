package teamport.aether.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntityActivator;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemBucket;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.block.AetherBlocks;
import teamport.aether.helper.ParticleMaker;
import teamport.aether.world.AetherDimension;

import java.util.Random;

@Mixin(value = ItemBucket.class)
public abstract class LavaBucketMixin {

    @WrapOperation(method = "tryPlaceFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;setBlockTypeNotify(Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/block/Block;)Z"))
    private boolean aetherPlaceAerogel(World world, TilePosc pos, Block block, Operation<Boolean> original, @Local(argsOnly = true) ItemStack stack) {
        if (world.dimension == AetherDimension.getAether() && ItemBucket.getState(stack).equals(ItemBucket.STATE_LAVA)) {
            boolean placed = original.call(world, pos, AetherBlocks.AEROGEL);
            if (placed) {
                ParticleMaker.spawnParticle(world, "smoke", pos.x() + 0.5, pos.y(), pos.z() + 0.5, 0.0, 0.03, 0.0, 0);
            }
            return placed;
        }
        return original.call(world, pos, block);
    }

    @WrapOperation(method = "tryPlaceFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;playSoundEffect(Lnet/minecraft/core/entity/Entity;Lnet/minecraft/core/sound/SoundCategory;DDDLjava/lang/String;FF)V"))
    private void aetherPlaceAerogelSound(World world, Entity entity, SoundCategory category, double x, double y, double z, String soundPath, float volume, float pitch, Operation<Void> original, @Local(argsOnly = true) ItemStack stack) {
        if (world.dimension == AetherDimension.getAether() && ItemBucket.getState(stack).equals(ItemBucket.STATE_LAVA)) {
            original.call(world, entity, category, x, y, z, "fire.ignite", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
            return;
        }
        original.call(world, entity, category, x, y, z, soundPath, volume, pitch);
    }

    @WrapOperation(method = "onUseByActivator", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;setBlockTypeNotify(Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/block/Block;)Z"))
    private boolean aetherActivatorPlaceAerogel(World world, TilePosc pos, Block block, Operation<Boolean> original, @Local(argsOnly = true) ItemStack stack) {
        if (world.dimension == AetherDimension.getAether() && ItemBucket.getState(stack).equals(ItemBucket.STATE_LAVA)) {
            boolean placed = original.call(world, pos, AetherBlocks.AEROGEL);
            if (placed) {
                ParticleMaker.spawnParticle(world, "smoke", pos.x() + 0.5, pos.y(), pos.z() + 0.5, 0.0, 0.03, 0.0, 0);
            }
            return placed;
        }
        return original.call(world, pos, block);
    }
}
