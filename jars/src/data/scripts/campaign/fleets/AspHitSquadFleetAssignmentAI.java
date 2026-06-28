/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.intel.misc.AspHitSquadDepartureIntel;
import static data.scripts.JunkPiratesModPlugin.enableJunkPiratesIntel;

/**
 *
 * @author paul well actually sort of was this time
 */
public class AspHitSquadFleetAssignmentAI implements EveryFrameScript {
    
    private final AspHitSquadData data;
    private final CampaignFleetAPI fleet;
    private boolean orderedEscape = false;
    
    public AspHitSquadFleetAssignmentAI(CampaignFleetAPI fleet, AspHitSquadData data) {
        this.fleet = fleet;
        this.data = data;

        if (isRouteValid()) {
            setFleetUp();
        }
    }

    private boolean isRouteValid() {
        return fleet != null
                && data != null
                && data.from != null
                && data.to != null
                && data.from.getPrimaryEntity() != null
                && data.to.getPrimaryEntity() != null;
    }

    @Override
    public void advance(float amount) {
        //SectorEntityToken home = data.from.getPrimaryEntity();
        
        if (!isRouteValid()) return;
        if (fleet.getAI().getCurrentAssignment() != null) { // there is a command to action
            if (data.to.getPrimaryEntity() == null) { // nowhere to go
                fleet.clearAssignments();
                orderedEscape = true;
            } else
            
            if (fleet.getFleetPoints() < data.startingFP / 2 ) { // severely damaged
                fleet.clearAssignments();
                orderedEscape = true;
            }
        } else {
            if (orderedEscape) { // review logic against behaviour!
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, data.from.getPrimaryEntity(), 1000, "aborting mission");// go back whence they came and despawn
            } else if ("mission_complete".equals(data.mission)) { // no assignments and not escaping - get on with it
                fleet.addAssignment (FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, data.from.getPrimaryEntity(), getOrbitDays(), getReturningActionText());
            } else if ("loiter".equals(data.mission)) {
                fleet.addAssignment (FleetAssignment.ORBIT_AGGRESSIVE, data.to.getPrimaryEntity(), getOrbitDays(), getIntelActionText());
                data.mission = "then_what";
            } else if ("new_assignment".equals(data.mission)) {
                data.from = data.to;
                fleet.addAssignment (FleetAssignment.ORBIT_AGGRESSIVE, data.from.getPrimaryEntity(), getOrbitDays(), getStartingActionText());
                data.mission = "find_somewhere_else";
            } else if ("find_somewhere_else".equals(data.mission)) {
                MarketAPI next = findNewTarget();
                if (data.fleet != null && next != null) {
                    data.to = next;
                    if (enableJunkPiratesIntel) {
                    new AspHitSquadDepartureIntel(data);
                    }
                } else {
                    data.mission = "mission_complete";
                }
                if (!isRouteValid()) return;
                fleet.addAssignment (FleetAssignment.GO_TO_LOCATION, data.to.getPrimaryEntity(), 1000f, getTravelActionText());
                fleet.addAssignment (FleetAssignment.ORBIT_AGGRESSIVE, data.to.getPrimaryEntity(), getOrbitDays(), getIntelActionText());
                data.mission = "then_what";
            } else if ("hunt_player".equals(data.mission)) {
                if (data.fleet != null) {
                    if (enableJunkPiratesIntel) {
                        new AspHitSquadDepartureIntel(data);
                    }
                }
                fleet.addAssignment(FleetAssignment.INTERCEPT, Global.getSector().getPlayerFleet(), 7.0f); // spend a week on it
                data.mission = "hunting";
            } else if ("hunting".equals(data.mission)) { // we started but do we finish
                if (Global.getSector().getPlayerFleet().isVisibleToSensorsOf(fleet)) { // target is visible to fleet sensors
                    data.mission = "hunt_player";
                } else { // lost contact
                    data.mission = "then_what";
                }
                
            } else if ("then_what".equals(data.mission)) {
                if (playerStillWanted()) {
                
                    if (playerHere()) {
                            data.mission = "hunt_player";
                        } else {
                        float decision_yeah = (float) Math.random() * 10f;

                        if (decision_yeah < 1.5f) {
                            data.mission = "mission_complete";
                            } else if (decision_yeah < 4.0f) {
                            data.mission = "new_assignment";

                            } else {
                            data.mission = "loiter";
                            }
                        }

                    } else {
                    data.mission = "mission_complete";
                }
            }
        }

    }    

    public boolean playerStillWanted() {
        
        return Global.getSector().getMemoryWithoutUpdate().getBoolean("$playerIsAspWanted");
     
    }
    
    public boolean playerHere() {
        if (!isRouteValid() || Global.getSector().getPlayerFleet() == null) return false;
        return Global.getSector().getPlayerFleet().getStarSystem() == data.to.getStarSystem();
    }
    
    public MarketAPI findNewTarget() {
        //if (data.to == null) return null;
        
        WeightedRandomPicker<MarketAPI> markets = new WeightedRandomPicker<MarketAPI>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            // Find markets to visit.
            if (market == null || market.isHidden()) continue;
            if (market.getPrimaryEntity() == null) continue;
            if (market == data.to) continue; // this is where we are

            if (!market.hasSpaceport()) continue;
            if (market.getFaction() != null && market.getFaction().isHostileTo("syndicate_asp")) continue; // only get markets who would work with the ASP Syndicate

            markets.add(market, market.getSize()); // weights the market by market size.
        }
        return markets.pick(); // slam in a new destination in the data
    }
    
    public static class AspHitSquadData {
        public String mission;
        public float startingFP;
        public MarketAPI from;
        public MarketAPI to;
                
        public CampaignFleetAPI fleet;
        
        public AspHitSquadData(CampaignFleetAPI fleet) {
            this.fleet = fleet;
            }
        
        }
        
        protected float getOrbitDays() {
            return 8.0f;
        }
        
        protected void setFleetUp() {
                fleet.addAssignment (FleetAssignment.GO_TO_LOCATION, data.to.getPrimaryEntity(), 1000f, getStartingActionText());
                fleet.addAssignment (FleetAssignment.ORBIT_AGGRESSIVE, data.to.getPrimaryEntity(), getOrbitDays(), getIntelActionText());
                data.mission = "then_what";
        }
        
	
	
	
	protected String getStartingActionText() {
            // we're probably going to be in orbit around paddington or something about to decide what to do next.
            // before leaving anywhere, spend a day checking the system for the player or something.
            // maybe - 60% check for player; 25% new target then check for player; 10% go home. Every day.
            String mission = data.mission;
            
            String missionText = "Preparing";

            if ( "then_what".equals(mission)) missionText = "Considering next action";
            if ( "hunt_player".equals(mission)) missionText = "Intercepting the known pirate " +  playerName();
            if ( "hunting".equals(mission)) missionText = "On the prowl for " + playerName();
            if ( "mission_complete".equals(mission)) missionText = "Traveling to " + data.to.getName();
            //if ( mission == "loiter") missionText = "Negotiating with " + factionDisplayName(data.customerFaction) + " officials";
            
            return missionText;
            
	}

        protected String playerName() {
            if (Global.getSector().getPlayerPerson() == null) return "the target";
            return Global.getSector().getPlayerPerson().getNameString();
        }
        
        protected String factionDisplayName(String faction) {
            
            if (faction == null || Global.getSector().getFaction(faction) == null) return "local";
            String factionName = Global.getSector().getFaction(faction).getDisplayName();
            
            return factionName;
        }
        
	protected String getEndingActionText() {
                // this would be after wrapping up a journey; deciding to pack it in after failing to locate player.
                // Might be an active fleet hearing a report from other fleet on player kill.
		return "Concluding local investigations at " + data.to.getName();
	}
        
        protected String getIntelActionText() {
                // Looking for info on the player
		return "Seeking intel on " + playerName() + " at " + getData().to.getName();
	}
        
        
        protected String getReturningActionText() {
            // Done. Either going back because run out of gas (failed) or successful attack on player (won).
            // Decide whether to be happy or sad or just non-specific.
            return "Returning back to " + data.from.getName(); // from shouldn't change for a fleet,
        }
	
	protected String getTravelActionText() {
            // We might be: 
            //      - Travelling to a new market
            //      - Hovering around a new market
            //      - Seeking intel on the player
            //      - Moving on
                           
            String mission = data.mission;
            
            String missionText = "Traveling";

            if ( "then_what".equals(mission)) missionText = "Considering next action";
            if ( "hunt_player".equals(mission)) missionText = "Intercepting the known pirate " +  playerName();
            if ( "hunting".equals(mission)) missionText = "On the prowl for " + playerName();
            if ( "mission_complete".equals(mission)) missionText = "Returning to " + data.from.getName();
                
                if (mission == null || mission.isEmpty()) {
                        return "traveling to " + getData().to.getName();
                } else {
                    return missionText;
                }
	}
	
	protected String getInSystemActionText(RouteSegment segment) {
            return "WHAT";
	}
        
        @Override
        public boolean runWhilePaused()
        {
            return false;
        }
        
        @Override
        public boolean isDone()
        {
            return !fleet.isAlive();
        }
	
	protected AspHitSquadData getData() {
                
                return data;
	}
	
}
	











