package teamport.aether.entity;

import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.util.collection.NamespaceID;
import teamport.aether.block.entity.TileEntityEnchanter;
import teamport.aether.block.entity.TileEntityFreezer;
import teamport.aether.block.entity.TileEntityIncubator;
import teamport.aether.block.entity.TileEntityMimic;
import teamport.aether.entity.animal.aerbunny.MobAerbunny;
import teamport.aether.entity.animal.aerwhale.MobAerwhale;
import teamport.aether.entity.animal.moa.MobMoaBlack;
import teamport.aether.entity.animal.moa.MobMoaBlue;
import teamport.aether.entity.animal.moa.MobMoaWhite;
import teamport.aether.entity.animal.phow.MobPhow;
import teamport.aether.entity.animal.phyg.MobPhyg;
import teamport.aether.entity.animal.sheepuff.MobSheepuff;
import teamport.aether.entity.animal.whirly.MobWhirly;
import teamport.aether.entity.boss.slider.MobBossSlider;
import teamport.aether.entity.boss.sunspirit.MobBossSunspirit;
import teamport.aether.entity.boss.valkyrie.queen.MobBossValkyrie;
import teamport.aether.entity.floating_block.EntityFloatingBlock;
import teamport.aether.entity.monster.aechorplant.MobAechorPlant;
import teamport.aether.entity.monster.cockatrice.MobCockatrice;
import teamport.aether.entity.monster.fireminion.MobFireMinion;
import teamport.aether.entity.monster.mimic.MobMimic;
import teamport.aether.entity.monster.sentry.MobSentry;
import teamport.aether.entity.monster.swet.MobSwet;
import teamport.aether.entity.monster.swet.MobSwetGold;
import teamport.aether.entity.monster.tempest.MobTempest;
import teamport.aether.entity.monster.valkyrie.MobValkyrie;
import teamport.aether.entity.monster.zephyr.MobZephyr;
import teamport.aether.entity.projectile.*;
import teamport.aether.entity.vehicle.parachute.EntityParachute;
import teamport.aether.entity.vehicle.parachute.EntityParachuteGold;
import turniplabs.halplibe.helper.EntityHelper;
import net.minecraft.core.block.entity.TileEntityDispatcher;

import static teamport.aether.AetherMod.MOD_ID;

public final class AetherEntities {
    private static boolean hasInit = false;

    public static void init() {
        if (!hasInit) {
            hasInit = true;
            initializeEntities();
        }

    }

    public static void initializeEntities() {
        EntityDispatcher.getInstance().addMapping(MobSentry.class, NamespaceID.getPermanent(MOD_ID, "sentry"), MobSentry::new, "guidebook.section.mob.sentry.name");
        EntityDispatcher.getInstance().addMapping(MobZephyr.class, NamespaceID.getPermanent(MOD_ID, "zephyr"), MobZephyr::new, "guidebook.section.mob.zephyr.name");
        EntityDispatcher.getInstance().addMapping(MobAechorPlant.class, NamespaceID.getPermanent(MOD_ID, "aechorplant"), MobAechorPlant::new, "guidebook.section.mob.aechorplant.name");
        EntityDispatcher.getInstance().addMapping(MobMimic.class, NamespaceID.getPermanent(MOD_ID, "mimic"), MobMimic::new, "guidebook.section.mob.mimic.name");
        EntityDispatcher.getInstance().addMapping(MobSwet.class, NamespaceID.getPermanent(MOD_ID, "swet"), MobSwet::new, "guidebook.section.mob.swet.name");
        EntityDispatcher.getInstance().addMapping(MobSwetGold.class, NamespaceID.getPermanent(MOD_ID, "swet_gold"), MobSwetGold::new, "guidebook.section.mob.swet.gold.name");
        EntityDispatcher.getInstance().addMapping(MobCockatrice.class, NamespaceID.getPermanent(MOD_ID, "cockatrice"), MobCockatrice::new, "guidebook.section.mob.cockatrice.name");
        EntityDispatcher.getInstance().addMapping(MobValkyrie.class, NamespaceID.getPermanent(MOD_ID, "valkyrie"), MobValkyrie::new, "guidebook.section.mob.valkyrie.name");
        EntityDispatcher.getInstance().addMapping(MobWhirly.class, NamespaceID.getPermanent(MOD_ID, "whirly"), MobWhirly::new, "guidebook.section.mob.whirly.name");
        EntityDispatcher.getInstance().addMapping(MobTempest.class, NamespaceID.getPermanent(MOD_ID, "tempest"), MobTempest::new, "guidebook.section.mob.tempest.name");
        EntityDispatcher.getInstance().addMapping(MobFireMinion.class, NamespaceID.getPermanent(MOD_ID, "fire_minion"), MobFireMinion::new, "guidebook.section.mob.fireminion.name");

        EntityDispatcher.getInstance().addMapping(MobBossSlider.class, NamespaceID.getPermanent(MOD_ID, "boss_slider"), MobBossSlider::new, "guidebook.section.mob.slider.name");
        EntityDispatcher.getInstance().addMapping(MobBossValkyrie.class, NamespaceID.getPermanent(MOD_ID, "boss_valkyrie"), MobBossValkyrie::new, "guidebook.section.mob.valkyrie.queen.name");
        EntityDispatcher.getInstance().addMapping(MobBossSunspirit.class, NamespaceID.getPermanent(MOD_ID, "boss_sunspirit"), MobBossSunspirit::new, "guidebook.section.mob.sunspirit.name");

        EntityDispatcher.getInstance().addMapping(MobSheepuff.class, NamespaceID.getPermanent(MOD_ID, "sheepuff"), MobSheepuff::new, "guidebook.section.mob.sheepuff.name");
        EntityDispatcher.getInstance().addMapping(MobPhow.class, NamespaceID.getPermanent(MOD_ID, "phow"), MobPhow::new, "guidebook.section.mob.phow.name");
        EntityDispatcher.getInstance().addMapping(MobPhyg.class, NamespaceID.getPermanent(MOD_ID, "phyg"), MobPhyg::new, "guidebook.section.mob.phyg.name");
        EntityDispatcher.getInstance().addMapping(MobAerwhale.class, NamespaceID.getPermanent(MOD_ID, "aerwhale"), MobAerwhale::new, "guidebook.section.mob.aerwhale.name");
        EntityDispatcher.getInstance().addMapping(MobAerbunny.class, NamespaceID.getPermanent(MOD_ID, "aerbunny"), MobAerbunny::new, "guidebook.section.mob.aerbunny.name");

        EntityDispatcher.getInstance().addMapping(MobMoaBlue.class, NamespaceID.getPermanent(MOD_ID, "moa_blue"), MobMoaBlue::new, "guidebook.section.mob.moa.blue.name");
        EntityDispatcher.getInstance().addMapping(MobMoaWhite.class, NamespaceID.getPermanent(MOD_ID, "moa_white"), MobMoaWhite::new, "guidebook.section.mob.moa.white.name");
        EntityDispatcher.getInstance().addMapping(MobMoaBlack.class, NamespaceID.getPermanent(MOD_ID, "moa_black"), MobMoaBlack::new, "guidebook.section.mob.moa.black.name");


        EntityDispatcher.getInstance().addMapping(EntityParachute.class, NamespaceID.getPermanent(MOD_ID, "parachute"), EntityParachute::new, null);
        EntityDispatcher.getInstance().addMapping(EntityParachuteGold.class, NamespaceID.getPermanent(MOD_ID, "parachute_gold"), EntityParachuteGold::new, null);

        EntityDispatcher.getInstance().addMapping(EntityFloatingBlock.class, NamespaceID.getPermanent(MOD_ID, "floating_block"), EntityFloatingBlock::new, null);


        TileEntityDispatcher.addMapping(TileEntityEnchanter.class, NamespaceID.getPermanent(MOD_ID, "enchanter"));
        TileEntityDispatcher.addMapping(TileEntityFreezer.class, NamespaceID.getPermanent(MOD_ID, "freezer"));
        TileEntityDispatcher.addMapping(TileEntityIncubator.class, NamespaceID.getPermanent(MOD_ID, "incubator"));
        TileEntityDispatcher.addMapping(TileEntityMimic.class, NamespaceID.getPermanent(MOD_ID, "chest_mimic"));

        EntityDispatcher.getInstance().addMapping(ProjectileKnifeLightning.class, NamespaceID.getPermanent(MOD_ID, "knife_lightning"), ProjectileKnifeLightning::new, null);
        EntityDispatcher.getInstance().addMapping(ProjectileDart.class, NamespaceID.getPermanent(MOD_ID, "dart"), ProjectileDart::new, null);
        EntityDispatcher.getInstance().addMapping(ProjectileNeedle.class, NamespaceID.getPermanent(MOD_ID, "needle"), ProjectileNeedle::new, null);
        EntityDispatcher.getInstance().addMapping(ProjectileArrowFlaming.class, NamespaceID.getPermanent(MOD_ID, "arrow_flaming"), ProjectileArrowFlaming::new, null);
        EntityDispatcher.getInstance().addMapping(ProjectileHammerHead.class, NamespaceID.getPermanent(MOD_ID, "hammer_head"), ProjectileHammerHead::new, null);
        EntityDispatcher.getInstance().addMapping(ProjectileWindball.class, NamespaceID.getPermanent(MOD_ID, "windball"), ProjectileWindball::new, null);
        EntityDispatcher.getInstance().addMapping(ProjectileElementFire.class, NamespaceID.getPermanent(MOD_ID, "projectile_fire"), ProjectileElementFire::new, null);
        EntityDispatcher.getInstance().addMapping(ProjectileElementIce.class, NamespaceID.getPermanent(MOD_ID, "projectile_ice"), ProjectileElementIce::new, null);
        EntityDispatcher.getInstance().addMapping(ProjectileElementLightning.class, NamespaceID.getPermanent(MOD_ID, "projectile_lightning"), ProjectileElementLightning::new, null);
    }
}
