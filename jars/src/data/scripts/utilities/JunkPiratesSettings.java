package data.scripts.utilities;

import com.fs.starfarer.api.Global;
import java.lang.reflect.Method;
import org.json.JSONObject;

public final class JunkPiratesSettings {
    private static final String MOD_ID = "junk_pirates_release";
    private static final String LEGACY_SETTINGS_PATH = "mendoncaModSettings.ini";

    private static JSONObject legacySettings;
    private static boolean legacySettingsLoaded = false;

    private JunkPiratesSettings() {
    }

    public static boolean getBoolean(String fieldId, boolean fallback) {
        Object lunaValue = getLunaValue("getBoolean", fieldId);
        if (lunaValue instanceof Boolean) {
            return (Boolean) lunaValue;
        }

        JSONObject settings = getLegacySettings();
        return settings != null ? settings.optBoolean(fieldId, fallback) : fallback;
    }

    public static int getInt(String fieldId, int fallback) {
        Object lunaValue = getLunaValue("getInt", fieldId);
        if (lunaValue instanceof Number) {
            return ((Number) lunaValue).intValue();
        }

        JSONObject settings = getLegacySettings();
        return settings != null ? settings.optInt(fieldId, fallback) : fallback;
    }

    public static float getFloat(String fieldId, float fallback) {
        Object lunaValue = getLunaValue("getDouble", fieldId);
        if (lunaValue instanceof Number) {
            return ((Number) lunaValue).floatValue();
        }

        JSONObject settings = getLegacySettings();
        return settings != null ? (float) settings.optDouble(fieldId, fallback) : fallback;
    }

    private static Object getLunaValue(String methodName, String fieldId) {
        try {
            if (!Global.getSettings().getModManager().isModEnabled("lunalib")) {
                return null;
            }

            Class<?> lunaSettings = Class.forName("lunalib.lunaSettings.LunaSettings");
            Method getter = lunaSettings.getMethod(methodName, String.class, String.class);
            return getter.invoke(null, MOD_ID, fieldId);
        } catch (Throwable ex) {
            return null;
        }
    }

    private static JSONObject getLegacySettings() {
        if (legacySettingsLoaded) {
            return legacySettings;
        }

        legacySettingsLoaded = true;
        try {
            legacySettings = Global.getSettings().loadJSON(LEGACY_SETTINGS_PATH);
        } catch (Throwable ex) {
            Global.getLogger(JunkPiratesSettings.class).warn("Unable to load Junk Pirates legacy settings", ex);
        }
        return legacySettings;
    }
}
