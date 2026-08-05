package teamport.aether.mixin.accessors;

import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * BTA 8.0 made {@code IconCoordinate.setPosition} protected. The guidebook pages build sub-icons by
 * carving regions out of a stitched sheet, which still needs to place them.
 */
@Mixin(value = IconCoordinate.class)
public interface IconCoordinateAccessor {
	@Invoker("setPosition")
	void aether$setPosition(int x, int y);
}
