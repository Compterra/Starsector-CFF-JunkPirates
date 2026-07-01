/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.campaign.intel.misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static com.fs.starfarer.api.util.Misc.ucFirst;
import data.scripts.campaign.fleets.JunkPiratesExplorerFleetAssignmentAI.JunkPiratesExplorerData;
import java.awt.Color;
import java.util.Set;

/**
 *
 * @author paul
 */
public class JunkExplorerDecisionIntel  extends BaseIntelPlugin {

	protected JunkPiratesExplorerData data;
        transient protected FactionAPI junkFaction;
        transient protected String fleetSizeDescriptor;
        transient protected String decision;
        transient protected String fleetType;

	public JunkExplorerDecisionIntel(JunkPiratesExplorerData data, String decision) {
		this.data = data;
		this.decision = decision;

                if (!isValidDataForPosting()) return;

		initTransientData();

		boolean sameLoc = isSameNonHyperspaceLocationAsPlayer(data.from.getPrimaryEntity());

                boolean friends = Global.getSector().getPlayerFaction() != null && data.fleet.getFaction().isAtWorst(Global.getSector().getPlayerFaction(), RepLevel.WELCOMING);

                if (!friends) return;

                float prob = 1.0f;

		if (!sameLoc) {
			prob -= 0.6f; // 40% chance if not in system
		}

		if (Math.random() > prob) {
			return;
		}

		float postingRange = 0f;

		setPostingRangeLY(postingRange, true);

		setPostingLocation(data.from.getPrimaryEntity());

		Global.getSector().getIntelManager().queueIntel(this, 15);

	}


	protected boolean isValidDataForPosting() {
		return Global.getSector() != null
				&& Global.getSector().getPlayerFleet() != null
				&& Global.getSector().getPlayerFaction() != null
				&& Global.getSector().getIntelManager() != null
				&& data != null
				&& data.from != null
				&& data.from.getPrimaryEntity() != null
				&& data.to != null
				&& data.to.getPrimaryEntity() != null
				&& data.fleet != null
				&& data.fleet.getFaction() != null;
	}

	protected boolean isSameNonHyperspaceLocationAsPlayer(SectorEntityToken entity) {
		if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return false;
		if (entity == null || entity.getContainingLocation() == null) return false;
		return entity.getContainingLocation() == Global.getSector().getPlayerFleet().getContainingLocation()
				&& !entity.getContainingLocation().isHyperspace();
	}

	protected void initTransientData() {
//		data = (EconomyFleetAssignmentAI.EconomyRouteData) route.getCustom();
                junkFaction = Global.getSector() != null ? Global.getSector().getFaction("junk_pirates") : null;



                if ("party".equals(decision)) {
                    fleetType = "Explorers";
                } else if ("troll".equals(decision)) {
                    fleetType = "Trouble Makers";
                } else {
                    fleetType = "Explorers";
                }

                if (data.fleet.getFleetPoints() > 300) {
                    fleetSizeDescriptor = "huge";
                } else if (data.fleet.getFleetPoints() > 200) {
                    fleetSizeDescriptor = "very large";
                } else if (data.fleet.getFleetPoints() > 100) {
                    fleetSizeDescriptor = "large";
                } else if (data.fleet.getFleetPoints() > 50) {
                    fleetSizeDescriptor = "moderate";
                } else if (data.fleet.getFleetPoints() > 10) {
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
//			info.addPara(junkFaction.getDisplayName() + " " + fleetType, initPad, tc,
//						 junkFaction.getBaseUIColor(), junkFaction.getDisplayName());
			info.addPara("Fleet size: " + ucFirst(fleetSizeDescriptor), initPad, tc);
			info.addPara("Target: " + data.to.getName(), initPad, tc);
			initPad = 0f;
		}

		if (mode != IntelInfoPlugin.ListInfoMode.IN_DESC) {
			LabelAPI label = info.addPara(data.to.getStarSystem().getName(), tc, initPad);
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

                String what = data.fleet != null && data.fleet.getCommander() != null ? data.fleet.getCommander().getNameString() : "the explorer";

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

		if (junkFaction != null) {
			info.addImage(junkFaction.getLogo(), width, 128, opad);
		}

//		LabelAPI label = info.addPara(Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " " +
//					 faction.getPersonNamePrefix() + " " + fleetType + " is departing from " +
//					 data.from.getName() + " and heading to " + data.to.getName() + ".",
//					 opad, tc,
//					 faction.getBaseUIColor(),
//					 faction.getPersonNamePrefix());
//		label.setHighlight(faction.getPersonNamePrefix(), data.from.getName(), data.to.getName());
//		label.setHighlightColors(data.from.getFaction().getBaseUIColor(), data.from.getFaction().getBaseUIColor(), data.to.getFaction().getBaseUIColor());

                String heOrShe = "She";
                String hisOrHer = "her";
                String whatWillSheDo = " has decided to cause as much trouble as possible within the ";

                if (data.fleet.getCommander() != null && data.fleet.getCommander().isMale()) {
                    heOrShe = "He";
                    hisOrHer = "his";
                }

                String fleetDescriptor = "fleet";

                if ("party".equals(decision)) {
                    whatWillSheDo = " has decided on a voyage of discovery; traveling to the ";
                    fleetDescriptor = "group of friends";
                }

		String junkPrefix = junkFaction != null ? junkFaction.getPersonNamePrefix() : "junk pirate";
		Color junkColor = junkFaction != null ? junkFaction.getBaseUIColor() : Misc.getHighlightColor();
		String commanderRank = data.fleet.getCommander() != null ? data.fleet.getCommander().getRank() : "captain";
		LabelAPI label = info.addPara("Friends at " + data.from.getName() +
				 " are excited to inform you that, having spent time in reflection, " + getWhat() +
				 ", the " +
				 junkPrefix + " " + commanderRank +
                                 " is leaving orbit having decided on " +
                                 hisOrHer + " preferred course of action.",opad, tc);

                                label.setHighlight(data.from.getName(), junkPrefix);
                                label.setHighlightColors(data.from.getFaction().getBaseUIColor(), junkColor);

                String fleetSizeDescriptorModified = fleetSizeDescriptor;
                if ("moderate".equals(fleetSizeDescriptorModified)) {
                    fleetSizeDescriptorModified = "moderately sized";
                }
		LabelAPI label2 = info.addPara(heOrShe + whatWillSheDo + data.to.getStarSystem().getName() + ". " + heOrShe + " travels with a " +
                                fleetSizeDescriptorModified + " " + fleetDescriptor + ".",opad, tc);

                if ("tiny".equals(fleetSizeDescriptor)) {
                    LabelAPI label3 = info.addPara(heOrShe + " should know better, really.",opad, tc);
                } else if ("huge".equals(fleetSizeDescriptor)) {
                    LabelAPI label3 = info.addPara(heOrShe + " has often been accused of overdoing it.",opad, tc);
                }

                info.beginIconGroup();
                info.setIconSpacingMedium();

                info.addIconGroup(32, 1, opad);


	}

	@Override
	public String getIcon() {
		initTransientData();
                if ("party".equals(decision) ) {
                    return Global.getSettings().getSpriteName("intel", "junk_pirates_party");
                }
		return Global.getSettings().getSpriteName("intel", "hostilities");
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);

                if ("party".equals(decision)) {
                    tags.add(Tags.INTEL_STORY);
                } else {
                    tags.add(Tags.INTEL_HOSTILITIES);
                }

		if (junkFaction != null) tags.add(junkFaction.getId());

		return tags;
	}


	public String getSortString() {
		return "Explorer Fleet";
	}

	public String getFleetTypeName() {

                String fleetType = "Junk Pirates Explorers";

                if ("troll".equals(decision)) {
                    fleetType = "Junk Pirates Troublemakers";
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
            if (Global.getSector() == null) return null;
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
		if (Global.getSector() == null) return;
		if (isImportant()) {
			if (!Global.getSector().getScripts().contains(this)) {
				Global.getSector().addScript(this);
			}
		} else {
			if (Global.getSector() != null) Global.getSector().removeScript(this);
		}
	}

	@Override
	public void reportRemovedIntel() {
		super.reportRemovedIntel();
		if (Global.getSector() != null) Global.getSector().removeScript(this);
	}


}
