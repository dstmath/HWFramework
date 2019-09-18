package com.android.server.rms.iaware.dev;

import android.content.Context;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;

public abstract class DevSchedFeatureBase {
    private static final String TAG = "DevSchedFeatureBase";
    static ScreenState mScreenState = ScreenState.ScreenOn;
    public int mDeviceId = -1;

    public enum ScreenState {
        ScreenOn,
        ScreenOff
    }

    public abstract boolean handleUpdateCustConfig();

    public abstract boolean handlerNaviStatus(boolean z);

    public DevSchedFeatureBase(Context context) {
    }

    public static void setScreenState(ScreenState state) {
        mScreenState = state;
    }

    public boolean handScreenStateChange(ScreenState state) {
        return false;
    }

    public boolean handleResAppData(long timestamp, int event, AttrSegments attrSegments) {
        return false;
    }

    public void doDumpsys(String[] args) {
    }

    public int getDeviceId() {
        return this.mDeviceId;
    }

    public void sendCurrentDeviceMode() {
    }
}
