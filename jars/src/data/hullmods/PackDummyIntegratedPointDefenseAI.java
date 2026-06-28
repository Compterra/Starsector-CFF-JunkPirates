package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class PackDummyIntegratedPointDefenseAI extends BaseHullMod {

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship == null) return;

        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon == null) continue;
            if (weapon.getSize() == WeaponSize.SMALL && weapon.getType() != WeaponType.MISSILE) {
                weapon.setPD(true);
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }
}
