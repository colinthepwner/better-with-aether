package teamport.aether.world.chunk;

import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.LargeFeature;
import net.minecraft.core.world.generate.chunk.ChunkGeneratorResult;

public class MapGenCavesAether extends LargeFeature {
    @Override
    public void doGeneration(World world, java.util.Random random, int chunkX, int chunkZ, int x, int z, ChunkGeneratorResult result) {
        // The Aether doesn't have caves.
    }
}
