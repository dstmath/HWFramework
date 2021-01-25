package com.android.server;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.hidl.HealthInfoAdapter;

public class BatteryServiceEx {
    private static final String TAG = "BatteryServiceEx";
    private BatteryServiceBridge mBridge = null;

    public BatteryServiceEx(Context context) {
    }

    /* access modifiers changed from: protected */
    public void setBatteryService(BatteryServiceBridge bridge) {
        this.mBridge = bridge;
    }

    public BatteryService getBatteryService() {
        return this.mBridge;
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
    }

    /* access modifiers changed from: protected */
    public void updateLight(boolean isEnable, int ledOnMS, int ledOffMS) {
    }

    /* access modifiers changed from: protected */
    public void updateLight() {
    }

    /* access modifiers changed from: protected */
    public void newUpdateLightsLocked() {
    }

    /* access modifiers changed from: protected */
    public void playRing() {
    }

    /* access modifiers changed from: protected */
    public void stopRing() {
    }

    /* access modifiers changed from: protected */
    public void printBatteryLog(HealthInfoAdapter oldInfo, HealthInfoAdapter newInfo, int oldPlugType, boolean isUpdatesStopped) {
    }

    /* access modifiers changed from: protected */
    public int alterWirelessTxSwitchInternal(int status) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getWirelessTxSwitchInternal() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean supportWirelessTxChargeInternal() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void registerHealthCallback() {
        BatteryServiceBridge batteryServiceBridge = this.mBridge;
        if (batteryServiceBridge != null) {
            batteryServiceBridge.registerHealthCallback();
        }
        Log.e(TAG, "registerHealthCallback with BatteryService is null ");
    }

    /* access modifiers changed from: protected */
    public boolean isWirelessCharge() {
        BatteryServiceBridge batteryServiceBridge = this.mBridge;
        if (batteryServiceBridge != null) {
            return batteryServiceBridge.isWirelessCharge();
        }
        Log.e(TAG, "isWirelessCharge with BatteryService is null ");
        return false;
    }

    /* access modifiers changed from: protected */
    public int getLowBatteryWarningLevel() {
        BatteryServiceBridge batteryServiceBridge = this.mBridge;
        if (batteryServiceBridge != null) {
            return batteryServiceBridge.getLowBatteryWarningLevel();
        }
        Log.e(TAG, "getLowBatteryWarningLevel with BatteryService is null ");
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getPlugType() {
        BatteryServiceBridge batteryServiceBridge = this.mBridge;
        if (batteryServiceBridge != null) {
            return batteryServiceBridge.mPlugType;
        }
        Log.e(TAG, "getPlugType with BatteryService is null ");
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getOldPlugInType() {
        BatteryServiceBridge batteryServiceBridge = this.mBridge;
        if (batteryServiceBridge != null) {
            return batteryServiceBridge.mOldPlugInType;
        }
        Log.e(TAG, "getOldPlugInType with BatteryService is null ");
        return 0;
    }

    /* access modifiers changed from: protected */
    public void setOldPlugInType(int plugInType) {
        BatteryServiceBridge batteryServiceBridge = this.mBridge;
        if (batteryServiceBridge != null) {
            batteryServiceBridge.mOldPlugInType = plugInType;
        }
        Log.e(TAG, "setOldPlugInType with BatteryService is null ");
    }

    /* access modifiers changed from: protected */
    public int getHealthInfoBatteryLevel() {
        BatteryServiceBridge batteryServiceBridge = this.mBridge;
        if (batteryServiceBridge != null) {
            return batteryServiceBridge.getHealthInfo().batteryLevel;
        }
        Log.e(TAG, "getHealthInfoBatteryLevel with BatteryService is null ");
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getHealthInfoBatteryStatus() {
        BatteryServiceBridge batteryServiceBridge = this.mBridge;
        if (batteryServiceBridge != null) {
            return batteryServiceBridge.getHealthInfo().batteryStatus;
        }
        Log.e(TAG, "getHealthInfoBatteryStatus with BatteryService is null ");
        return 0;
    }

    /* access modifiers changed from: protected */
    public void handleNonStandardChargeLine(int plugInType) {
    }

    /* access modifiers changed from: protected */
    public void cancelChargeLineNotification(int titleId) {
    }

    /* access modifiers changed from: protected */
    public void cameraUpdateLight(boolean isEnable) {
    }

    /* access modifiers changed from: protected */
    public void startAutoPowerOff() {
    }

    /* access modifiers changed from: protected */
    public void stopAutoPowerOff() {
    }

    /* access modifiers changed from: protected */
    public void setHwChargeTimeRemaining(int time) {
        try {
            if (this.mBridge != null && this.mBridge.getBatteryStats() != null) {
                this.mBridge.getBatteryStats().setHwChargeTimeRemaining((long) time);
            }
        } catch (RemoteException e) {
            Log.e("HwBatteryService", "setHwChargeTimeRemaining, Remote Exception.");
        }
    }
}
