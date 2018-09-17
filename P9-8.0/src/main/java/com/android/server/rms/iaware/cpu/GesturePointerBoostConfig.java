package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class GesturePointerBoostConfig extends CPUCustBaseConfig {
    private static final String CONFIG_GESTURE_POINTER_BOOST = "gesture_pointer_boost";
    private static final String TAG = "GesturePointerBoostConfig";
    private Map<String, CPUPropInfoItem> mGestureInfoMap = new ArrayMap();
    private Map<String, String> mGestureItem2PropMap = new ArrayMap();

    public GesturePointerBoostConfig() {
        init();
    }

    public void setConfig(CPUFeature feature) {
    }

    private void init() {
        this.mGestureItem2PropMap.put("move_boost_dif", "moveboostdif");
        obtainConfigInfo(CONFIG_GESTURE_POINTER_BOOST, this.mGestureItem2PropMap, this.mGestureInfoMap);
    }

    public int getIntXmlValue(String item) {
        int value = -1;
        if (item == null) {
            return value;
        }
        CPUPropInfoItem infoItem = (CPUPropInfoItem) this.mGestureInfoMap.get(item);
        if (infoItem != null) {
            try {
                value = Integer.parseInt(infoItem.mValue);
            } catch (NumberFormatException e) {
                AwareLog.d(TAG, "parse value failed:" + infoItem.mValue);
            }
        }
        return value;
    }
}
