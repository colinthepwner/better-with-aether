package teamport.aether.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.entity.animal.MobFireflyCluster;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemJar;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.AetherMod;
import teamport.aether.item.AetherItems;

@Mixin(value = ItemJar.class)
public abstract class ItemJarMixin {
    @org.spongepowered.asm.mixin.injection.Inject(method = "captureFirefly", at = @At("HEAD"), cancellable = true)
    private static void onCaptureFirefly(MobFireflyCluster firefly, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.core.item.Item> cir) {
        if (firefly.getColor() == AetherMod.SILVER) {
            cir.setReturnValue(AetherItems.LANTERN_FIREFLY_SILVER);
        }
    }
}
