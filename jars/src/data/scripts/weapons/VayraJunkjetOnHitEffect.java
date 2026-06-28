package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import static data.scripts.plugins.VayraElectroChaffAndJunkjetPlugin.VAYRA_DEBUG;
import data.scripts.plugins.VayraElectroChaffAndJunkjetPlugin;
import static data.scripts.plugins.VayraElectroChaffAndJunkjetPlugin.EMP_ARC_RANGE;
import static data.scripts.plugins.VayraElectroChaffAndJunkjetPlugin.log;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class VayraJunkjetOnHitEffect implements OnHitEffectPlugin {
    
    // colors for EMP arcs (should ideally match the shock beam and other weapon/proj stuff)
    private static Color FRINGE_COLOR = new Color(235, 255, 245, 225);
    private static Color CORE_COLOR = new Color(185, 255, 200, 255);

    private static String EMP_SPECIAL_ID = "vayra_junkjet_missile_emp";
    
    private static final String SOUND_ID = "hit_heavy_energy"; // needs to be a sound ID declared in sounds.json
    private static final float SOUND_PITCH = 1.2f;
    private static final float SOUND_VOL = 0.69f;

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if (projectile == null || engine == null || point == null) {
            return;
        }

        WeaponAPI weapon = projectile.getWeapon();
        ShipAPI ship = projectile.getSource();
        boolean piercedShield = false;

        if (target == null || weapon == null || ship == null) {
            if (VAYRA_DEBUG) {
                log.info("scrapjet hit but null target, source weapon, or source ship -- returning");
            }
            return;
        }

        if (shieldHit && target instanceof ShipAPI) {
            // roll for shield-piercing
            float pierceChance = ((ShipAPI) target).getHardFluxLevel() - 0.1f;
            pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
            piercedShield = (float) Math.random() < pierceChance;
        }

        if (EMP_SPECIAL_ID.equals(projectile.getProjectileSpecId()) && (piercedShield || !shieldHit)) {
            if (VAYRA_DEBUG) {
                log.info("EMP scrapjet direct hit or pierced shield, spawning arcs");
            }
            // cause arcs if shield pierced or not hit
            float dam = projectile.getDamageAmount();
            float emp = projectile.getEmpAmount();
            engine.spawnEmpArcPierceShields(
                    ship, point, target, target,
                    DamageType.ENERGY,
                    dam, // damage
                    emp, // emp 
                    100000f, // max range 
                    "tachyon_lance_emp_impact",
                    35f,
                    FRINGE_COLOR,
                    CORE_COLOR);

            // don't consume chaff unless we hit a living ship (don't wanna waste them on asteroids/hulks)
            if (target instanceof ShipAPI) {
                ShipAPI targetShip = (ShipAPI) target;
                if (!targetShip.isHulk()) {

                    // consume previously existing chaff particles for bonus arcs
                    List<MissileAPI> missiles = CombatUtils.getMissilesWithinRange(target.getLocation(), target.getCollisionRadius() + EMP_ARC_RANGE);
                    for (MissileAPI missile : missiles) {
                        if (missile != null && VayraElectroChaffAndJunkjetPlugin.getChaff().contains(missile) && !VayraElectroChaffAndJunkjetPlugin.getChaffSpent().contains(missile)) {
                            VayraElectroChaffAndJunkjetPlugin.expendChaff(missile, target, true);
                        }
                    }
                }
            }
        }

        // spawn the chaff particles
        int chaff = VayraElectroChaffAndJunkjetPlugin.spawnChaff(projectile.getProjectileSpecId(), weapon, point, target, shieldHit);

        // check if we spawned any chaff
        if (chaff > 0) {

            // if we did, play explosion sound
            Global.getSoundPlayer().playSound(SOUND_ID, SOUND_PITCH, SOUND_VOL, point, new Vector2f(0f, 0f));
        }
    }
}
