package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;
import java.util.Map;

public class OnDemandBoost {
    private static final String COLD_START = "cold_start_duration_max";
    private static final String COLD_START_OFF_DELAY = "cold_start_off_delay";
    private static final int INVALID_TIME = -1;
    private static final int MAX_DURATION_TIME = 60000;
    private static final int MAX_OFF_DELAY_TIME = 10000;
    private static final String TAG = "OnDemandBoost";
    private static final String WINDOW_SWITCH = "window_switch_duration_max";
    private static final String WIN_SWITCH_OFF_DELAY = "window_switch_off_delay";
    private static OnDemandBoost sInstance;
    private static Object sLock = new Object();
    private int mColdStartDuration = 1500;
    private int mColdStartOffDelay = 200;
    private int mWinSwitchoffDelay = 200;
    private int mWindowSwitchDuration = 400;

    private OnDemandBoost() {
    }

    public static OnDemandBoost getInstance() {
        OnDemandBoost onDemandBoost;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new OnDemandBoost();
            }
            onDemandBoost = sInstance;
        }
        return onDemandBoost;
    }

    public void setParams(Map<String, Integer> onDemandParaMap) {
        if (onDemandParaMap != null) {
            int coldStartTime = getDurationTime(COLD_START, onDemandParaMap);
            if (coldStartTime != -1) {
                this.mColdStartDuration = coldStartTime;
            }
            int windowSwitchTime = getDurationTime(WINDOW_SWITCH, onDemandParaMap);
            if (windowSwitchTime != -1) {
                this.mWindowSwitchDuration = windowSwitchTime;
            }
            int coldStartOffDelayTime = getOffDelayTime(COLD_START_OFF_DELAY, onDemandParaMap);
            if (coldStartOffDelayTime != -1) {
                this.mColdStartOffDelay = coldStartOffDelayTime;
            }
            int windowSwitchOffDelayTime = getOffDelayTime(WIN_SWITCH_OFF_DELAY, onDemandParaMap);
            if (windowSwitchOffDelayTime != -1) {
                this.mWinSwitchoffDelay = windowSwitchOffDelayTime;
            }
            AwareLog.d(TAG, "setParms:" + this.mColdStartDuration + " " + this.mWindowSwitchDuration + " " + this.mColdStartOffDelay + " " + this.mWinSwitchoffDelay);
        }
    }

    private int getDurationTime(String durationType, Map<String, Integer> onDemandParaMap) {
        Integer time = onDemandParaMap.get(durationType);
        if (time != null) {
            int durationTime = time.intValue();
            if (durationTime > 0 && durationTime < 60000) {
                return durationTime;
            }
        }
        return -1;
    }

    private int getOffDelayTime(String delayType, Map<String, Integer> onDemandParaMap) {
        Integer time = onDemandParaMap.get(delayType);
        if (time != null) {
            int durationTime = time.intValue();
            if (durationTime >= 0 && durationTime < 10000) {
                return durationTime;
            }
        }
        return -1;
    }

    public int getColdStartDuration() {
        return this.mColdStartDuration;
    }

    public int getWindowSwitchDuration() {
        return this.mWindowSwitchDuration;
    }

    public int getColdStartOffDelay() {
        return this.mColdStartOffDelay;
    }

    public int getWinSwitchOffDelay() {
        return this.mWinSwitchoffDelay;
    }
}
