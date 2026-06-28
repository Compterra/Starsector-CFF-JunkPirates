/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.campaign.intel.misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.CargoQuantityData;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.fleets.AspCourierFleetAssignmentAI.AspCourierRouteData;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author paul
 */
public class AspCourierDepartureIntel  extends BaseIntelPlugin {

	transient protected List<CargoQuantityData> cargoDeliver;
	transient protected List<SpecialItemData> specialDeliver;
	transient protected List<String> weaponDeliver;
	transient protected boolean money;
	transient protected boolean prisoner;
	transient protected boolean items;
	transient protected boolean vip;
	transient protected boolean special;
	transient protected boolean weapons;
	transient protected FactionAPI customerFaction;
	transient protected FactionAPI origFaction;
	transient protected FactionAPI aspFaction;
	protected AspCourierRouteData data;
        	

	public AspCourierDepartureIntel(AspCourierRouteData data) {
		this.data = data;
		
                if (data.from == null || data.fleet == null || data.cargotype == null || data.to == null) return;
                
		initTransientData();
		
		//if (deliverList.isEmpty() && returnList.isEmpty()) {
//		if (cargoDeliver.isEmpty() && specialDeliver.isEmpty() && weaponDeliver.isEmpty()) {
//			return;
//		}

		float prob = 0.1f;
		if (prisoner) prob += 0.3f;
		if (money) prob += 0.1f;
		if (!specialDeliver.isEmpty() && !weaponDeliver.isEmpty()) {
			prob += 0.2f;
		}
		
		boolean sameLoc = data.from.getPrimaryEntity().getContainingLocation() != null &&
						  data.from.getPrimaryEntity().getContainingLocation() == 
							  Global.getSector().getPlayerFleet().getContainingLocation() &&
						  !data.from.getPrimaryEntity().getContainingLocation().isHyperspace();
		if (sameLoc) prob = 1f;
		
		float target = Global.getSettings().getFloat("targetNumTradeFleetNotifications"); // may as well align with vanilla
		float numAlready = Global.getSector().getIntelManager().getIntelCount(AspCourierDepartureIntel.class, true);
		
		float probMult = Misc.getProbabilityMult(target, numAlready, 0.5f);
		if (probMult > 1) probMult = 1; // just making it less likely if there's a bunch of these already
		
		prob *= probMult;
		
		
		
		
		if (Math.random() > prob) {
			return;
		}
		
		float postingRange = 0f;
		if (prisoner) {
			postingRange = Math.max(3f, postingRange); // bigger stink
		}
		setPostingRangeLY(postingRange, true);
		
		setPostingLocation(data.from.getPrimaryEntity());
		
		Global.getSector().getIntelManager().queueIntel(this, 20);
                
//                sendUpdateIfPlayerHasIntel(new Object(), true);
	}
	
	
	protected void initTransientData() {
//		data = (EconomyFleetAssignmentAI.EconomyRouteData) route.getCustom();

		cargoDeliver = new ArrayList<CargoQuantityData>();
		specialDeliver = new ArrayList<SpecialItemData>();
		weaponDeliver = new ArrayList<String>();
		items = false;
		money = false;
		prisoner = false;
		vip = false;
                special = false;
                weapons = false;

                if (specialDeliver.size() > 0) {
                    special = true;
                } else if (weaponDeliver.size() > 0) {
                    weapons = true;
                }
                
                if (data.cargotype == null) data.cargotype = "items";
                
                if (data.cargotype == "items") {
                    items = true;
                } else if (data.cargotype == "prisoner") {
                    prisoner = true;
                } else if (data.cargotype == "money") {
                    money = true;
                } else if (data.cargotype == "vip") {
                    vip = true;
                }

		
		customerFaction = Global.getSector().getFaction(data.to.getFactionId());
		origFaction = Global.getSector().getFaction(data.from.getFactionId());
		aspFaction = Global.getSector().getFaction("syndicate_asp");
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
			info.addPara("Working for: " + customerFaction.getDisplayName(), initPad, tc,
						 customerFaction.getBaseUIColor(), customerFaction.getDisplayName());
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
			LabelAPI label = info.addPara("From " + data.from.getName() + " to " + data.to.getName(), tc, initPad);
			label.setHighlight(data.from.getName(), data.to.getName());
			label.setHighlightColors(data.from.getFaction().getBaseUIColor(), data.to.getFaction().getBaseUIColor());
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
            
                String what = "important documents";
            
                if (data.cargotype == "prisoner") {
                    what = "dangerous prisoners";
                } else if (data.cargotype == "money") {
                    what = "valuable items";
                } else if (data.cargotype == "vip") {
                    what = "important people";
                }
                
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
		
		float tier = data.size;
		String fleetType = "Courier Fleet";
		
		
//		LabelAPI label = info.addPara(Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " " + 
//					 faction.getPersonNamePrefix() + " " + fleetType + " is departing from " +
//					 data.from.getName() + " and heading to " + data.to.getName() + ".",
//					 opad, tc, 
//					 faction.getBaseUIColor(),
//					 faction.getPersonNamePrefix());
//		label.setHighlight(faction.getPersonNamePrefix(), data.from.getName(), data.to.getName());
//		label.setHighlightColors(data.from.getFaction().getBaseUIColor(), data.from.getFaction().getBaseUIColor(), data.to.getFaction().getBaseUIColor());
		
		LabelAPI label = info.addPara("Your contacts " + data.from.getOnOrAt() + " " + data.from.getName() + 
				 " let you know that " + 
				 aspFaction.getPersonNamePrefixAOrAn() + " " + 
				 aspFaction.getPersonNamePrefix() + " " + fleetType + " was seen in orbit around " + 
				 data.from.getName() + ".",
				 opad, tc, 
				 aspFaction.getBaseUIColor(),
				 aspFaction.getPersonNamePrefix());
		
		label.setHighlight(data.from.getName(), aspFaction.getPersonNamePrefix(), data.to.getName());
		label.setHighlightColors(data.from.getFaction().getBaseUIColor(), aspFaction.getBaseUIColor(), data.to.getFaction().getBaseUIColor());
		
//		addBulletPoints(info, ListInfoMode.IN_DESC);

//                if (sinceLaunched <= 2) {
//                    info.addPara("This message arrived recently.", opad);
//                } else {
//                    info.addPara("This message arrived " + sinceLaunched + " days ago.", opad);
//                }
		
		String what = getWhat();
		
                LabelAPI label2 = info.addPara("Information is limited, but the courier group were seen in negotiations with " + data.to.getFaction().getDisplayName() + 
                        " officials and were rumoured to be discussing the shipping of " + what + " to " + data.to.getName() + ".", opad);
		
		label2.setHighlight(data.to.getFaction().getDisplayName(), data.to.getName());
		label2.setHighlightColors(data.to.getFaction().getBaseUIColor(), data.to.getFaction().getBaseUIColor());
                
                info.beginIconGroup();
                info.setIconSpacingMedium();

                info.addIconGroup(32, 1, opad);
		

	}
	
	@Override
	public String getIcon() {
		initTransientData();
		return Global.getSettings().getSpriteName("intel", "tradeFleet_other");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_FLEET_DEPARTURES);
		
		tags.add(customerFaction.getId());;
		
		return tags;
	}
	
	
	public String getSortString() {
		return "Courier Fleet";
	}
	
	public String getFleetTypeName() {
		
                String fleetType = "Courier Fleet";
            
		if (prisoner) {
                    fleetType = "Armed Guard";
                }
		
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
	
	
	public List<IntelInfoPlugin.ArrowData> getArrowData(SectorMapAPI map) {
		List<IntelInfoPlugin.ArrowData> result = new ArrayList<IntelInfoPlugin.ArrowData>();
		
		if (data.from.getContainingLocation() == data.to.getContainingLocation() &&
				data.from.getContainingLocation() != null &&
				!data.from.getContainingLocation().isHyperspace()) {
			return null;
		}
		
		SectorEntityToken entityFrom = data.from.getPrimaryEntity();
		if (map != null) {
			SectorEntityToken iconEntity = map.getIntelIconEntity(this);
			if (iconEntity != null) {
				entityFrom = iconEntity;
			}
		}
		
		IntelInfoPlugin.ArrowData arrow = new IntelInfoPlugin.ArrowData(entityFrom, data.to.getPrimaryEntity());
		arrow.color = getFactionForUIColors().getBaseUIColor();
		result.add(arrow);
		
		return result;
	}
	
	
}
