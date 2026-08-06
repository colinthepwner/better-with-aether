package teamport.aether.mixin.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGenericJar;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityFlowerJar;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.useless.dragonfly.models.block.StaticBlockModel;
import teamport.aether.block.AetherBlockTags;

/// A jar holding an Aether plant should sit in Aether dirt rather than overworld dirt.
///
/// 8.0 turned the jar's dirt into a whole DragonFly model (`minecraft:block/jar_dirt`) chosen by
/// `getModelFromData`, where 7.3 swapped a single `IconCoordinate`. `getModelFromData` only sees the
/// metadata, and which plant is in the jar lives on the tile entity, so the override has to happen a
/// level up at `getModel`. `BlockModelGenericJar` does not declare `getModel` — a mixin cannot inject
/// into an inherited method, so this *adds* the override to the target class instead.
@Environment(EnvType.CLIENT)
@Mixin(BlockModelGenericJar.class)
public abstract class BlockModelGenericJarMixinAetherDirt {

    /// Resolved on first use, not in an initialiser: model data is not loaded yet when the block
    /// model classes are first touched, and `loadDataModel` fails silently rather than throwing.
    @Unique
    private static StaticBlockModel better_with_aether$jarAetherDirt;

    @Shadow
    public abstract StaticBlockModel getModelFromData(int data);

    public StaticBlockModel getModel(WorldSource source, TilePosc pos) {
        int data = source.getBlockData(pos);
        if (data == 1 && better_with_aether$isAetherPlant(source, pos)) {
            if (better_with_aether$jarAetherDirt == null) {
                better_with_aether$jarAetherDirt =
                    BlockModelDispatcher.loadDataModel("aether:block/jar_aether_dirt").asModel();
            }
            return better_with_aether$jarAetherDirt;
        }
        return this.getModelFromData(data);
    }

    @Unique
    private static boolean better_with_aether$isAetherPlant(WorldSource source, TilePosc pos) {
        TileEntity tileEntity = source.getTileEntity(pos.x(), pos.y(), pos.z());
        if (!(tileEntity instanceof TileEntityFlowerJar jar)) return false;
        Block<?> planted = Blocks.getBlock(jar.flowerInPot);
        return planted != null && planted.hasTag(AetherBlockTags.PLANTABLE_IN_AETHER_JAR);
    }
}
