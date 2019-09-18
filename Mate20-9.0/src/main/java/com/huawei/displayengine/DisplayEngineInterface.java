package com.huawei.displayengine;

import android.os.Bundle;
import android.os.PersistableBundle;
import com.huawei.android.pgmng.plug.PGAction;
import java.util.HashMap;
import java.util.Map;

public class DisplayEngineInterface {
    public static final int DE_NOT_SUPPORT = 0;
    public static final int DE_SUPPORT = 1;
    public static final int RETURN_ERROR = -1;
    public static final int RETURN_PARAMETER_INVALID = -2;
    public static final int RETURN_SUCCESS = 0;
    private static final String TAG = "DE J DisplayEngineInterface";
    private static volatile Map<String, Integer> mIDsMap = null;
    private static Object mLock = new Object();
    private DisplayEngineManager mManager = new DisplayEngineManager();

    private int getID(String name) {
        String str = name;
        if (mIDsMap == null) {
            synchronized (mLock) {
                if (mIDsMap == null) {
                    Map<String, Integer> map = new HashMap<>();
                    map.put("FEATURE_SHARP", 0);
                    map.put("FEATURE_CONTRAST", 1);
                    map.put("FEATURE_BLC", 2);
                    map.put("FEATURE_GMP", 3);
                    map.put("FEATURE_XCC", 4);
                    map.put("FEATURE_HUE", 5);
                    map.put("FEATURE_SAT", 6);
                    map.put("FEATURE_GAMMA", 7);
                    map.put("FEATURE_IGAMMA", 8);
                    map.put("FEATURE_LRE", 9);
                    map.put("FEATURE_SRE", 10);
                    map.put("FEATURE_COLORMODE", 11);
                    map.put("FEATURE_CABC", 12);
                    map.put("FEATURE_RGBW", 13);
                    map.put("FEATURE_PANELINFO", 14);
                    map.put("FEATURE_HDR10", 15);
                    map.put("FEATURE_XNIT", 16);
                    map.put("FEATURE_SHARP2P", 22);
                    map.put("FEATURE_READMODE", 26);
                    map.put("FEATURE_DALTONIAN", 27);
                    map.put("FEATURE_COLOR_INVERSE", 28);
                    map.put("FEATURE_DC_BRIGHTNESS_DIMMING", 30);
                    map.put("FEATURE_SCREEN_TIME_CONTROL", 31);
                    map.put("SCENE_VIDEO", 1);
                    map.put("SCENE_VIDEO_HDR10", 2);
                    map.put("SCENE_IMAGE", 3);
                    map.put("SCENE_CAMERA", 4);
                    map.put("SCENE_UI", 5);
                    map.put("SCENE_WEB", 6);
                    map.put("SCENE_REAL_POWERMODE", 24);
                    map.put("SCENE_COLORTEMP", 11);
                    map.put("SCENE_SRE", 12);
                    map.put("SCENE_PROCAMERA", 14);
                    map.put("SCENE_EYEPROTECTION", 15);
                    map.put("SCENE_VIDEO_APP", 23);
                    map.put("SCENE_AOD", 33);
                    map.put("SCENE_READMODE", 37);
                    map.put("SCENE_DALTONIAN", 39);
                    map.put("SCENE_COLOR_INVERSE", 40);
                    map.put("SCENE_SCREEN_TIME_CONTROL", 42);
                    map.put("ACTION_START", 0);
                    map.put("ACTION_STOP", 1);
                    map.put("ACTION_PAUSE", 2);
                    map.put("ACTION_RESUME", 3);
                    map.put("ACTION_FULLSCREEN_START", 4);
                    map.put("ACTION_FULLSCREEN_STOP", 5);
                    map.put("ACTION_FULLSCREEN_PAUSE", 6);
                    map.put("ACTION_FULLSCREEN_RESUME", 7);
                    map.put("ACTION_FULLSCREEN_EXIT", 8);
                    map.put("ACTION_THUMBNAIL", 9);
                    map.put("ACTION_FULLSCREEN_VIEW", 10);
                    map.put("ACTION_LIVE_IMAGE", 11);
                    map.put("ACTION_ONLINE_FULLSCREEN_VIEW", 12);
                    map.put("ACTION_IMAGE_EXIT", 13);
                    map.put("ACTION_ENTER", 14);
                    map.put("ACTION_EXIT", 15);
                    map.put("ACTION_MODE_ON", 16);
                    map.put("ACTION_MODE_OFF", 17);
                    map.put("ACTION_DALTONIAN_DEU", 25);
                    map.put("ACTION_DALTONIAN_PRO", 26);
                    map.put("ACTION_DALTONIAN_TRI", 27);
                    map.put("ACTION_PG_DEFAULT_FRONT", 10000);
                    map.put("ACTION_PG_BROWSER_FRONT", Integer.valueOf(PGAction.PG_ID_BROWSER_FRONT));
                    map.put("ACTION_PG_3DGAME_FRONT", Integer.valueOf(PGAction.PG_ID_3DGAME_FRONT));
                    map.put("ACTION_PG_EBOOK_FRONT", Integer.valueOf(PGAction.PG_ID_EBOOK_FRONT));
                    map.put("ACTION_PG_GALLERY_FRONT", Integer.valueOf(PGAction.PG_ID_GALLERY_FRONT));
                    map.put("ACTION_PG_INPUT_START", Integer.valueOf(PGAction.PG_ID_INPUT_START));
                    map.put("ACTION_PG_INPUT_END", 10006);
                    map.put("ACTION_PG_CAMERA_FRONT", 10007);
                    map.put("ACTION_PG_OFFICE_FRONT", Integer.valueOf(PGAction.PG_ID_OFFICE_FRONT));
                    map.put("ACTION_PG_VIDEO_FRONT", Integer.valueOf(PGAction.PG_ID_VIDEO_FRONT));
                    map.put("ACTION_PG_LAUNCHER_FRONT", Integer.valueOf(PGAction.PG_ID_LAUNCHER_FRONT));
                    map.put("ACTION_PG_2DGAME_FRONT", Integer.valueOf(PGAction.PG_ID_2DGAME_FRONT));
                    map.put("ACTION_PG_MMS_FRONT", 10013);
                    map.put("ACTION_PG_VIDEO_START", 10015);
                    map.put("ACTION_PG_VIDEO_END", 10016);
                    map.put("ACTION_PG_CAMERA_END", 10017);
                    map.put("DATA_TYPE_IMAGE", 0);
                    map.put("DATA_TYPE_VIDEO", 1);
                    map.put("DATA_TYPE_VIDEO_HDR10", 2);
                    map.put("DATA_TYPE_CAMERA", 3);
                    map.put("MESSAGE_ID_CUSTOM", 2);
                    mIDsMap = map;
                }
            }
        }
        if (mIDsMap.containsKey(str)) {
            return mIDsMap.get(str).intValue();
        }
        return -1;
    }

    public int getSupported(String feature) {
        int id = getID(feature);
        if (id != -1) {
            return this.mManager.getSupported(id);
        }
        return 0;
    }

    public int setScene(String scene, String action) {
        int sId = getID(scene);
        int aId = getID(action);
        if (sId == -1 || aId == -1) {
            return -2;
        }
        return this.mManager.setScene(sId, aId);
    }

    public void updateLightSensorState(boolean sensorEnable) {
        this.mManager.updateLightSensorState(sensorEnable);
    }

    public int setData(String type, PersistableBundle data) {
        int id = getID(type);
        if (id != -1) {
            return this.mManager.setData(id, data);
        }
        return -2;
    }

    public int sendMessage(String messageID, Bundle data) {
        int id = getID(messageID);
        if (id != -1) {
            return this.mManager.sendMessage(id, data);
        }
        return -2;
    }

    public int getEffect(String feature, String type, byte[] status, int length) {
        int fId = getID(feature);
        int tId = getID(type);
        if (fId == -1 || tId == -1) {
            return -2;
        }
        return this.mManager.getEffect(fId, tId, status, length);
    }

    public int setEffect(String feature, String mode, PersistableBundle data) {
        int fId = getID(feature);
        int mId = getID(mode);
        if (fId == -1 || mId == -1) {
            return -2;
        }
        return this.mManager.setEffect(fId, mId, data);
    }

    public Object imageProcess(String command, Map<String, Object> param) {
        return this.mManager.imageProcess(command, param);
    }
}
