package teamport.aether.agent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.container.ScreenInventory;
import net.minecraft.client.gui.container.ScreenInventoryCreative;
import net.minecraft.client.render.colorizer.Colorizer;
import net.minecraft.client.render.colorizer.Colorizers;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.client.sound.SoundRepository;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.gamemode.Gamemodes;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.settings.WorldConfiguration;
import net.minecraft.core.world.type.WorldTypeGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import teamport.aether.AetherColorizers;
import teamport.aether.block.AetherBlocks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/// Headless-ish smoke driver. A plain launch only proves that startup works -- screens are not
/// constructed until they are opened, so the first keypress is where things actually broke last
/// time. This walks the client through the states a player would reach and, more importantly,
/// prints what the silent-fallback lookups actually return instead of leaving us to infer it from
/// a screenshot.
///
/// Off unless `-Daether.agent=1` is passed. Gradle's own `-D` goes to the daemon, so it has to be
/// handed to the game through a `runs { named("client") { vmArg(...) } }` block.
@Environment(EnvType.CLIENT)
public final class AetherAgent {
    public static final boolean ENABLED = "1".equals(System.getProperty("aether.agent"));
    private static final Logger LOG = LoggerFactory.getLogger("aether-agent");
    private static final String TAG = "[AGENT] ";

    private AetherAgent() {}

    private static int ticks = 0;
    private static int step = 0;
    private static int stepStartedAt = 0;
    private static boolean done = false;

    private static final List<String> FAILURES = new ArrayList<>();

    /// Their renderers call `bindTexture` with a literal path instead of going through
    /// `MobRenderer.loadEntityTexture`, so `getEntityTexture()` is never consulted for them and the
    /// vanilla `basePath` default it returns is never drawn.
    private static final java.util.Set<String> BESPOKE_TEXTURE = new java.util.HashSet<>(java.util.Arrays.asList(
        "aether:parachute", "aether:parachute_gold"));

    /// Drawn by EntityRendererSprite or a bespoke renderer, never by a DragonFly model, so they are
    /// *supposed* to miss `models.json`. Without this list the probe cries wolf on all ten.
    private static final java.util.Set<String> NO_DRAGONFLY_MODEL = new java.util.HashSet<>(java.util.Arrays.asList(
        "aether:floating_block", "aether:knife_lightning", "aether:dart", "aether:needle",
        "aether:arrow_flaming", "aether:hammer_head", "aether:windball",
        "aether:projectile_fire", "aether:projectile_ice", "aether:projectile_lightning"));

    public static void onTick(Minecraft mc) {
        if (!ENABLED || done) return;
        ticks++;
        try {
            drive(mc);
        } catch (Throwable t) {
            fail("driver threw at step " + step, t);
            finish(mc);
        }
    }

    private static void nextStep() {
        step++;
        stepStartedAt = ticks;
    }

    private static int inStep() {
        return ticks - stepStartedAt;
    }

    private static void drive(Minecraft mc) {
        switch (step) {
            case 0: // let the main menu settle so the atlas is stitched and models are loaded
                if (ticks > 60) {
                    probeAssets(mc);
                    nextStep();
                }
                break;

            case 1: // create a world
                LOG.info(TAG + "creating world 'agent-probe'");
                WorldConfiguration cfg = new WorldConfiguration();
                cfg.setWorldName("agent-probe");
                cfg.setStringSeed("aetherprobe");
                cfg.setGamemode(Gamemodes.CREATIVE);
                cfg.setCheatsEnabled(true);
                if (cfg.getWorldTypeGroup() == null) cfg.setWorldTypeGroup(WorldTypeGroups.DEFAULT);
                mc.createAndStartWorld(cfg);
                nextStep();
                break;

            case 2: // wait for the world to actually exist
                if (mc.currentWorld != null && mc.thePlayer != null) {
                    LOG.info(TAG + "world loaded, dimension={}", mc.currentWorld.dimension.id);
                    nextStep();
                } else if (inStep() > 2400) {
                    fail("world never loaded within 2400 ticks", null);
                    nextStep();
                }
                break;

            case 3: // open the survival inventory -- this is where ARMOR_INVENTORY_SIZE widening bites
                if (inStep() > 40) {
                    guard("open ScreenInventory", () -> mc.displayScreen(new ScreenInventory(mc.thePlayer)));
                    nextStep();
                }
                break;

            case 4: // let it render a few frames, then the creative menu
                if (inStep() > 20) {
                    guard("render ScreenInventory", () -> {
                        if (mc.currentScreen != null) mc.currentScreen.tick();
                    });
                    guard("open ScreenInventoryCreative", () -> mc.displayScreen(new ScreenInventoryCreative(mc.thePlayer)));
                    nextStep();
                }
                break;

            case 5:
                if (inStep() > 20) {
                    guard("render ScreenInventoryCreative", () -> {
                        if (mc.currentScreen != null) mc.currentScreen.tick();
                    });
                    mc.displayScreen(null);
                    nextStep();
                }
                break;

            case 6: // into the Aether. Read the source dimension off currentWorld -- startWorld always
                    // opens the Overworld, so the player's own dimension is not what you think.
                if (inStep() > 20) {
                    LOG.info(TAG + "usePortal -> Aether (from dim {})", mc.currentWorld.dimension.id);
                    mc.thePlayer.timeUntilPortal = 200; // or the far side sends us straight back
                    guard("usePortal(3)", () -> mc.usePortal(3, null));
                    nextStep();
                }
                break;

            case 7:
                if (inStep() > 200) {
                    if (mc.currentWorld != null) {
                        LOG.info(TAG + "after portal: dimension={} player=({},{},{})",
                            mc.currentWorld.dimension.id,
                            (int) mc.thePlayer.x, (int) mc.thePlayer.y, (int) mc.thePlayer.z);
                        if (mc.currentWorld.dimension.id != 3) {
                            fail("portal did not land in the Aether, ended in dim " + mc.currentWorld.dimension.id, null);
                        }
                    } else {
                        fail("world went null after usePortal", null);
                    }
                    probeEntityRenderers(mc);
                    nextStep();
                }
                break;

            case 8: // behaviour, in-world, in the Aether
                if (inStep() > 20) {
                    probeColorizers(mc);
                    probeSounds();
                    probeAccessoryIcons();
                    probeEntityTextures(mc);
                    nextStep();
                }
                break;

            case 9:
                if (inStep() > 5) {
                    probeBlockBehaviour(mc);
                    nextStep();
                }
                break;

            case 10: // machine screens are opened one per step so each gets a tick to construct
                if (inStep() > 5) {
                    probeMachineScreens(mc);
                    nextStep();
                }
                break;

            default:
                if (inStep() > 40) finish(mc);
                break;
        }
    }

    // ------------------------------------------------------------------ probes

    /// Everything below is a silent-fallback lookup: `BlockModelDispatcher.loadDataModel` hands back
    /// an empty model for a name it cannot resolve and logs nothing, `TextureRegistry` hands back
    /// the missing-texture icon, DragonFly hands back its fallback model. None of them throw, so the
    /// only way to tell a working lookup from a broken one is to ask.
    private static void probeAssets(Minecraft mc) {
        LOG.info(TAG + "======== ASSET PROBE ========");

        // 1. namespace registry -- the root of texture, sound, lang and entity-geometry discovery
        List<String> ns = new ArrayList<>();
        for (String s : Registries.NAMESPACES) ns.add(s);
        LOG.info(TAG + "NAMESPACES = {}", ns);
        if (!ns.contains("aether")) fail("'aether' is NOT registered in Registries.NAMESPACES", null);

        // 2. translations
        int keys = I18n.getInstance().getCurrentLanguage().keySize();
        LOG.info(TAG + "I18n keys = {}", keys);
        String[] sampleKeys = {
            "dimension.aether.name",
            "tile.aether.portal.aether.name",
            "tile.aether.block.ambrosium.name",
            "item.aether.ambrosium.name",
            "guidebook.section.mob.aerbunny.name",
        };
        for (String k : sampleKeys) {
            String v = I18n.getInstance().translateKey(k);
            LOG.info(TAG + "  lang {} -> {}{}", k, v, v.equals(k) ? "   <<< UNTRANSLATED" : "");
        }

        // 3. textures
        String[] tex = {
            "aether:block/dirt_aether",
            "aether:block/grass_aether/side",
            "aether:block/grass_aether/top",
            "aether:item/ambrosium",
            "aether:item/trinket/armor_pendant_outline",
            "aether:item/trinket/armor_shield_round_outline",
            "aether:gui/hud/protection_holy",
            "aether:block/portal_aether/blue",
        };
        for (String t : tex) {
            boolean src, has;
            try { src = TextureRegistry.hasSourceFile(t); } catch (Throwable e) { src = false; }
            try { has = TextureRegistry.hasTexture(t); } catch (Throwable e) { has = false; }
            LOG.info(TAG + "  tex {} sourceFile={} inAtlas={}{}", t, src, has, has ? "" : "   <<< MISSING");
            if (!has) fail("texture not in atlas: " + t, null);
        }

        // 4. DragonFly entity geometry. modelDataCache is protected static, so reflect.
        try {
            Class<?> cache = Class.forName("org.useless.dragonfly.data.entity.mojang.EntityGeometryMojangData$Cache");
            Field f = cache.getDeclaredField("modelDataCache");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ?> m = (Map<String, ?>) f.get(null);
            TreeSet<String> all = new TreeSet<>(m.keySet());
            List<String> aether = new ArrayList<>();
            for (String k : all) if (k.contains("aether") || k.startsWith("geometry.")) aether.add(k);
            LOG.info(TAG + "DragonFly geometry cache: {} entries total", all.size());
            LOG.info(TAG + "  geometries = {}", aether);

            Method getModel = cache.getMethod("getModel", String.class, double.class);
            Method fallback = cache.getMethod("getFallbackModel");
            Object fb = fallback.invoke(null);
            String[] wanted = {
                "geometry.phyg", "geometry.phow", "geometry.sheepuff.sheared", "geometry.aerbunny",
                "geometry.aerwhale", "geometry.moa", "geometry.zephyr", "geometry.cockatrice",
                "geometry.mimic", "geometry.sentry", "geometry.whirly", "geometry.tempest",
                "geometry.fire_minion", "geometry.aechorplant", "geometry.valkyrie",
                "geometry.slider", "geometry.sunspirit", "geometry.parachute", "geometry.slime",
            };
            for (String w : wanted) {
                Object got;
                try { got = getModel.invoke(null, w, 0.0d); } catch (Throwable e) { got = null; }
                boolean isFb = got == null || got == fb;
                LOG.info(TAG + "  geo {} -> {}{}", w, got == null ? "null" : got.getClass().getSimpleName(),
                    isFb ? "   <<< FALLBACK/NULL" : "");
                if (isFb) fail("DragonFly model unresolved (fallback): " + w, null);
            }
        } catch (Throwable t) {
            fail("could not probe DragonFly geometry cache", t);
        }
        LOG.info(TAG + "======== END ASSET PROBE ========");
    }

    private static void probeEntityRenderers(Minecraft mc) {
        LOG.info(TAG + "======== ENTITY RENDERER PROBE ========");
        try {
            Class<?> aetherEntities = Class.forName("teamport.aether.entity.AetherEntities");
            LOG.info(TAG + "AetherEntities loaded: {}", aetherEntities.getName());
        } catch (Throwable t) {
            fail("AetherEntities not loadable", t);
        }
        // The decisive check. Cache.getModelForEntity keys off the *registered entity id*, not the
        // geometry name, and silently hands back a one-cube fallback when the id is not a key in
        // models.json -- which the renderer then caches forever. A geometry that loads fine can
        // still never reach the mob it belongs to.
        try {
            Class<?> cache = Class.forName("org.useless.dragonfly.data.entity.mojang.EntityGeometryMojangData$Cache");
            Method forEntity = cache.getMethod("getModelForEntity",
                net.minecraft.core.util.collection.NamespaceID.class, String.class);
            // getFallbackModel() builds a fresh instance per call, so `==` proves nothing. It is
            // always the same shape though -- a single 16-cube bone -- so compare visibleBounds.
            Object fb = cache.getMethod("getFallbackModel").invoke(null);
            String fbBounds = String.valueOf(
                org.useless.dragonfly.models.entity.StaticEntityModel.class
                    .getMethod("visibleBounds").invoke(fb));
            LOG.info(TAG + "  fallback-model bounds = {}", fbBounds);
            Method bounds = org.useless.dragonfly.models.entity.StaticEntityModel.class.getMethod("visibleBounds");

            net.minecraft.core.entity.EntityDispatcher d = net.minecraft.core.entity.EntityDispatcher.getInstance();
            for (Map.Entry<Integer, String> e : new java.util.TreeMap<>(d.getEntityIds()).entrySet()) {
                String id = e.getValue();
                if (id == null || !id.startsWith("aether")) continue;
                Object model;
                try {
                    model = forEntity.invoke(null, net.minecraft.core.util.collection.NamespaceID.getPermanent(
                        id.substring(0, id.indexOf(':')), id.substring(id.indexOf(':') + 1)), "main");
                } catch (Throwable t) {
                    model = null;
                }
                String b = "null";
                if (model != null) {
                    try { b = String.valueOf(bounds.invoke(model)); } catch (Throwable t) { b = "?"; }
                }
                boolean isFb = model == null || b.equals(fbBounds);
                boolean expected = NO_DRAGONFLY_MODEL.contains(id);
                LOG.info(TAG + "  entity {} (numeric {}) main-model bounds = {}{}",
                    id, e.getKey(), b,
                    !isFb ? "" : expected ? "   (fallback -- sprite renderer, expected)" : "   <<< FALLBACK CUBE");
                if (isFb && !expected) {
                    fail("entity has no models.json entry, renders as fallback cube: " + id, null);
                }
            }
        } catch (Throwable t) {
            fail("could not probe per-entity models", t);
        }
        LOG.info(TAG + "======== END ENTITY RENDERER PROBE ========");
    }

    // ------------------------------------------------------------------ behaviour probes

    /// Asset probes only prove a lookup resolves. These call the code paths a player triggers,
    /// because the whole class of bug this mod kept hitting -- an override on a signature the engine
    /// no longer dispatches to -- resolves every asset perfectly and still does nothing.

    /// The colorizer returns -1 (white) when it was never set up, and the grass/leaf textures are
    /// greyscale, so "white tint" is exactly what grey grass looks like. Assert a real colour.
    private static void probeColorizers(Minecraft mc) {
        LOG.info(TAG + "======== COLORIZER PROBE ========");
        record Probe(String name, Colorizer colorizer) {}
        Probe[] probes = {
            new Probe("grassAether", AetherColorizers.GRASS_AETHER),
            new Probe("skyroot", AetherColorizers.SKYROOT),
            new Probe("oakGolden", AetherColorizers.OAK_GOLDEN),
        };
        TilePos pos = new TilePos((int) mc.thePlayer.x, (int) mc.thePlayer.y, (int) mc.thePlayer.z);
        for (Probe p : probes) {
            boolean enabled = p.colorizer().isEnabled();
            int world = p.colorizer().getColor(mc.currentWorld, pos);
            int fallback = p.colorizer().getDefaultColor(1.0, 0.7);
            LOG.info(TAG + "  colorizer {} enabled={} worldColor=#{} defaultColor=#{}",
                p.name(), enabled, hex(world), hex(fallback));
            if (!enabled) {
                fail("colorizer never set up (getColor would tint white): " + p.name(), null);
            } else if ((world & 0xFFFFFF) == 0xFFFFFF) {
                fail("colorizer returned white -- greyscale textures will render grey: " + p.name(), null);
            } else if ((world & 0xFFFFFF) == 0x000000) {
                fail("colorizer returned black -- colormap loaded but never updated: " + p.name(), null);
            }
        }
        LOG.info(TAG + "======== END COLORIZER PROBE ========");
    }

    /// `playSoundAtEntity` with an unknown id is a no-op, so a typo'd or unregistered sound is
    /// silent in exactly the way a working-but-quiet one is.
    private static void probeSounds() {
        LOG.info(TAG + "======== SOUND PROBE ========");
        // Taken from the mod's own playSound call sites, not from the .ogg filenames: sounds.json
        // groups the numbered variants under one key (call1.ogg + call2.ogg live under
        // "mob.zephyr.call"), so "mob.zephyr.call1" is not an id and never was.
        String[] ids = {
            "aether:mob.aerbunny.death", "aether:mob.aerbunny.hurt", "aether:mob.aerbunny.land",
            "aether:mob.aerbunny.lift", "aether:mob.aerwhale.call", "aether:mob.moa",
            "aether:mob.slider.awaken", "aether:mob.slider.collide", "aether:mob.slider.death",
            "aether:mob.slider.move", "aether:mob.sunspirit.death", "aether:mob.sunspirit.hurt",
            "aether:mob.sunspirit.talk", "aether:mob.valkyrie.death", "aether:mob.valkyrie.hurt",
            "aether:mob.valkyrie.laugh", "aether:mob.valkyrie.talk", "aether:mob.wingflap",
            "aether:mob.zephyr.call", "aether:mob.zephyr.shoot", "aether:achievement",
            "aether:achievement.bronze", "aether:achievement.silver", "aether:achievement.gold",
            "aether:heal", "aether:life.shard.chime", "aether:life.shard.chime.final",
            "aether:portal", "aether:travel", "aether:trigger", "aether:zap",
        };
        int missing = 0;
        for (String id : ids) {
            // Pass the namespaced id whole: SoundRepository.makeNamespaceFromKey defaults an
            // unqualified key to "minecraft", so stripping the prefix looks up the wrong repository.
            Object event = null;
            try {
                event = SoundRepository.SOUNDS.getSoundEvent(id);
            } catch (Throwable ignored) {
                // fall through to the failure below
            }
            if (event == null) {
                missing++;
                LOG.info(TAG + "  sound {} -> UNRESOLVED", id);
                fail("sound id does not resolve: " + id, null);
            }
        }
        LOG.info(TAG + "  {} of {} aether sound ids resolve", ids.length - missing, ids.length);
        LOG.info(TAG + "======== END SOUND PROBE ========");
    }

    /// The empty-slot outlines are drawn by SlotAccessory; a missing one leaves the slot blank
    /// rather than erroring.
    private static void probeAccessoryIcons() {
        String[] icons = {
            "aether:item/armor_gloves_outline", "aether:item/armor_capes_outline",
            "aether:item/armor_wildcard_outline",
        };
        for (String icon : icons) {
            boolean has;
            try { has = TextureRegistry.hasTexture(icon); } catch (Throwable e) { has = false; }
            LOG.info(TAG + "  accessory icon {} inAtlas={}", icon, has);
            if (!has) fail("accessory slot outline missing: " + icon, null);
        }
    }

    /// Every one of these was silently dead until the legacy-shim overrides were converted, and none
    /// of them logs anything when it does not run.
    private static void probeBlockBehaviour(Minecraft mc) {
        LOG.info(TAG + "======== BLOCK BEHAVIOUR PROBE ========");
        World world = mc.currentWorld;
        Player player = mc.thePlayer;
        int bx = (int) player.x;
        int by = (int) player.y + 3; // clear of the player so placement does not collide
        int bz = (int) player.z + 2;

        // --- getPlacedData: dead override meant these all placed with metadata 0 -----------------
        record Placed(String name, net.minecraft.core.block.Block<?> block, int expected) {}
        Placed[] placed = {
            new Placed("GRASS_AETHER", AetherBlocks.GRASS_AETHER, 1),
            new Placed("DIRT_AETHER", AetherBlocks.DIRT_AETHER, 1),
            new Placed("QUICKSOIL", AetherBlocks.QUICKSOIL, 1),
            new Placed("ORE_AMBROSIUM_HOLYSTONE", AetherBlocks.ORE_AMBROSIUM_HOLYSTONE, 1),
        };
        for (Placed p : placed) {
            TilePos pos = new TilePos(bx, by, bz);
            int data;
            try {
                data = p.block().getPlacedData(player, new ItemStack(p.block()), world, pos, Side.TOP, 0.5, 0.5);
            } catch (Throwable t) {
                fail("getPlacedData threw for " + p.name(), t);
                continue;
            }
            LOG.info(TAG + "  getPlacedData {} -> {} (want {})", p.name(), data, p.expected());
            if (data != p.expected()) {
                fail("getPlacedData returned " + data + " for " + p.name() + ", want " + p.expected()
                    + " -- override is not being dispatched to", null);
            }
        }

        // --- aercloud: white damps the fall, blue bounces ----------------------------------------
        probeCloud(world, player, new TilePos(bx, by, bz), "AERCLOUD_WHITE", AetherBlocks.AERCLOUD_WHITE, false);
        probeCloud(world, player, new TilePos(bx, by, bz), "AERCLOUD_BLUE", AetherBlocks.AERCLOUD_BLUE, true);

        LOG.info(TAG + "======== END BLOCK BEHAVIOUR PROBE ========");
    }

    private static void probeCloud(World world, Player player, TilePos pos, String name,
                                   net.minecraft.core.block.Block<?> cloud, boolean expectBounce) {
        try {
            world.setBlockTypeDataNotify(pos, cloud, 0);
            double before = -1.0;
            player.yd = before;
            player.fallDistance = 42.0F;
            // stand the player on top so BlockLogicCloudBlue's `entity.y > y` bounce test can pass
            double savedY = player.y;
            player.y = pos.y() + 1.0;
            cloud.onEntityCollision(world, pos, player);
            double after = player.yd;
            float fall = player.fallDistance;
            player.y = savedY;
            player.yd = 0.0;
            player.fallDistance = 0.0F;
            world.setBlockTypeDataNotify(pos, Blocks.AIR, 0);

            LOG.info(TAG + "  {} onEntityCollision: yd {} -> {}, fallDistance -> {}", name, before, after, fall);
            if (fall != 0.0F) {
                fail(name + " did not reset fallDistance -- onEntityCollision is not being dispatched to", null);
            }
            if (expectBounce) {
                if (after <= 0.0) fail(name + " did not bounce the entity upward (yd=" + after + ")", null);
            } else if (after <= before) {
                fail(name + " did not damp the fall (yd stayed " + after + ")", null);
            }
        } catch (Throwable t) {
            fail("cloud probe threw for " + name, t);
        }
    }

    /// Right-clicking every machine was a no-op until `onBlockRightClicked` was moved to
    /// `onInteracted`: the block returned nothing and no screen was ever constructed.
    private static void probeMachineScreens(Minecraft mc) {
        LOG.info(TAG + "======== MACHINE SCREEN PROBE ========");
        World world = mc.currentWorld;
        Player player = mc.thePlayer;
        int bx = (int) player.x;
        int by = (int) player.y + 3;
        int bz = (int) player.z + 2;

        record Machine(String name, net.minecraft.core.block.Block<?> block) {}
        Machine[] machines = {
            new Machine("FREEZER_IDLE", AetherBlocks.FREEZER_IDLE),
            new Machine("ENCHANTER_IDLE", AetherBlocks.ENCHANTER_IDLE),
            new Machine("INCUBATOR_IDLE", AetherBlocks.INCUBATOR_IDLE),
        };
        for (Machine m : machines) {
            TilePos pos = new TilePos(bx, by, bz);
            try {
                world.setBlockTypeDataNotify(pos, m.block(), 0);
                mc.displayScreen(null);
                boolean handled = m.block().onInteracted(world, pos, player, Side.TOP, 0.5, 0.5);
                Object screen = mc.currentScreen;
                // Two assertions. `handled` proves dispatch reached the mod at all --
                // BlockLogic.onInteracted returns false by default, so true can only come from the
                // override. The screen proves the body ran to completion rather than falling into
                // the `!world.isClientSide` guard and doing nothing.
                LOG.info(TAG + "  {} onInteracted -> {} screen={}", m.name(), handled,
                    screen == null ? "none" : screen.getClass().getSimpleName());
                if (!handled) {
                    fail(m.name() + ".onInteracted returned false -- right-click does nothing", null);
                } else if (screen == null) {
                    fail(m.name() + ".onInteracted opened no screen", null);
                }
                mc.displayScreen(null);
                world.setBlockTypeDataNotify(pos, Blocks.AIR, 0);
            } catch (Throwable t) {
                fail("machine screen probe threw for " + m.name(), t);
                mc.displayScreen(null);
            }
        }
        LOG.info(TAG + "======== END MACHINE SCREEN PROBE ========");
    }

    /// Resolve the texture path each mob's renderer will actually bind, for every skin variant, and
    /// check the file is really there. `bindTexture` on a missing path silently yields the
    /// missing-texture image or leaves the previous binding in place, so a wrong path looks like
    /// "that mob's texture is broken" and nothing is ever logged. Checked through
    /// `Colorizers.findTexturePackWithFile`, which is the same resource lookup the game uses.
    private static void probeEntityTextures(Minecraft mc) {
        LOG.info(TAG + "======== ENTITY TEXTURE PROBE ========");
        EntityDispatcher dispatcher = EntityDispatcher.getInstance();
        int checked = 0;
        for (Map.Entry<Integer, String> e : new java.util.TreeMap<>(dispatcher.getEntityIds()).entrySet()) {
            String id = e.getValue();
            if (id == null || !id.startsWith("aether")) continue;
            Entity entity;
            try {
                entity = dispatcher.createEntityInWorld(id, mc.currentWorld);
            } catch (Throwable t) {
                continue;
            }
            if (!(entity instanceof Mob mob)) continue;
            if (BESPOKE_TEXTURE.contains(id)) {
                LOG.info(TAG + "  {} (renderer binds its own texture, skipped)", id);
                continue;
            }
            checked++;

            String def = null;
            try { def = mob.getDefaultEntityTexture(); } catch (Throwable ignored) { /* reported below */ }
            checkTexture(id, "default", def);

            // Walk every skin variant the mob will actually cycle through.
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (int i = 0; i < 64; i++) {
                String tex = null;
                try { tex = mob.getEntityTexture(); } catch (Throwable ignored) { /* reported below */ }
                if (tex != null && !seen.add(tex)) break;
                checkTexture(id, "variant" + i, tex);
                boolean moved;
                try { moved = mob.cycleVariant(); } catch (Throwable ignored) { break; }
                if (!moved) break;
            }
        }
        LOG.info(TAG + "  checked {} aether mobs", checked);
        LOG.info(TAG + "======== END ENTITY TEXTURE PROBE ========");
    }

    private static void checkTexture(String id, String which, String path) {
        if (path == null) {
            LOG.info(TAG + "  {} {} -> null (inherits vanilla)", id, which);
            return;
        }
        boolean present;
        try {
            present = Colorizers.findTexturePackWithFile(Minecraft.getMinecraft(), path) != null;
        } catch (Throwable t) {
            present = false;
        }
        // "the file exists" is not the assertion. Mob.basePath defaults to the vanilla player skin
        // directory and is only redirected by setTextureIdentifier(); assigning the
        // textureIdentifier field directly leaves it pointing at char/, which resolves perfectly and
        // draws the player skin on the mob. Assert the resolved value is not that fallback.
        boolean fallback = path.startsWith("/assets/minecraft/textures/entity/char/");
        LOG.info(TAG + "  {} {} -> {}{}{}", id, which, path,
            present ? "" : "   <<< MISSING", fallback ? "   <<< VANILLA PLAYER-SKIN FALLBACK" : "");
        if (!present) {
            fail("entity texture does not exist: " + id + " " + which + " -> " + path, null);
        } else if (fallback) {
            fail("entity texture fell back to the vanilla player skin (basePath never redirected --"
                + " use setTextureIdentifier): " + id + " " + which, null);
        }
    }

    private static String hex(int rgb) {
        return String.format("%06X", rgb & 0xFFFFFF);
    }

    // ------------------------------------------------------------------ plumbing

    private static void guard(String what, Runnable r) {
        try {
            r.run();
            LOG.info(TAG + "ok: {}", what);
        } catch (Throwable t) {
            fail(what, t);
        }
    }

    private static void fail(String what, Throwable t) {
        String msg = what + (t == null ? "" : " :: " + t.getClass().getName() + ": " + t.getMessage());
        FAILURES.add(msg);
        LOG.error(TAG + "FAIL {}", msg, t);
    }

    private static void finish(Minecraft mc) {
        done = true;
        LOG.info(TAG + "======== SUMMARY: {} failure(s) ========", FAILURES.size());
        for (String f : FAILURES) LOG.error(TAG + "  - {}", f);
        LOG.info(TAG + "======== AGENT DONE ========");
        // Quit through the game's own path. Runtime.halt() skips the shutdown hooks and Windows
        // then reports the abandoned GL context as 0xC0000409, which makes every clean run look
        // like a build failure. Read the SUMMARY line above for the actual verdict.
        try {
            mc.shutdown();
        } catch (Throwable t) {
            LOG.warn(TAG + "graceful shutdown failed, halting", t);
            Runtime.getRuntime().halt(FAILURES.isEmpty() ? 0 : 1);
        }
    }
}
