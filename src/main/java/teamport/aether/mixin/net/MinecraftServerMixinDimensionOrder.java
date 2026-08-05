package teamport.aether.mixin.net;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.world.Dimension;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// Guarantees the Overworld is the first dimension `initWorld` walks.
///
/// 8.0's `initWorld` builds the Overworld's `WorldServer` first and then passes it as the parent of
/// every sub-dimension, throwing "Dimension load order issue!" if it meets a sub-dimension before
/// the Overworld exists. It iterates `Dimension.getDimensionList()`, a fastutil hash map, so the
/// order is whatever hashing produces -- vanilla only happens to work because its three ids land
/// favourably. Registering the Aether adds a fourth key and can push id 0 out of first place, which
/// makes this fail for some configured dimension ids and not others.
///
/// Reordering here rather than choosing a luckier id keeps it correct for any value of
/// `AetherConfig.DIMENSION`, including one a user sets by hand.
@Environment(EnvType.SERVER)
@Mixin(value = MinecraftServer.class)
public abstract class MinecraftServerMixinDimensionOrder {
    @WrapOperation(method = "initWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/Dimension;getDimensionList()Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;"))
    private Int2ObjectMap<Dimension> overworldFirst(Operation<Int2ObjectMap<Dimension>> original) {
        Int2ObjectMap<Dimension> source = original.call();

        Int2ObjectLinkedOpenHashMap<Dimension> ordered = new Int2ObjectLinkedOpenHashMap<>();
        Dimension overworld = source.get(Dimension.OVERWORLD.id);
        if (overworld != null) {
            ordered.put(Dimension.OVERWORLD.id, overworld);
        }
        for (Int2ObjectMap.Entry<Dimension> entry : source.int2ObjectEntrySet()) {
            if (entry.getIntKey() != Dimension.OVERWORLD.id) {
                ordered.put(entry.getIntKey(), entry.getValue());
            }
        }
        return ordered;
    }
}
