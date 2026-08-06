package teamport.aether;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.colorizer.Colorizer;
import net.minecraft.client.render.colorizer.Colorizers;

/**
 * Holder for the mod's colorizers.
 * <p>
 * {@link Colorizers#registerColorizers()} clears the colorizer list and re-adds only the vanilla
 * entries, and it runs again on every {@link Colorizers#reload()} (world change, texture pack
 * refresh). Anything added once at startup is therefore dropped before it is ever set up, so
 * {@link Colorizer#isEnabled()} stays false and every lookup returns -1 (white) — which renders the
 * greyscale grass/leaf textures grey. {@code ColorizersMixinRegisterAether} calls {@link #register()}
 * from the tail of that method so the mod's colorizers survive each rebuild.
 * <p>
 * The instances are final and created here rather than in {@link AetherClient} so that a re-register
 * cannot hand a stale object to a {@code BlockColorCustom} dispatch created earlier.
 */
@Environment(EnvType.CLIENT)
public final class AetherColorizers {
    public static final Colorizer GRASS_AETHER = new Colorizer("grassAether");
    public static final Colorizer SKYROOT = new Colorizer("skyroot");
    public static final Colorizer OAK_GOLDEN = new Colorizer("oakGolden");

    private AetherColorizers() {}

    public static void register() {
        Colorizers.add(GRASS_AETHER);
        Colorizers.add(SKYROOT);
        Colorizers.add(OAK_GOLDEN);
    }
}
