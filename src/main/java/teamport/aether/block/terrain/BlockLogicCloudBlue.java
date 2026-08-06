package teamport.aether.block.terrain;

import net.minecraft.client.render.particle.Particle;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.phys.AABB;
import org.joml.primitives.AABBdc;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import teamport.aether.achievements.AetherAchievements;
import teamport.aether.helper.ParticleMaker;
import turniplabs.halplibe.helper.EnvironmentHelper;

public class BlockLogicCloudBlue extends BlockLogicCloudBase {
    public BlockLogicCloudBlue(Block<?> block) {
        super(block);
    }

    @Override
    public org.joml.primitives.AABBdc getCollisionAABB(WorldSource world, TilePosc pos) {
		int x = pos.x();
		int y = pos.y();
		int z = pos.z();
        return null;
    }

    @Override
    public void onEntityCollision(World world, TilePosc pos, Entity entity) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();
        //don't reference particles on the server. It will crash.
        if (!EnvironmentHelper.isServerEnvironment()) {
            if (entity instanceof Player) {
                ((Player) entity).addStat(AetherAchievements.BOUNCE, 1);
            }

            if (true) {
                ParticleMaker.spawnParticle(entity.world, "splash", entity.x, entity.y, entity.z, world.rand.nextFloat(), world.rand.nextFloat(), world.rand.nextFloat(), 0);
            }
        }

        entity.fallDistance = 0.0F;
        entity.yd *= 0.005;

        if (!EnvironmentHelper.isServerEnvironment()) {
            if (entity.y > y && !entity.isSneaking() ) {
                this.jump(entity);
            }

        } else {
            if (entity.y > y && !entity.isSneaking()) {
                this.jump(entity);
            }

        }
    }

    public void jump(Entity entity) {
        entity.fallDistance = 0.0F;
        entity.yd = 2.0f;
    }
}
