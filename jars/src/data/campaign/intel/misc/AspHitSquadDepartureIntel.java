/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.campaign.intel.misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static com.fs.starfarer.api.util.Misc.ucFirst;
import data.scripts.campaign.fleets.AspHitSquadFleetAssignmentAI.AspHitSquadData;
import java.awt.Color;
import java.util.Set;

/**
 *
 * @author paul
 */
public class AspHitSquadDepartureIntel  extends BaseIntelPlugin {

	protected AspHitSquadData data;
        transient protected FactionAPI aspFaction;
        transient protected String fleetSizeDescriptor;

	public AspHitSquadDepartureIntel(AspHitSquadData data) {
		this.data = data;
		
                if (data.from == null || data.to == null || data.fleet == null) return;
                
		initTransientData();
		
		boolean sameLoc = data.from.getPrimaryEntity().getContainingLocation() != null &&
						  data.from.getPrimaryEntity().getContainingLocation() == 
							  Global.getSector().getPlayerFleet().getContainingLocation() &&
						  !data.from.getPrimaryEntity().getContainingLocation().isHyperspace();
		
                float prob = 0.5f; // approx 50% base chance
                
		if (!sameLoc) {
			prob += 0.5f; //  approx 100% chance if player in system
		}
                
                float target = 5f; // we only want maybe 5 or 6 sector wide. If you are so heavily wanted it's fine to be diluted
		float numAlready = Global.getSector().getIntelManager().getIntelCount(AspHitSquadDepartureIntel.class, true);
		
                if (numAlready > target) {
                    prob -= 0.15f * (numAlready - target); // less chance more news, 15% per news item over
                }
                
                if (data.from.getFaction().isHostileTo(Factions.PLAYER)) {
                    prob -= 0.3f; // less likely to spill news at a hostile planet - 70% if in system, 20% if out system, 0% if lots of news & out of system
                }
                
                if (Math.random() > prob) {
                    return;
		}
                
		float postingRange = 0f;

		setPostingRangeLY(postingRange, true);
		
		setPostingLocation(data.from.getPrimaryEntity());
		
                float postingTime = ((float) Math.random() * 6) + 4; 
                
		Global.getSector().getIntelManager().queueIntel(this, postingTime); // stick it there for about a week or so, fairly transient sort of news.
                
	}
	
	
	protected void initTransientData() {
//		data = (EconomyFleetAssignmentAI.EconomyRouteData) route.getCustom();
                aspFaction = Global.getSector().getFaction("syndicate_asp");
                
                if (data.fleet.getFleetPoints() > 300) {
                    fleetSizeDescriptor = "huge";
                } else if (data.fleet.getFleetPoints() > 200) {
                    fleetSizeDescriptor = "very large";
                } else if (data.fleet.getFleetPoints() > 100) {
                    fleetSizeDescriptor = "large";
                } else if (data.fleet.getFleetPoints() > 50) {
                    fleetSizeDescriptor = "moderate";
                } else if (data.fleet.getFleetPoints() > 15) {
                    fleetSizeDescriptor = "small";
                } else {
                    fleetSizeDescriptor = "tiny";
                }
                
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == IntelInfoPlugin.ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		if (mode != IntelInfoPlugin.ListInfoMode.IN_DESC) {
			info.addPara("Faction: " + aspFaction.getDisplayName(), initPad, tc,
						 aspFaction.getBaseUIColor(), aspFaction.getDisplayName());
			info.addPara("Fleet size: " + ucFirst(fleetSizeDescriptor), initPad, tc);
//			info.addPara("To Client: " + customerFaction.getDisplayName(), initPad, tc,
//						 customerFaction.getBaseUIColor(), customerFaction.getDisplayName());
			initPad = 0f;
		}
		
//		String what = getWhat();
//		if (valuable) {
//			info.addPara("Carrying valuable " + what, tc, initPad);
//			initPad = 0f;
//		} else if (large) {
//			info.addPara("Large volume of " + what, tc, initPad);
//			initPad = 0f;
//		}
		
		if (mode != IntelInfoPlugin.ListInfoMode.IN_DESC) {
			LabelAPI label = info.addPara("Hit Squad at " + data.from.getName(), tc, initPad);
			label.setHighlight(data.from.getName());
			label.setHighlightColors(data.from.getFaction().getBaseUIColor());
			initPad = 0f;
		}
		
//                if (sinceLaunched == null) {
//                    sinceLaunched = 0f;
//                }
//                
//		if (sinceLaunched <= 2) {
//			info.addPara("Message recently recieved", tc, initPad);
//		} else {
//                        info.addPara("Recieved " + sinceLaunched + " days ago", tc, initPad);
//			}
		
		unindent(info);
	}
	
	protected String getWhat() {
            
                String what = Global.getSector().getPlayerPerson().getNameString();
                
//		String what = "important documents";
//		if (prisoner) what = "dangerous prisoners";
//		if (special) what = "-//redact//-";
//		if (money) what = "valuable items";
//		if (weapons) what = "materiel";
//		if (vip) what = "important people";
		return what;
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		float pad = 3f;
		float opad = 10f;
		
		initTransientData();
		
		LabelAPI label = info.addPara(getName(), c, 0f);
//		label.setHighlight(Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix()));
//		label.setHighlightColor(getFactionForUIColors().getBaseUIColor());
		
		addBulletPoints(info, mode);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		initTransientData();
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		info.addImage(aspFaction.getLogo(), width, 128, opad);
		
		String fleetType = "Hit Fleet";
		
		
//		LabelAPI label = info.addPara(Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " " + 
//					 faction.getPersonNamePrefix() + " " + fleetType + " is departing from " +
//					 data.from.getName() + " and heading to " + data.to.getName() + ".",
//					 opad, tc, 
//					 faction.getBaseUIColor(),
//					 faction.getPersonNamePrefix());
//		label.setHighlight(faction.getPersonNamePrefix(), data.from.getName(), data.to.getName());
//		label.setHighlightColors(data.from.getFaction().getBaseUIColor(), data.from.getFaction().getBaseUIColor(), data.to.getFaction().getBaseUIColor());
		
		LabelAPI label = info.addPara("You are getting a lot of comms traffic from " + data.from.getName() + 
				 " with your ident attached to it.", opad, tc);
                LabelAPI label2 = info.addPara("It appears that " + aspFaction.getPersonNamePrefixAOrAn() + " " + 
				 aspFaction.getPersonNamePrefix() + " " + fleetType +
                                " was seen in orbit, seeking not much else but your whereabouts.",
				 opad, tc);
		
		label.setHighlight(data.from.getName(), aspFaction.getPersonNamePrefix());
		label.setHighlightColors(data.from.getFaction().getBaseUIColor(), aspFaction.getBaseUIColor());
		
//		addBulletPoints(info, ListInfoMode.IN_DESC);

//                if (sinceLaunched <= 2) {
//                    info.addPara("This message arrived recently.", opad);
//                } else {
//                    info.addPara("This message arrived " + sinceLaunched + " days ago.", opad);
//                }
		
		String what = getWhat();
		
                LabelAPI label3;
                
                String fleetSizeDescriptorModified = fleetSizeDescriptor;
                if (fleetSizeDescriptorModified == "moderate") {
                    fleetSizeDescriptorModified = "moderately sized";
                }
                
                if (fleetSizeDescriptor == "tiny" || fleetSizeDescriptor == "small") {
                    label3 = info.addPara("However, rumours suggest the fleet is " + fleetSizeDescriptor + ".", opad);
                } else {
                    label3 = info.addPara("The information available points to the fleet being " + fleetSizeDescriptorModified + ".", opad);
                }
		label3.setHighlight(fleetSizeDescriptor);
		label3.setHighlightColors(aspFaction.getBaseUIColor());
                
                info.beginIconGroup();
                info.setIconSpacingMedium();

                info.addIconGroup(32, 1, opad);
		

	}
	
	@Override
	public String getIcon() {
		initTransientData();
		return Global.getSettings().getSpriteName("intel", "hostilities");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_MILITARY);
		
		tags.add(aspFaction.getId());;
		
		return tags;
	}
	
	
	public String getSortString() {
		return "Hit Fleet";
	}
	
	public String getFleetTypeName() {
		
                String fleetType = "Hit Fleet";
            
		return fleetType;
	}
	
	public String getName() {
		//return Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix()) + " " + getFleetTypeName();
		return getFleetTypeName();
		//return "Trade Fleet Departure";
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
            if (data.to == null || data.to.getFaction() == null || data.to.getFaction().getId() == null) {
                return null;
            } else {
            FactionAPI faction = Global.getSector().getFaction(data.to.getFaction().getId());
		return faction;
            }
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
            if (data.from == null || data.from.getPrimaryEntity() == null) {
                return null;
            } else {
		return data.from.getPrimaryEntity();
            }
	}
	
	
//	protected Float sinceLaunched = null;
//	@Override
//	protected void advanceImpl(float amount) {
//		super.advanceImpl(amount);
////		
//////		if (route.getDelay() > 0) return;
////		
//		if (sinceLaunched == null) sinceLaunched = 0f;
//		
////		if (sinceLaunched > 2) {
////			sendUpdateIfPlayerHasIntel(new Object(), true);
////		}
////		
//		float days = Misc.getDays(amount);
//		sinceLaunched += days;
//	}
	
//	public float getTimeRemainingFraction() {
//		float f = route.getDelay() / 30f;
//		return f;
//	}
	

//	@Override
//	public boolean shouldRemoveIntel() {
//
//		if (sinceLaunched != null && sinceLaunched < getBaseDaysAfterEnd()) {
//			return false;
//		}
//		return true;
//	}
	

	@Override
	public void setImportant(Boolean important) {
		super.setImportant(important);
		if (isImportant()) {
			if (!Global.getSector().getScripts().contains(this)) {
				Global.getSector().addScript(this);
			}
		} else {
			Global.getSector().removeScript(this);
		}
	}

	@Override
	public void reportRemovedIntel() {
		super.reportRemovedIntel();
		Global.getSector().removeScript(this);
	}
	
	
}
