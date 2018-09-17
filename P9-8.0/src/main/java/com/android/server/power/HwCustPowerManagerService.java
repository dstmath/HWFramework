package com.android.server.power;

import android.content.Context;
import android.database.ContentObserver;
import android.os.BatteryManagerInternal;
import android.service.dreams.DreamManagerInternal;

public class HwCustPowerManagerService {
    static final String TAG = "HwCustPowerManagerService";

    public void init(Context context) {
    }

    public boolean isDelayEnanbled() {
        return false;
    }

    public void checkDelay(String tagName) {
    }

    public HwCustPowerManagerService(Context context) {
    }

    public void updateSettingsLocked() {
    }

    public boolean readConfigurationLocked(boolean config) {
        return config;
    }

    public boolean isStartDreamFromUser() {
        return false;
    }

    public void setStartDreamFromUser(boolean buser) {
    }

    public boolean isChargingAlbumSupported() {
        return false;
    }

    public boolean isChargingAlbumEnabled() {
        return false;
    }

    public void systemReady(BatteryManagerInternal batterymanager, DreamManagerInternal dreammanager, ContentObserver observer) {
    }

    public boolean canDreamLocked() {
        return false;
    }

    public void handleDreamLocked() {
    }

    public boolean startDream(boolean bfullscreen) {
        return false;
    }

    public boolean stopDream() {
        return false;
    }
}
