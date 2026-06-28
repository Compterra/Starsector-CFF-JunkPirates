/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.utilities;

import com.fs.starfarer.api.Global;
import java.io.IOException;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author paul
 */
public class junkPiratesConfig {
        public static JSONArray spineretteSystemWhitelist;
        public static JSONArray spineretteTagWhitelist;
        public static JSONArray spineretteSystemBlacklist;
        public static JSONArray spineretteTagBlacklist;

    public static void loadJunkPiratesModConfig() {
        
        try {
        JSONObject junkPiratesWhitelists = Global.getSettings().getMergedJSONForMod("data/config/jpConfig/junk_pirates_Config.json", "junk_pirates_release");

            spineretteSystemWhitelist = junkPiratesWhitelists.optJSONArray("spineretteSystemWhitelist");
            spineretteTagWhitelist = junkPiratesWhitelists.optJSONArray("spineretteTagWhitelist");
            spineretteSystemBlacklist = junkPiratesWhitelists.optJSONArray("spineretteSystemBlacklist");
            spineretteTagBlacklist = junkPiratesWhitelists.optJSONArray("spineretteTagBlacklist");
            
        } catch (IOException | JSONException ex) {
            Global.getLogger(junkPiratesConfig.class).warn("JP Config Exception", ex);
        }
    }
}

