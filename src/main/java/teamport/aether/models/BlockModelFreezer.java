package teamport.aether.models;

import net.minecraft.core.world.pos.TilePosc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelHorizontalRotation;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicRotatable;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.helper.Sides;
import net.minecraft.core.world.WorldSource;
import teamport.aether.block.AetherBlocks;

@Environment(EnvType.CLIENT)
public class BlockModelFreezer<T extends BlockLogic> extends BlockModelHorizontalRotation<T> {
    public BlockModelFreezer(Block<T> block) {
        super(block);
    }

    @Override
    public IconCoordinate getBlockTexture(WorldSource blockAccess, net.minecraft.core.world.pos.TilePosc pos, Side side) {
        int data = blockAccess.getBlockMetadata(pos.x(), pos.y(), pos.z());
        int index = Sides.orientationLookUpHorizontal[6 * Math.min(data, 5) + side.id];
        if (index >= Sides.orientationLookUpHorizontal.length) {
            return getTextureWithRetroFallback(Side.BOTTOM);
        }

        Side effectiveSide = Side.fromId(index);
        if (effectiveSide == Side.TOP) {
            IconCoordinate originalTop = getTextureWithRetroFallback(Side.TOP);
            Container container = (Container) blockAccess.getTileEntity(pos.x(), pos.y(), pos.z());
            if (container != null) {
                boolean hasOutput = container.getItem(2) != null;
                if (hasOutput) {
                    String namespace = originalTop.namespaceId.namespace();
                    String path = originalTop.namespaceId.value();
                    return TextureRegistry.getTexture(namespace + ":block/" + path + "_filled");
                }
                if (this.block == AetherBlocks.FREEZER_ACTIVE) {
                    String namespace = originalTop.namespaceId.namespace();
                    String path = originalTop.namespaceId.value();
                    return TextureRegistry.getTexture(namespace + ":block/" + path.replace("idle_top", "active_top"));
                }
            }
            return originalTop;
        } else {
            return getTextureWithRetroFallback(effectiveSide);
        }
    }

    private IconCoordinate getTextureWithRetroFallback(Side side) {
        if (this.blockTextures.hasTexture()) {
            IconCoordinate retroTexture = this.blockTextures.get(side);
            return retroTexture != null ? retroTexture : BLOCK_TEXTURE_UNASSIGNED;
        }
        IconCoordinate standardTexture = this.blockTextures.get(side);
        return standardTexture != null ? standardTexture : BLOCK_TEXTURE_UNASSIGNED;
    }

    @Override
    public boolean render(TessellatorGeneral tessellator, WorldSource world, TilePosc pos) {
		int x = pos.x();
		int y = pos.y();
		int z = pos.z();
        int meta = world.getBlockMetadata(x, y, z);
        Direction direction = BlockLogicRotatable.getDirectionFromMeta(meta);

        switch (direction) {
            case NORTH:
                renderBlocks.uvRotateTop = 3;
                break;
            case EAST:
                renderBlocks.uvRotateTop = 2;
                break;
            case SOUTH:
                renderBlocks.uvRotateTop = 0;
                break;
            case WEST:
                renderBlocks.uvRotateTop = 1;
                break;
            default:
                break;
        }

        boolean result = renderBlocks.renderStandardBlock(tessellator, world, this, this.block.getBounds(), x, y, z);
                return result;
    }
}
