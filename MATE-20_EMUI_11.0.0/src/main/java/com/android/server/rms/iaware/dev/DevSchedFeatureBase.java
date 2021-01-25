package com.android.server.rms.iaware.dev;

import android.content.Context;
import com.android.server.rms.iaware.dev.FeatureXmlConfigParserRt;

public abstract class DevSchedFeatureBase {
    private static final String TAG = "DevSchedFeatureBase";
    static ScreenState sScreenState = ScreenState.ScreenOn;
    protected int deviceId = -1;

    public enum ScreenState {
        ScreenOn,
        ScreenOff
    }

    public DevSchedFeatureBase(Context context) {
    }

    public static void setScreenState(ScreenState state) {
        sScreenState = state;
    }

    public boolean handleScreenStateChange(ScreenState state) {
        return false;
    }

    public void handleTopAppChange(int pid, int uid, String pkg) {
    }

    public void doDumpsys(String[] args) {
    }

    public boolean handleNaviStatus(boolean isInNavi) {
        return true;
    }

    public int getDeviceId() {
        return this.deviceId;
    }

    public void sendCurrentDeviceMode() {
    }

    public void readFeatureConfig(FeatureXmlConfigParserRt.FeatureXmlConfig config) {
    }
}
