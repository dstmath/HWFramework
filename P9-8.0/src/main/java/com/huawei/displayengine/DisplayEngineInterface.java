package com.huawei.displayengine;

import android.os.Bundle;
import android.os.PersistableBundle;
import com.huawei.android.os.BuildEx.VERSION_CODES;
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
        if (mIDsMap == null) {
            synchronized (mLock) {
                if (mIDsMap == null) {
                    Map<String, Integer> map = new HashMap();
                    map.put("FEATURE_SHARP", Integer.valueOf(0));
                    map.put("FEATURE_CONTRAST", Integer.valueOf(1));
                    map.put("FEATURE_BLC", Integer.valueOf(2));
                    map.put("FEATURE_GMP", Integer.valueOf(3));
                    map.put("FEATURE_XCC", Integer.valueOf(4));
                    map.put("FEATURE_HUE", Integer.valueOf(5));
                    map.put("FEATURE_SAT", Integer.valueOf(6));
                    map.put("FEATURE_GAMMA", Integer.valueOf(7));
                    map.put("FEATURE_IGAMMA", Integer.valueOf(8));
                    map.put("FEATURE_LRE", Integer.valueOf(9));
                    map.put("FEATURE_SRE", Integer.valueOf(10));
                    map.put("FEATURE_COLORMODE", Integer.valueOf(11));
                    map.put("FEATURE_CABC", Integer.valueOf(12));
                    map.put("FEATURE_RGBW", Integer.valueOf(13));
                    map.put("FEATURE_PANELINFO", Integer.valueOf(14));
                    map.put("FEATURE_HDR10", Integer.valueOf(15));
                    map.put("FEATURE_XNIT", Integer.valueOf(16));
                    map.put("FEATURE_SHARP2P", Integer.valueOf(22));
                    map.put("SCENE_PG", Integer.valueOf(0));
                    map.put("SCENE_VIDEO", Integer.valueOf(1));
                    map.put("SCENE_VIDEO_HDR10", Integer.valueOf(2));
                    map.put("SCENE_IMAGE", Integer.valueOf(3));
                    map.put("SCENE_CAMERA", Integer.valueOf(4));
                    map.put("SCENE_UI", Integer.valueOf(5));
                    map.put("SCENE_WEB", Integer.valueOf(6));
                    map.put("SCENE_WECHAT", Integer.valueOf(7));
                    map.put("SCENE_QQ", Integer.valueOf(8));
                    map.put("SCENE_TAOBAO", Integer.valueOf(9));
                    map.put("SCENE_POWERMODE", Integer.valueOf(10));
                    map.put("SCENE_REAL_POWERMODE", Integer.valueOf(24));
                    map.put("SCENE_COLORTEMP", Integer.valueOf(11));
                    map.put("SCENE_SRE", Integer.valueOf(12));
                    map.put("SCENE_COLORMODE", Integer.valueOf(13));
                    map.put("SCENE_PROCAMERA", Integer.valueOf(14));
                    map.put("SCENE_EYEPROTECTION", Integer.valueOf(15));
                    map.put("SCENE_XNIT", Integer.valueOf(16));
                    map.put("SCENE_VIDEO_APP", Integer.valueOf(23));
                    map.put("ACTION_START", Integer.valueOf(0));
                    map.put("ACTION_STOP", Integer.valueOf(1));
                    map.put("ACTION_PAUSE", Integer.valueOf(2));
                    map.put("ACTION_RESUME", Integer.valueOf(3));
                    map.put("ACTION_FULLSCREEN_START", Integer.valueOf(4));
                    map.put("ACTION_FULLSCREEN_STOP", Integer.valueOf(5));
                    map.put("ACTION_FULLSCREEN_PAUSE", Integer.valueOf(6));
                    map.put("ACTION_FULLSCREEN_RESUME", Integer.valueOf(7));
                    map.put("ACTION_FULLSCREEN_EXIT", Integer.valueOf(8));
                    map.put("ACTION_THUMBNAIL", Integer.valueOf(9));
                    map.put("ACTION_FULLSCREEN_VIEW", Integer.valueOf(10));
                    map.put("ACTION_LIVE_IMAGE", Integer.valueOf(11));
                    map.put("ACTION_ONLINE_FULLSCREEN_VIEW", Integer.valueOf(12));
                    map.put("ACTION_IMAGE_EXIT", Integer.valueOf(13));
                    map.put("ACTION_ENTER", Integer.valueOf(14));
                    map.put("ACTION_EXIT", Integer.valueOf(15));
                    map.put("ACTION_MODE_ON", Integer.valueOf(16));
                    map.put("ACTION_MODE_OFF", Integer.valueOf(17));
                    map.put("ACTION_PG_DEFAULT_FRONT", Integer.valueOf(VERSION_CODES.CUR_DEVELOPMENT));
                    map.put("ACTION_PG_BROWSER_FRONT", Integer.valueOf(10001));
                    map.put("ACTION_PG_3DGAME_FRONT", Integer.valueOf(10002));
                    map.put("ACTION_PG_EBOOK_FRONT", Integer.valueOf(10003));
                    map.put("ACTION_PG_GALLERY_FRONT", Integer.valueOf(10004));
                    map.put("ACTION_PG_INPUT_START", Integer.valueOf(10005));
                    map.put("ACTION_PG_INPUT_END", Integer.valueOf(10006));
                    map.put("ACTION_PG_CAMERA_FRONT", Integer.valueOf(10007));
                    map.put("ACTION_PG_OFFICE_FRONT", Integer.valueOf(10008));
                    map.put("ACTION_PG_VIDEO_FRONT", Integer.valueOf(10009));
                    map.put("ACTION_PG_LAUNCHER_FRONT", Integer.valueOf(10010));
                    map.put("ACTION_PG_2DGAME_FRONT", Integer.valueOf(10011));
                    map.put("ACTION_PG_MMS_FRONT", Integer.valueOf(10013));
                    map.put("ACTION_PG_VIDEO_START", Integer.valueOf(10015));
                    map.put("ACTION_PG_VIDEO_END", Integer.valueOf(10016));
                    map.put("ACTION_PG_CAMERA_END", Integer.valueOf(10017));
                    map.put("DATA_TYPE_IMAGE", Integer.valueOf(0));
                    map.put("DATA_TYPE_VIDEO", Integer.valueOf(1));
                    map.put("DATA_TYPE_VIDEO_HDR10", Integer.valueOf(2));
                    map.put("DATA_TYPE_CAMERA", Integer.valueOf(3));
                    map.put("DATA_TYPE_IMAGE_INFO", Integer.valueOf(4));
                    map.put("DATA_TYPE_XNIT", Integer.valueOf(5));
                    map.put("MESSAGE_ID_IMAGE", Integer.valueOf(0));
                    map.put("MESSAGE_ID_VIDEO", Integer.valueOf(1));
                    map.put("MESSAGE_ID_CUSTOM", Integer.valueOf(2));
                    mIDsMap = map;
                }
            }
        }
        if (mIDsMap.containsKey(name)) {
            return ((Integer) mIDsMap.get(name)).intValue();
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
