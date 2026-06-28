package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import static data.scripts.plugins.VayraElectroChaffAndJunkjetPlugin.VAYRA_DEBUG;
import data.scripts.plugins.VayraElectroChaffAndJunkjetPlugin;
import java.util.List;
import org.lazywizard.lazylib.combat.CombatUtils;
import static data.scripts.plugins.VayraElectroChaffAndJunkjetPlugin.EMP_ARC_RANGE;
import static data.scripts.plugins.VayraElectroChaffAndJunkjetPlugin.log;
import java.util.WeakHashMap;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

public class VayraElectrostaticBeamOnHitEffect implements BeamEffectPlugin {

    // amount to reduce burst length by for calculating crit interval, and increase it by for calculating crit chance
    private static float TIME_MOD = 0.05f;

    private static Map<WeaponAPI, IntervalUtil> FIRE_INTERVALS = new WeakHashMap<>();

    private boolean wasZero = true;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine == null || beam == null || beam.getWeapon() == null || beam.getDamage() == null) {
            return;
        }
        CombatEntityAPI target = beam.getDamageTarget();

        // initialize crit stuff
        if (beam.getWeapon().getSpec() == null || beam.getWeapon().getSpec().getDerivedStats() == null) {
            return;
        }
        float burstLength = beam.getWeapon().getSpec().getDerivedStats().getBurstFireDuration();
        float intervalLength = Math.max(0.05f, burstLength - TIME_MOD);
        float critChance = Math.max(0f, Math.min(1f, burstLength + TIME_MOD));

        // initialize fire interval for THIS SPECIFIC WEAPON
        IntervalUtil fireInterval = FIRE_INTERVALS.get(beam.getWeapon());
        if (fireInterval == null) {
            fireInterval = new IntervalUtil(intervalLength, intervalLength);
            FIRE_INTERVALS.put(beam.getWeapon(), fireInterval);
            if (VAYRA_DEBUG) {
                log.info(String.format("initialized fireInterval of [%s] seconds with [%s] critChance for [%s]", intervalLength, critChance, beam.getWeapon()));
            }
        }

        // don't trigger on hits to things that aren't ships, or hits to shield
        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
            ShipAPI targetShip = (ShipAPI) target;
            boolean shieldHit = targetShip.getShield() != null && targetShip.getShield().isWithinArc(beam.getTo());

            if (!shieldHit) {
                // needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
                float dur = beam.getDamage().getDpsDuration();
                if (!wasZero) {
                    dur = 0;
                }
                wasZero = beam.getDamage().getDpsDuration() <= 0;
                fireInterval.advance(dur);

                // trigger every <interval> seconds
                // don't trigger if we're hitting a hulk (don't wanna waste chaff)
                if (fireInterval.intervalElapsed() && !targetShip.isHulk() && Math.random() < critChance) {
                    // spawn own pathetic arc
                    Vector2f dir = Vector2f.sub(beam.getTo(), beam.getFrom(), new Vector2f());
                    if (dir.lengthSquared() > 0) {
                        dir.normalise();
                    }
                    dir.scale(50f);
                    Vector2f point = Vector2f.sub(beam.getTo(), dir, new Vector2f());
                    float emp = beam.getDamage().getFluxComponent() * 2f;
                    float dam = beam.getDamage().getDamage() * 0.5f;
                    engine.spawnEmpArc(
                            beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
                            DamageType.ENERGY,
                            dam, // damage
                            emp, // emp 
                            69420f, // max range 
                            "tachyon_lance_emp_impact",
                            beam.getWidth() * 2f,
                            beam.getFringeColor(),
                            beam.getCoreColor());

                    // consume previously existing chaff particles for bonus arcs
                    if (VAYRA_DEBUG) {
                        log.info("electrobeam hit nonhulk ship, triggering chaff");
                    }
                    List<MissileAPI> missiles = CombatUtils.getMissilesWithinRange(target.getLocation(), target.getCollisionRadius() + EMP_ARC_RANGE);
                    for (MissileAPI missile : missiles) {
                        if (missile != null && VayraElectroChaffAndJunkjetPlugin.getChaff().contains(missile) && !VayraElectroChaffAndJunkjetPlugin.getChaffSpent().contains(missile)) {
                            VayraElectroChaffAndJunkjetPlugin.expendChaff(missile, target, false);
                        }
                    }
                }
            }
        }
    }
}
