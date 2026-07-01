/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import java.util.ArrayList;
import java.util.List;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.CargoQuantityData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import static data.scripts.JunkPiratesModPlugin.isExerelin;

/**
 *
 * @author paul yeah right
 */
public class AspCourierFleetAssignmentAI implements EveryFrameScript {
    
    private final AspCourierRouteData data;
    private final CampaignFleetAPI fleet;
    private boolean orderedEscape = false;
    
    public AspCourierFleetAssignmentAI(CampaignFleetAPI fleet, AspCourierRouteData data) {
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
        
        if (!isRouteValid() || fleet.getAI() == null) return;
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
            if (orderedEscape) {
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, data.from.getPrimaryEntity(), 1000, "aborting mission");// go back whence they came and despawn
            } else if ("delivered".equals(data.mission)) { // no assignments and not esacping - get on with it
                if (data.fleet != null && data.fleet.getCargo() != null) {
                    data.fleet.getCargo().clear();
                }
                fleet.addAssignment (FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, data.from.getPrimaryEntity(), getOrbitDays(), getReturningActionText());
            } else {
                fleet.addAssignment (FleetAssignment.ORBIT_PASSIVE, data.from.getPrimaryEntity(), getOrbitDays(), getStartingActionText());
                fleet.addAssignment (FleetAssignment.GO_TO_LOCATION, data.to.getPrimaryEntity(), 1000, getTravelActionText());
                fleet.addAssignment (FleetAssignment.ORBIT_PASSIVE, data.to.getPrimaryEntity(), getOrbitDays(), getEndingActionText());
                data.mission = "delivered";
                
            }
        }

    }    

    public static class AspCourierRouteData {
        public String mission = "items"; // refactor to 'mission', 'customerFaction', we will need fuel and cargo capacity to be fair
        public String cargotype = "items"; // refactor to 'mission', 'customerFaction', we will need fuel and cargo capacity to be fair
        public String customerFaction;
        public float cargoCap;
        public float fuelCap;
        public float personnelCap;
        public float startingFP;
        //public boolean money = false;
        public float size;
        public boolean smuggling = false;
        public MarketAPI from;
        public MarketAPI to;
        
        public List<CargoQuantityData> cargoDeliver = new ArrayList<CargoQuantityData>();
        public List<SpecialItemData> specialDeliver = new ArrayList<SpecialItemData>();
        public List<String> weaponDeliver = new ArrayList<String>();
        
        public CampaignFleetAPI fleet;
        // add something like a getcargolist function
        
        // we will need a list of stuff that is in cargo probably just a cargoDeliver
        public void addDeliver(String id, int qty) {
			cargoDeliver.add(new CargoQuantityData(id, qty));
            }
        
        // add Deliver function
        public static String getCargoList(List<CargoQuantityData> cargo) {
                return "unknown";
            }
        
        public AspCourierRouteData(CampaignFleetAPI fleet) {
            this.fleet = fleet;
        }
        
        }
	
	private String origFaction;
	private String customerFaction;
//	private IntervalUtil factionChangeTracker = new IntervalUtil(0.1f, 0.3f);
        
        protected float getOrbitDays() {
            return 3.0f;
        }
        
        protected void setFleetUp() {            
            updateCargo();
        }
        
	protected void updateCargo() { // needs work based on missions
	
            
            
                float tier = data.size;
		
		CargoAPI cargo = fleet.getCargo();
                if (cargo == null) return;
                data.cargoDeliver.clear();
                data.specialDeliver.clear();
                data.weaponDeliver.clear();
                cargo.clear();
		
                if ("prisoner".equals(data.mission)) {
                    data.addDeliver(Commodities.CREW, (int) tier * 25);
                    if (isExerelin) { // Nex is enabled and we can stick a prisoner in the cargo hold
                            data.addDeliver("prisoner", (int) tier);
                            float creds = (float) Math.random() * tier;
                            data.addDeliver("syndicate_asp_credit_chip", (int) creds);
                    } else {
                        float creds = (float) Math.random() * 3 + tier;// not much to do I guess ... stick a few credits in the hold
                        data.addDeliver("syndicate_asp_credit_chip", (int) creds);
                    }
                }
                
                if ("vip".equals(data.mission)) {
                    data.addDeliver(Commodities.LOBSTER, (int) tier * 5);
                    data.addDeliver(Commodities.LUXURY_GOODS, (int) tier * 10);
                    if (isExerelin) { // Nex is enabled and we can stick a VIP in the cargo hold
                            data.addDeliver("agent", (int) tier);
                            float creds = (float) Math.random() * 3 + tier;
                            data.addDeliver("syndicate_asp_credit_chip", (int) creds);
                    } else {
                        float creds = (float) Math.random() * 6 + tier;// not much to do I guess ... stick a few thousand credits in the hold
                        data.addDeliver("syndicate_asp_credit_chip", (int) creds);
                    }
                }
                
                if ("money".equals(data.mission)) {
                        float creds = (float) Math.random() + tier * 3;// not much to do I guess ... stick a few thousand credits in the hold
                        data.addDeliver("syndicate_asp_credit_chip", (int) creds);
                }
                
                if ("items".equals(data.mission)) {
                        float creds = (float) Math.random() * tier + 3;// not much to do I guess ... stick a few thousand credits in the hold
                        data.addDeliver("syndicate_asp_credit_chip", (int) creds);
                        // this needs expanding to include special items and weapons as appropriate
                }
                
                for (CargoQuantityData thing : data.cargoDeliver) { // then stick cargo in the fleet data
                    addCommodityIfValid(cargo, thing.cargo, thing.units);
                }
                
	}

        protected void addCommodityIfValid(CargoAPI cargo, String commodityId, int units) {
            if (cargo == null || commodityId == null || units <= 0) return;
            CommoditySpecAPI spec = null;
            try {
                spec = Global.getSettings().getCommoditySpec(commodityId);
            } catch (Throwable ex) {
                spec = null;
            }
            if (spec == null) return;
            cargo.addCommodity(commodityId, units);
        }
	
	
	
	protected String getStartingActionText() {
            String mission = data.mission;
            
            String missionText = "INITIALISE";

            if ( "prisoner".equals(mission)) missionText = "Negotiating with " + factionDisplayName(data.customerFaction) + " officials";
            if ( "vip".equals(mission)) missionText = "Wining and dining with notable " + factionDisplayName(data.customerFaction) + " individuals";
            if ( "items".equals(mission)) missionText = "Discussing terms with " + factionDisplayName(data.customerFaction) + " traders";
            if ( "money".equals(mission)) missionText = "Discussing terms with " + factionDisplayName(data.customerFaction) + " financiers";
            
            return missionText + " at " + marketName(data.from);
            
	}

        protected String factionDisplayName(String faction) {
            
            if (Global.getSector() == null || faction == null || Global.getSector().getFaction(faction) == null) return "local";
            String factionName = Global.getSector().getFaction(faction).getDisplayName();
            
            return factionName;
        }
        
	protected String getEndingActionText() {
                
		return "Closing contract with " + factionDisplayName(data.customerFaction) + " representatives at " + marketName(getData().to);
	}
        
        protected String getReturningActionText() {
            return "Returning back to " + marketName(data.from);
        }
	
	protected String getTravelActionText() {
            
                String mission = data.mission;
                
		String missionText = "INITIALISE";
                
                if ( "prisoner".equals(mission)) missionText = "Transporting a dangerous prisoner";
                if ( "vip".equals(mission)) missionText = "Travelling with style";
                if ( "items".equals(mission)) missionText = "On a delivery contract";
                if ( "money".equals(mission)) missionText = "Providing financial services";
                
                if (mission == null || mission.isEmpty()) {
                        return "traveling to " + marketName(getData().to);
                }

                return missionText + " to " + marketName(getData().to);

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
            return fleet == null || !fleet.isAlive();
        }
	
	protected String marketName(MarketAPI market) {
            return market != null ? market.getName() : "unknown";
        }

	protected AspCourierRouteData getData() {
                
                return data;
	}
	
}
	











