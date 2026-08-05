package teamport.aether.mixin.item;

import net.minecraft.core.world.pos.TilePosc;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import teamport.aether.helper.MixinHelper;
import teamport.aether.helper.ParticleMaker;
import teamport.aether.world.AetherDimension;
import teamport.aether.world.SunSpiritDeath;
import turniplabs.halplibe.helper.EnvironmentHelper;

import java.util.List;

@Mixin(value = ItemBlock.class)
public abstract class ItemBlockBlacklistMixin {

    @Unique
    private static final int BANNED_BLOCK = -1;
    @Unique
    private static final int REPLACED_BLOCK = -2;

    @Shadow
    @NonNull
    protected Block<?> block;

    @Inject(method = "onUseOnBlock", at = @At("HEAD"), cancellable = true)
    private void banBlocksFromDimensions(ItemStack stack, World world, Player player, TilePosc pos, Side side, double xPlaced, double yPlaced, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (world.dimension != AetherDimension.getAether() && AetherDimension.getDimensionBlacklist(world.dimension).contains(this.block.id())) {
            
            int replacedId = REPLACED_BLOCK;
            if (this.block == Blocks.COBBLE_NETHERRACK_CRYSTALLINE || this.block == Blocks.PUMICE_WET && !SunSpiritDeath.isDead()) {
                replacedId = REPLACED_BLOCK;
            } else {
                replacedId = MixinHelper.BLOCK_TO_BECOME.getOrDefault(this.block.id(), REPLACED_BLOCK);
            }
            
            TilePosc targetPos = pos;
            net.minecraft.core.item.IPlaceable placeable = (net.minecraft.core.item.IPlaceable) (Object) this;
            if (placeable.shouldShiftOutOf(stack, world, player, pos, side, xPlaced, yPlaced)) {
                targetPos = pos.add(side.direction(), new net.minecraft.core.world.pos.TilePos());
            }
            
            if (replacedId == REPLACED_BLOCK || replacedId == BANNED_BLOCK) {
                ParticleMaker.spawnBlockBreakParticles(world, targetPos.x(), targetPos.y(), targetPos.z(), this.block.id());
                cir.setReturnValue(false);
                return;
            }
            
            if (!placeable.canPlaceDirectlyAtPosition(stack, world, player, targetPos, side, xPlaced, yPlaced)) {
                 cir.setReturnValue(false);
                 return;
            }
            
            Block<?> replacementBlock = Blocks.blocksList[replacedId];
            int meta = ((net.minecraft.core.item.IPlaceable.PlaceableBlock<?>) (Object) this).getPlacedData(stack, world, player, targetPos, side, xPlaced, yPlaced);
            
            if (world.setBlockTypeDataRaw(targetPos, replacementBlock, meta)) {
                stack.consumeItem(player);
                if (player == null) {
                    replacementBlock.onPlacedOnSide(world, targetPos, side, xPlaced, yPlaced);
                } else {
                    replacementBlock.onPlacedByMob(world, targetPos, side, player, xPlaced, yPlaced);
                }
                replacementBlock.onPlacedByWorld(world, targetPos);
                world.notifyBlockChange(targetPos, replacementBlock);
                world.playBlockSoundEffect(player, targetPos.x() + 0.5, targetPos.y() + 0.5, targetPos.z() + 0.5, replacementBlock, net.minecraft.core.enums.EnumBlockSoundEffectType.PLACE);
                
                ParticleMaker.spawnReplacementEffects(world, targetPos.x(), targetPos.y(), targetPos.z());
                if (!EnvironmentHelper.isClientWorld()) {
                    world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS, targetPos.x() + 0.5, targetPos.y() + 0.5, targetPos.z() + 0.5, "fire.ignite", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
                    world.playSoundEffect(null, SoundCategory.WORLD_SOUNDS, targetPos.x() + 0.5F, targetPos.y() + 0.5F, targetPos.z() + 0.5F, "random.fizz", 0.5f, 2.6f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8f);
                }
                cir.setReturnValue(true);
                return;
            }
            
            cir.setReturnValue(false);
        }
    }
}
