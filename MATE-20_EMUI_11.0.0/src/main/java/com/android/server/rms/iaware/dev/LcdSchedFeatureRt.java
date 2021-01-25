package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.Map;

public class LcdSchedFeatureRt extends DevSchedFeatureBase {
    private static final Map<Integer, Integer> PG_SCENE_TO_PRE_RECONG_MAP = new ArrayMap();
    private static final String TAG = "LCDSchedFeatureRT";

    static {
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_DIRECT_SWAPPINESS), 1);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_PROTECTLRU_SET_FILENODE), 19);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_PROTECTLRU_SET_PROTECTZONE), 9);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_PROTECTLRU_SWITCH), 18);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_PROTECTLRU_SET_PROTECTRATIO), 6);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE), 8);
        PG_SCENE_TO_PRE_RECONG_MAP.put(310, 5);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_SET_PREREAD_PATH), 0);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_PREREAD_DATA_REMOVE), 7);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_COMPRESS_GPU), 3);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_PREREAD_FILE), 3);
        PG_SCENE_TO_PRE_RECONG_MAP.put(315, 12);
        PG_SCENE_TO_PRE_RECONG_MAP.put(318, 6);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_UNMAP_FILE), 14);
        PG_SCENE_TO_PRE_RECONG_MAP.put(Integer.valueOf((int) MemoryConstant.MSG_PROCRECLAIM_ALL_SUSPEND), 15);
        PG_SCENE_TO_PRE_RECONG_MAP.put(322, 10);
        PG_SCENE_TO_PRE_RECONG_MAP.put(323, 4);
        PG_SCENE_TO_PRE_RECONG_MAP.put(324, 17);
        PG_SCENE_TO_PRE_RECONG_MAP.put(325, 2);
    }

    public LcdSchedFeatureRt(Context context, String name) {
        super(context);
        AwareLog.d(TAG, "create " + name + "LcdSchedFeatureRt success.");
    }

    public int getAppType(String pkgName) {
        int appType;
        Integer convertType;
        if (pkgName == null || (appType = AppTypeRecoManager.getInstance().getAppType(pkgName)) <= -1) {
            return 255;
        }
        if (appType > 255 && (convertType = PG_SCENE_TO_PRE_RECONG_MAP.get(Integer.valueOf(appType))) != null) {
            return convertType.intValue();
        }
        return appType;
    }
}
