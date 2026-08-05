package teamport.aether.models.dungeon;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelRotatable;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicRotatable;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.util.helper.Sides;
import net.minecraft.core.world.WorldSource;
import teamport.aether.world.feature.util.WorldFeaturePoint;

@Environment(EnvType.CLIENT)
public class BlockModelDungeonDoor<T extends BlockLogic> extends BlockModelRotatable<T> {
    private final int width;
    private final int height;

    private final IconCoordinate buffer = new IconCoordinate(TextureRegistry.worldAtlas, null);

    private IconCoordinate particleTexture = TextureRegistry.getTexture("minecraft:block/texture_missing");
    private IconCoordinate particleTextureRetro = TextureRegistry.getTexture("minecraft:block/texture_missing");
    private IconCoordinate particleOverbrightTexture = TextureRegistry.getTexture("minecraft:block/texture_missing");
    private IconCoordinate particleOverbrightTextureRetro = TextureRegistry.getTexture("minecraft:block/texture_missing");

    public BlockModelDungeonDoor(Block<T> block, int width, int height) {
        super(block);
        this.width = width;
        this.height = height;
    }

    protected IconCoordinate ctm(TextureLayer layer, IconCoordinate fallback, WorldSource blockAccess, net.minecraft.core.world.pos.TilePosc pos, Side side) {
        int meta = blockAccess.getBlockMetadata(pos.x(), pos.y(), pos.z());

        Side sideRotated = Side.fromId(Sides.orientationLookUpHorizontal[6 * Math.min(meta & BlockLogicRotatable.MASK_DIRECTION, 5) + side.id]);
        IconCoordinate baseTex = layer.get(sideRotated);

        if (baseTex == null) return fallback;

        Direction dir = BlockLogicRotatable.getDirectionFromMeta(meta);
        Direction offsetLeft = dir.rotateY(-1);
        Direction offsetRight = dir.rotateY(1);

        if (dir == Direction.WEST || dir == Direction.SOUTH) {
            offsetLeft = offsetLeft.opposite();
            offsetRight = offsetRight.opposite();
        }

        boolean up = blockAccess.getBlockId(pos.x(), pos.y() + 1, pos.z()) == block.id();
        boolean down = blockAccess.getBlockId(pos.x(), pos.y() - 1, pos.z()) == block.id();

        boolean right = blockAccess.getBlockId(
            pos.x() + offsetRight.offsetX(),
            pos.y() + offsetRight.offsetY(),
            pos.z() + offsetRight.offsetZ()
        ) == block.id();

        boolean left = blockAccess.getBlockId(
            pos.x() + offsetLeft.offsetX(),
            pos.y() + offsetLeft.offsetY(),
            pos.z() + offsetLeft.offsetZ()
        ) == block.id();

        int u;
        int v;

        if (!up) v = 0;
        else if (!down) v = height - 1;
        else {
            v = 0;
            while (v < height - 1) {
                if (blockAccess.getBlockId(pos.x(), pos.y() + 1 + v, pos.z()) == block.id()) v++;
                else break;
            }
        }

        if (!left) u = 0;
        else if (!right) u = width - 1;
        else {
            u = 0;
            WorldFeaturePoint p = new WorldFeaturePoint(pos.x(), pos.y(), pos.z());
            while (u < width - 1) {
                p = p.moveInDirection(offsetRight).copy();
                if (blockAccess.getBlockId(p.getX(), p.getY(), p.getZ()) == block.id()) u++;
                else break;
            }

            u = width - 1 - u;
        }

        if (side == Side.EAST || side == Side.NORTH) u = width - 1 - u;

        int textWidth = baseTex.width / width;
        int textHeight = baseTex.height / height;

        // buffer.setPosition removed
        buffer.setDimension(textWidth, textHeight);
        return buffer;
    }

    @Override
    public IconCoordinate getBlockTexture(WorldSource blockAccess, net.minecraft.core.world.pos.TilePosc pos, Side side) {
        if (isRetro()) {
            return ctm(this.blockTextures, TextureRegistry.getTexture("minecraft:block/texture_missing"), blockAccess, pos, side);
        }
        return ctm(this.blockTextures, TextureRegistry.getTexture("minecraft:block/texture_missing"), blockAccess, pos, side);
    }

    public IconCoordinate getBlockOverbrightTexture(WorldSource blockAccess, net.minecraft.core.world.pos.TilePosc pos, int side) {
        if (isRetro()) {
            return ctm(this.blockTextures, TextureRegistry.getTexture("minecraft:block/texture_missing"), blockAccess, pos, Side.fromId(side));
        }
        return ctm(this.blockTextures, TextureRegistry.getTexture("minecraft:block/texture_missing"), blockAccess, pos, Side.fromId(side));
    }

    @Override
    public IconCoordinate getBlockTextureFromSideAndMetadata(Side side, int metadata) {
        if (isRetro()) {
            return particleTextureRetro;
        }
        return this.particleTexture;
    }

    public IconCoordinate getBlockOverbrightTextureFromSideAndMeta(Side side, int metadata) {
        if (isRetro()) {
            return particleOverbrightTextureRetro;
        }
        return this.particleOverbrightTexture;
    }

    @Override
    public IconCoordinate getParticleTexture(Side side, int meta) {
        if (isRetro()) return particleTextureRetro;
        return particleTexture;
    }

    public BlockModelDungeonDoor<T> setParticleTexture(boolean isRetro, String texture) {
        if (isRetro) particleTextureRetro = TextureRegistry.getTexture(texture);
        else particleTexture = TextureRegistry.getTexture(texture);
        return this;
    }

    public BlockModelDungeonDoor<T> setParticleOverbrightTexture(boolean isRetro, String texture) {
        if (isRetro) particleOverbrightTextureRetro = TextureRegistry.getTexture(texture);
        else particleOverbrightTexture = TextureRegistry.getTexture(texture);
        return this;
    }
}
