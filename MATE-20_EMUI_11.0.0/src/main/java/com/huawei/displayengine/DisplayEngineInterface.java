package com.huawei.displayengine;

import android.os.Bundle;
import android.os.PersistableBundle;
import com.huawei.android.pgmng.plug.PowerKit;
import java.util.HashMap;
import java.util.Map;

public class DisplayEngineInterface {
    public static final String ACTION_ABORT = "ACTION_ABORT";
    public static final String ACTION_FINISH = "ACTION_FINISH";
    public static final int DE_NOT_SUPPORT = 0;
    public static final int DE_SUPPORT = 1;
    public static final String EFFECT_TYPE_CALIB_CHECK = "EFFECT_TYPE_CALIB_CHECK";
    public static final String EFFECT_TYPE_CALIB_INFO = "EFFECT_TYPE_CALIB_INFO";
    public static final String EFFECT_TYPE_CALIB_LVLS = "EFFECT_TYPE_CALIB_LVLS";
    public static final String EFFECT_TYPE_CALIB_TIME = "EFFECT_TYPE_CALIB_TIME";
    public static final String EFFECT_TYPE_PANEL_INFO = "EFFECT_TYPE_PANEL_INFO";
    public static final String FEATURE_FOLDINGCOMPENSATION = "FEATURE_FOLDINGCOMPENSATION";
    private static final Map<String, Integer> ID_MAP = new HashMap<String, Integer>(120) {
        /* class com.huawei.displayengine.DisplayEngineInterface.AnonymousClass1 */

        {
            put("FEATURE_SHARP", 0);
            put("FEATURE_CONTRAST", 1);
            put("FEATURE_BLC", 2);
            put("FEATURE_GMP", 3);
            put("FEATURE_XCC", 4);
            put("FEATURE_HUE", 5);
            put("FEATURE_SAT", 6);
            put("FEATURE_GAMMA", 7);
            put("FEATURE_IGAMMA", 8);
            put("FEATURE_LRE", 9);
            put("FEATURE_SRE", 10);
            put("FEATURE_COLORMODE", 11);
            put("FEATURE_CABC", 12);
            put("FEATURE_RGBW", 13);
            put("FEATURE_PANELINFO", 14);
            put("FEATURE_HDR10", 15);
            put("FEATURE_XNIT", 16);
            put("FEATURE_SHARP2P", 22);
            put("FEATURE_READMODE", 26);
            put("FEATURE_DALTONIAN", 27);
            put("FEATURE_COLOR_INVERSE", 28);
            put("FEATURE_FILM_FILTER", 30);
            put("FEATURE_SCREEN_TIME_CONTROL", 31);
            put("FEATURE_DC_BRIGHTNESS_DIMMING", 32);
            put(DisplayEngineInterface.FEATURE_FOLDINGCOMPENSATION, 33);
            put("FEATURE_READING_GLOBAL", 34);
            put("FEATURE_GAME_DISABLE_AUTO_BRIGHTNESS_MODE", 36);
            put("FEATURE_PANELCOLORCONSISTENCY", 37);
            put("SCENE_VIDEO", 1);
            put("SCENE_VIDEO_HDR10", 2);
            put("SCENE_IMAGE", 3);
            put("SCENE_CAMERA", 4);
            put("SCENE_UI", 5);
            put("SCENE_WEB", 6);
            put("SCENE_REAL_POWERMODE", 24);
            put("SCENE_COLORTEMP", 11);
            put("SCENE_SRE", 12);
            put("SCENE_PROCAMERA", 14);
            put("SCENE_EYEPROTECTION", 15);
            put("SCENE_VIDEO_APP", 23);
            put("SCENE_AOD", 33);
            put("SCENE_READMODE", 37);
            put("SCENE_READMODE_GLOBAL", 48);
            put("SCENE_DALTONIAN", 39);
            put("SCENE_COLOR_INVERSE", 40);
            put("SCENE_FILM_FILTER", 41);
            put("SCENE_SCREEN_TIME_CONTROL", 42);
            put(DisplayEngineInterface.SCENE_FOLD_CALIB, 46);
            put("SCENE_COLORCONSISTENCY_CALIB", 51);
            put("SCENE_GAME_DISABLE_AUTO_BRIGHTNESS_MODE", 49);
            put("ACTION_START", 0);
            put("ACTION_STOP", 1);
            put("ACTION_PAUSE", 2);
            put("ACTION_RESUME", 3);
            put("ACTION_FULLSCREEN_START", 4);
            put("ACTION_FULLSCREEN_STOP", 5);
            put("ACTION_FULLSCREEN_PAUSE", 6);
            put("ACTION_FULLSCREEN_RESUME", 7);
            put("ACTION_FULLSCREEN_EXIT", 8);
            put("ACTION_THUMBNAIL", 9);
            put("ACTION_FULLSCREEN_VIEW", 10);
            put("ACTION_LIVE_IMAGE", 11);
            put("ACTION_ONLINE_FULLSCREEN_VIEW", 12);
            put("ACTION_IMAGE_EXIT", 13);
            put("ACTION_ENTER", 14);
            put("ACTION_EXIT", 15);
            put("ACTION_MODE_ON", 16);
            put("ACTION_MODE_OFF", 17);
            put("ACTION_DALTONIAN_DEU", 25);
            put("ACTION_DALTONIAN_PRO", 26);
            put("ACTION_DALTONIAN_TRI", 27);
            put("ACTION_DALTONIAN_SIM_ALL", 31);
            put("ACTION_DALTONIAN_SIM_DEU", 32);
            put("ACTION_DALTONIAN_SIM_PRO", 33);
            put("ACTION_DALTONIAN_SIM_TRI", 34);
            put("ACTION_FILM_COLOR_FILTER_OR", 17);
            put("ACTION_FILM_COLOR_FILTER_A1", 36);
            put("ACTION_FILM_COLOR_FILTER_A2", 37);
            put("ACTION_FILM_COLOR_FILTER_A3", 38);
            put("ACTION_FILM_COLOR_FILTER_B1", 39);
            put("ACTION_FILM_COLOR_FILTER_B2", 40);
            put("ACTION_FILM_COLOR_FILTER_B3", 41);
            put("ACTION_FILM_COLOR_FILTER_C1", 42);
            put("ACTION_FILM_COLOR_FILTER_C2", 43);
            put("ACTION_FILM_COLOR_FILTER_C3", 44);
            put("ACTION_FILM_COLOR_FILTER_F1", 45);
            put("ACTION_FILM_COLOR_FILTER_F2", 46);
            put("ACTION_FILM_COLOR_FILTER_F3", 47);
            put("ACTION_FILM_COLOR_FILTER_O1", 52);
            put(DisplayEngineInterface.ACTION_FINISH, 50);
            put(DisplayEngineInterface.ACTION_ABORT, 51);
            put("ACTION_PG_DEFAULT_FRONT", 10000);
            put("ACTION_PG_BROWSER_FRONT", Integer.valueOf((int) PowerKit.PG_ID_BROWSER_FRONT));
            put("ACTION_PG_3DGAME_FRONT", Integer.valueOf((int) PowerKit.PG_ID_3DGAME_FRONT));
            put("ACTION_PG_EBOOK_FRONT", Integer.valueOf((int) PowerKit.PG_ID_EBOOK_FRONT));
            put("ACTION_PG_GALLERY_FRONT", Integer.valueOf((int) PowerKit.PG_ID_GALLERY_FRONT));
            put("ACTION_PG_INPUT_START", Integer.valueOf((int) PowerKit.PG_ID_INPUT_START));
            put("ACTION_PG_INPUT_END", 10006);
            put("ACTION_PG_CAMERA_FRONT", 10007);
            put("ACTION_PG_OFFICE_FRONT", Integer.valueOf((int) PowerKit.PG_ID_OFFICE_FRONT));
            put("ACTION_PG_VIDEO_FRONT", Integer.valueOf((int) PowerKit.PG_ID_VIDEO_FRONT));
            put("ACTION_PG_LAUNCHER_FRONT", Integer.valueOf((int) PowerKit.PG_ID_LAUNCHER_FRONT));
            put("ACTION_PG_2DGAME_FRONT", Integer.valueOf((int) PowerKit.PG_ID_2DGAME_FRONT));
            put("ACTION_PG_MMS_FRONT", 10013);
            put("ACTION_PG_VIDEO_START", 10015);
            put("ACTION_PG_VIDEO_END", 10016);
            put("ACTION_PG_CAMERA_END", 10017);
            put("DATA_TYPE_IMAGE", 0);
            put("DATA_TYPE_VIDEO", 1);
            put("DATA_TYPE_VIDEO_HDR10", 2);
            put("DATA_TYPE_CAMERA", 3);
            put("DATA_TYPE_SUPPORTED_FILTERS", 12);
            put("DE_DATA_TYPE_FILM_FILTER", 16);
            put(DisplayEngineInterface.EFFECT_TYPE_PANEL_INFO, 2);
            put(DisplayEngineInterface.EFFECT_TYPE_CALIB_INFO, 6);
            put(DisplayEngineInterface.EFFECT_TYPE_CALIB_CHECK, 7);
            put(DisplayEngineInterface.EFFECT_TYPE_CALIB_LVLS, 8);
            put(DisplayEngineInterface.EFFECT_TYPE_CALIB_TIME, 9);
            put("EFFECT_TYPE_CALIB_RANGE", 14);
            put("EFFECT_TYPE_DISPLAY_SWITCH", 15);
            put("EFFECT_TYPE_CUSTOM", 11);
            put("EFFECT_TYPE_CUSTOM_EXIT", 12);
            put("MESSAGE_ID_CUSTOM", 2);
        }
    };
    private static final int INVALID_ID = -1;
    public static final int RETURN_ERROR = -1;
    public static final int RETURN_PARAMETER_INVALID = -2;
    public static final int RETURN_SUCCESS = 0;
    public static final String SCENE_FOLD_CALIB = "SCENE_FOLD_CALIB";
    private DisplayEngineManager mManager = new DisplayEngineManager();

    public int getSupported(String feature) {
        int id = getId(feature);
        if (id != -1) {
            return this.mManager.getSupported(id);
        }
        return 0;
    }

    public int setScene(String scene, String action) {
        int sceneId = getId(scene);
        int actionId = getId(action);
        if (sceneId == -1 || actionId == -1) {
            return -2;
        }
        return this.mManager.setScene(sceneId, actionId);
    }

    public void updateLightSensorState(boolean isSensorEnable) {
        this.mManager.updateLightSensorState(isSensorEnable);
    }

    public int setData(String type, PersistableBundle data) {
        int id = getId(type);
        if (id != -1) {
            return this.mManager.setData(id, data);
        }
        return -2;
    }

    public int sendMessage(String message, Bundle data) {
        int id = getId(message);
        if (id != -1) {
            return this.mManager.sendMessage(id, data);
        }
        return -2;
    }

    public int getEffect(String feature, String mode, Bundle data) {
        int featureId = getId(feature);
        int typeId = getId(mode);
        if (featureId == -1 || typeId == -1) {
            return -2;
        }
        return this.mManager.getEffect(featureId, typeId, data);
    }

    public int getEffect(String feature, String type, byte[] status, int length) {
        int featureId = getId(feature);
        int typeId = getId(type);
        if (featureId == -1 || typeId == -1) {
            return -2;
        }
        return this.mManager.getEffect(featureId, typeId, status, length);
    }

    public int setEffect(String feature, String mode, PersistableBundle data) {
        int featureId = getId(feature);
        int modeId = getId(mode);
        if (featureId == -1 || modeId == -1) {
            return -2;
        }
        return this.mManager.setEffect(featureId, modeId, data);
    }

    public Object imageProcess(String command, Map<String, Object> param) {
        return this.mManager.imageProcess(command, param);
    }

    private int getId(String name) {
        if (ID_MAP.containsKey(name)) {
            return ID_MAP.get(name).intValue();
        }
        return -1;
    }
}
