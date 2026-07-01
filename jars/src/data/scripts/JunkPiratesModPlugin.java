package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SectorThemeGenerator;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.campaign.fleets.JunkPiratesExplorerFleetManager;
import data.scripts.campaign.fleets.SyndicateAspFleetManager;
import data.scripts.campaign.fleets.SyndicateAspHitSquadFleetManager;
import data.scripts.campaign.procgen.themes.JunkPiratesAnarchistThemeGenerator;
import data.scripts.utilities.JunkPiratesSettings;
// Import every entry from your mod's data/world/generators.csv
import data.scripts.world.JunkGen;
//import data.scripts.omnifac.AddOmniFac;

import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.TextureData;
import exerelin.campaign.SectorManager;
import java.util.ArrayList;
import java.util.List;

public class JunkPiratesModPlugin extends BaseModPlugin
{
    private static final String MEMKEY_GENERATED = "$junkPiratesGenerated";
    private static final String MEMKEY_ASP_WANTED = "$playerIsAspWanted";
    private static final String ENTITY_BREHINNI_STAR = "brehinni";
    private static final String ENTITY_CANIS_STAR = "canis";
    private static final String ENTITY_URSULO_STAR = "ursulo";
    private static final String ENTITY_GLORY = "glory";
    private static final String ENTITY_KARKOV_ACADEMY = "karkov_academy";
    private static final String ENTITY_PETRA = "petra";
    private static final String ENTITY_PADDINGTON = "paddington";

    public static final boolean isExerelin;
    public static final boolean isIndEvo;
    
    public static boolean enableASP;
    public static boolean enableASPCourierFleets;
    public static boolean enableASPHitSquads;
    public static boolean enablePACK;
    public static boolean enablePACKDiplomats;
    public static boolean enableJunkPirates;
    public static boolean enableJunkExplorers;
    
    public static boolean enableJunkPiratesIntel;
    
    public static float junkPiratesFleetFrequencyModifier;
    public static float junkPiratesMaxFleetModifier;
    
//    
//    public static int minAnarchistConstellations;
//    public static int maxAnarchistConstellations;
//
//    public static int softMaxSpinerettes;
//
//    public static boolean enableProcGen;
//    public static boolean enableSpinerettes;
    
    static
    {
        boolean foundExerelin;
        try
        {
            Global.getSettings().getScriptClassLoader().loadClass("data.scripts.world.ExerelinGen");
            foundExerelin = true;
        }
        catch (ClassNotFoundException ex)
        {
            foundExerelin = false;
        }
        isExerelin = foundExerelin;
    }
    
    static
    {
        boolean foundIndEvo;
        try
        {
            Global.getSettings().getScriptClassLoader().loadClass("com.fs.starfarer.api.impl.campaign.ids.IndEvo_ids");
            foundIndEvo = true;
        }
        catch (ClassNotFoundException ex)
        {
            try
            {
                Global.getSettings().getScriptClassLoader().loadClass("indevo.ids.Ids");
                foundIndEvo = true;
            }
            catch (ClassNotFoundException ex2)
            {
                foundIndEvo = false;
            }
        }
        isIndEvo = foundIndEvo;
    }
    
    
    
    private static void getProcGenSettings() {
        enableASP = JunkPiratesSettings.getBoolean("enableASP", true);
        enableASPCourierFleets = JunkPiratesSettings.getBoolean("enableASPCourierFleets", true);
        enableASPHitSquads = JunkPiratesSettings.getBoolean("enableASPHitSquads", true);
        enablePACK = JunkPiratesSettings.getBoolean("enablePACK", true);
        enablePACKDiplomats = JunkPiratesSettings.getBoolean("enablePACKDiplomats", true);
        enableJunkPirates = JunkPiratesSettings.getBoolean("enableJunkPirates", true);
        enableJunkExplorers = JunkPiratesSettings.getBoolean("enableJunkExplorers", true);

        enableJunkPiratesIntel = JunkPiratesSettings.getBoolean("enableJunkPiratesIntel", false);

        junkPiratesFleetFrequencyModifier = JunkPiratesSettings.getFloat("junkPiratesFleetFrequencyModifier", 1f);
        junkPiratesMaxFleetModifier = JunkPiratesSettings.getFloat("junkPiratesMaxFleetModifier", 1f);
    }
    
    private static void initJunkPirates() {
        ensureBountyParticipation();
        new JunkGen().generate(Global.getSector());
        Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_GENERATED, true);
    }

    private static boolean isNexRandomMode() {
        if (!isExerelin) return false;
        try {
            return !SectorManager.getCorvusMode();
        } catch (Throwable ex) {
            return false;
        }
    }

    private static boolean shouldGenerateJunkPiratesSystems() {
        return !isNexRandomMode();
    }

    private static boolean hasEntity(String id) {
        return Global.getSector() != null && Global.getSector().getEntityById(id) != null;
    }

    private static boolean hasAnyJunkPiratesCoreWorlds() {
        return hasEntity(ENTITY_BREHINNI_STAR)
                || hasEntity(ENTITY_CANIS_STAR)
                || hasEntity(ENTITY_URSULO_STAR)
                || hasEntity(ENTITY_GLORY)
                || hasEntity(ENTITY_KARKOV_ACADEMY)
                || hasEntity(ENTITY_PETRA)
                || hasEntity(ENTITY_PADDINGTON);
    }

    private static void generateJunkPiratesIfNeeded() {
        if (!shouldGenerateJunkPiratesSystems()) {
            return;
        }
        if (!hasAnyJunkPiratesCoreWorlds()) {
            initJunkPirates();
            return;
        }
        Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_GENERATED, true);
    }

    private static void ensureBountyParticipation() {
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("junk_pirates");
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("pack");
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("syndicate_asp");
    }

    private static void ensureGlobalScripts() {
        if (enableASPCourierFleets && !Global.getSector().hasScript(SyndicateAspFleetManager.class)) {
            Global.getSector().addScript(new SyndicateAspFleetManager());
        }

        if (enableASPCourierFleets && enableASPHitSquads) {
            if (!Global.getSector().getMemoryWithoutUpdate().contains(MEMKEY_ASP_WANTED)) {
                Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_ASP_WANTED, false);
            }
            if (!Global.getSector().hasScript(SyndicateAspHitSquadFleetManager.class)) {
                Global.getSector().addScript(new SyndicateAspHitSquadFleetManager());
            }
        }

        if (enableJunkExplorers && !Global.getSector().hasScript(JunkPiratesExplorerFleetManager.class)) {
            Global.getSector().addScript(new JunkPiratesExplorerFleetManager());
        }
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        getProcGenSettings();
        ensureBountyParticipation();
        if (isNexRandomMode()) {
            addNexerelinRandommodeShips();
        }
        ensureGlobalScripts();
        
        
//        if (enablePACKDiplomats) {
//            to do
//        }
//        if (enableJunkExplorers) {
//            to do
//        }
                         
    }
    @Override
    public void onNewGame() {
        getProcGenSettings();
        ensureBountyParticipation();
        generateJunkPiratesIfNeeded();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        getProcGenSettings();
        ensureBountyParticipation();
        if (!Global.getSector().getMemoryWithoutUpdate().contains(MEMKEY_GENERATED)
                || !hasAnyJunkPiratesCoreWorlds()) {
            generateJunkPiratesIfNeeded();
        }
        ensureGlobalScripts();
    }
    
    private static void ensureThemeGeneratorRegistered() {
        for (Object generator : SectorThemeGenerator.generators) {
            if (generator instanceof JunkPiratesAnarchistThemeGenerator) return;
        }
        int index = Math.min(1, SectorThemeGenerator.generators.size());
        SectorThemeGenerator.generators.add(index, new JunkPiratesAnarchistThemeGenerator());
    }

    public static void addNexerelinRandommodeShips() {
       
        List<String> aspships = new ArrayList<String>();
        
        aspships.add("syndicate_asp_hognose_p");
        aspships.add("syndicate_asp_kingcobra_p");
        aspships.add("syndicate_asp_diamondback_p");
        aspships.add("syndicate_asp_copperhead_p");
        aspships.add("syndicate_asp_gigantophis_p");
        
        FactionAPI faction = Global.getSector().getFaction("syndicate_asp");
        if (faction == null) return;
        for (String id : aspships) {
            if (faction.knowsShip(id)) continue;
            faction.addKnownShip(id, true);
        }
    }


//    @Override
//    public void onNewGame()
//    {
//        // Calling a separate method avoids duplicate code with onEnabled()
//        initJunkPirates();
//    }

    @Override  
    public void onApplicationLoad()
    {  
    
        ensureThemeGeneratorRegistered();
        
        getProcGenSettings();
        
        ShaderLib.init();  
        LightData.readLightDataCSV("data/lights/junk_pirates_light_data.csv");  
        TextureData.readTextureDataCSV("data/lights/junk_pirates_texture_data.csv");  
    }
}
