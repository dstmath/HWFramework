package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.Map;

public class LCDSchedFeatureRT extends DevSchedFeatureBase {
    private static final String TAG = "LCDSchedFeatureRT";
    private static final Map<Integer, Integer> sPgSceneToPreRecongMap = new ArrayMap();

    static {
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_DIRECT_SWAPPINESS), 1);
        sPgSceneToPreRecongMap.put(304, 19);
        sPgSceneToPreRecongMap.put(305, 9);
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_PROTECTLRU_SWITCH), 18);
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_PROTECTLRU_SET_PROTECTRATIO), 6);
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE), 8);
        sPgSceneToPreRecongMap.put(310, 5);
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_SET_PREREAD_PATH), 0);
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_PREREAD_DATA_REMOVE), 7);
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_COMPRESS_GPU), 3);
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_PREREAD_FILE), 3);
        sPgSceneToPreRecongMap.put(315, 12);
        sPgSceneToPreRecongMap.put(318, 6);
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_UNMAP_FILE), 14);
        sPgSceneToPreRecongMap.put(Integer.valueOf(MemoryConstant.MSG_PROCRECLAIM_ALL_SUSPEND), 15);
        sPgSceneToPreRecongMap.put(322, 10);
        sPgSceneToPreRecongMap.put(323, 4);
        sPgSceneToPreRecongMap.put(324, 17);
        sPgSceneToPreRecongMap.put(325, 2);
    }

    public LCDSchedFeatureRT(Context context, String name) {
        super(context);
        AwareLog.d(TAG, "create " + name + "LCDSchedFeatureRT success.");
    }

    public boolean handleUpdateCustConfig() {
        return true;
    }

    public boolean handlerNaviStatus(boolean isInNavi) {
        return true;
    }

    public int getAppType(String pkgName) {
        if (pkgName == null) {
            return 255;
        }
        int appType = AppTypeRecoManager.getInstance().getAppType(pkgName);
        if (appType <= -1) {
            return 255;
        }
        if (appType <= 255) {
            return appType;
        }
        Integer convertType = sPgSceneToPreRecongMap.get(Integer.valueOf(appType));
        if (convertType == null) {
            return appType;
        }
        return convertType.intValue();
    }
}
