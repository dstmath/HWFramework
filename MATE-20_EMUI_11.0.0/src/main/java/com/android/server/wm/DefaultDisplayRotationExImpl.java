package com.android.server.wm;

import android.os.Message;
import com.huawei.server.wm.IHwDisplayRotationEx;

public class DefaultDisplayRotationExImpl implements IHwDisplayRotationEx {
    public DefaultDisplayRotationExImpl(WindowManagerServiceEx service, DisplayContentEx displayContent, boolean isDefaultDisplay) {
    }

    public int getSwingRotation(int lastRotation, int sensorRotation) {
        return 0;
    }

    public void setSwingRotation(int rotation) {
    }

    public void setRotationType(int type) {
    }

    public int getRotationType() {
        return 0;
    }

    public void reportRotation(int rotationType, int oldRotation, int newRotation, String packageName) {
    }

    public void setRotation(int rotation) {
    }

    public boolean isIntelliServiceEnabled(int orientatin) {
        return false;
    }

    public int getRotationFromSensorOrFace(int sensorRotation) {
        return 0;
    }

    public void setSensorRotation(int rotation) {
    }

    public void startIntelliService(int orientation) {
    }

    public void startIntelliService() {
    }

    public void handleReportLog(Message msg) {
    }
}
