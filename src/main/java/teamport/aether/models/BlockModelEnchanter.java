package teamport.aether.models;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelHorizontalRotation;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.helper.Sides;
import net.minecraft.core.world.WorldSource;

@Environment(EnvType.CLIENT)
public class BlockModelEnchanter<T extends BlockLogic> extends BlockModelHorizontalRotation<T> {
    public BlockModelEnchanter(Block<T> block) {
        super(block);
    }

    @Override
    public IconCoordinate getBlockTexture(WorldSource blockAccess, net.minecraft.core.world.pos.TilePosc pos, Side side) {
        int data = blockAccess.getBlockMetadata(pos.x(), pos.y(), pos.z());
        int index = Sides.orientationLookUpHorizontal[6 * Math.min(data, 5) + side.id];
        if (index >= Sides.orientationLookUpHorizontal.length) {
            if (isRetro()) {
                return this.blockTextures.get(Side.BOTTOM);
            }
            return this.blockTextures.get(Side.BOTTOM);
        } else if (index == Side.NORTH.id) {
            IconCoordinate originalFront;
            if (isRetro()) {
                originalFront = this.blockTextures.get(Side.NORTH);
            } else {
                originalFront = this.blockTextures.get(Side.NORTH);
            }
            Container container = (Container) blockAccess.getTileEntity(pos.x(), pos.y(), pos.z());
            if (container != null) {
                boolean hasOutput = container.getItem(2) != null;
                if (hasOutput && originalFront != null) {
                    return TextureRegistry.getTexture(originalFront.namespaceId.namespace() + ":block/" + originalFront.namespaceId.value() + "_filled");
                }
            }

            return originalFront;
        } else {
            if (isRetro()) {
                return this.blockTextures.get(Side.fromId(index));
            }
            return this.blockTextures.get(Side.fromId(index));
        }
    }
}
