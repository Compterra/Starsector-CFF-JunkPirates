/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.procgen.themes;

import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import java.util.Random;

/**
 *
 * @author paul. Honestly.
 */
public class JunkPiratesAnarchistStationFleetManager extends SourceBasedFleetManager{
    
        protected int minPts;
	protected int maxPts;
	protected int totalLost;

	public JunkPiratesAnarchistStationFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay, 
									  int minPts, int maxPts) {
		super(source, thresholdLY, minFleets, maxFleets, respawnDelay);
		this.minPts = minPts;
		this.maxPts = maxPts;
	}
	
	@Override
	protected CampaignFleetAPI spawnFleet() {
		if (source == null || source.getContainingLocation() == null) return null;
		if (!(source.getContainingLocation() instanceof StarSystemAPI)) return null;
		Random random = new Random();
		
		int pointRange = Math.max(1, maxPts - minPts + 1);
		int combatPoints = minPts + random.nextInt(pointRange);
		
		int bonus = totalLost * 4;
		if (bonus > maxPts) bonus = maxPts;
		
		combatPoints += bonus;
		
		String type = FleetTypes.PATROL_SMALL;
		if (combatPoints > 8) type = FleetTypes.PATROL_MEDIUM;
		if (combatPoints > 16) type = FleetTypes.PATROL_LARGE;
		
		combatPoints *= 8f;

                String anarchist_faction = "pack";
                FactionAPI sourceFaction = source.getFaction();
                if (sourceFaction != null && sourceFaction.getId() != null) anarchist_faction = sourceFaction.getId();                
                
		FleetParamsV3 params = new FleetParamsV3(
				null, //source.getMarket(),
				source.getLocationInHyperspace(),
				anarchist_faction,
				1f,
				type,
				combatPoints, // combatPts
				0f, // freighterPts 
				0f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				1f // qualityMod
		);
		params.officerNumberBonus = 10;
		params.random = random;
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null || fleet.isEmpty()) return null;
		
		LocationAPI location = source.getContainingLocation();
		if (location == null) return null;
		location.addEntity(fleet);
		
		JunkPiratesAnarchistSeededFleetManager.initJunkPiratesAnarchistFleetProperties(random, fleet, false);
		
		fleet.setLocation(source.getLocation().x, source.getLocation().y);
		fleet.setFacing(random.nextFloat() * 360f);
		
		fleet.addScript(new JunkPiratesAnarchistAssignmentAI(fleet, (StarSystemAPI) location, source));
		
		return fleet;
	}

	
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
		super.reportFleetDespawnedToListener(fleet, reason, param);
		if (reason == CampaignEventListener.FleetDespawnReason.DESTROYED_BY_BATTLE) {
			totalLost++;
		}
	}

	
    
}
