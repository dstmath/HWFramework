package com.android.server;

import android.content.Context;
import android.hardware.health.V1_0.HealthInfo;
import com.huawei.android.hidl.HealthInfoAdapter;
import com.huawei.server.HwBasicPlatformFactory;

public class BatteryServiceBridge extends BatteryService {
    private BatteryServiceEx mBatteryServiceEx;

    public BatteryServiceBridge(Context context) {
        super(context);
        initBatteryService(context);
    }

    private void initBatteryService(Context context) {
        this.mBatteryServiceEx = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwBatteryService(context);
        this.mBatteryServiceEx.setBatteryService(this);
    }

    public void onStart() {
        BatteryServiceBridge.super.onStart();
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.onStart();
        }
    }

    public void onBootPhase(int phase) {
        BatteryServiceBridge.super.onBootPhase(phase);
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.onBootPhase(phase);
        }
    }

    /* access modifiers changed from: protected */
    public void updateLight(boolean isEnable, int ledOnMS, int ledOffMS) {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.updateLight(isEnable, ledOnMS, ledOffMS);
        }
    }

    /* access modifiers changed from: protected */
    public void updateLight() {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.updateLight();
        }
    }

    /* access modifiers changed from: protected */
    public void newUpdateLightsLocked() {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.newUpdateLightsLocked();
        }
    }

    /* access modifiers changed from: protected */
    public void playRing() {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.playRing();
        }
    }

    /* access modifiers changed from: protected */
    public void stopRing() {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.stopRing();
        }
    }

    /* access modifiers changed from: protected */
    public void printBatteryLog(HealthInfo oldInfo, HealthInfo newInfo, int oldPlugType, boolean isUpdatesStopped) {
        HealthInfoAdapter oldInfoAdapter = null;
        HealthInfoAdapter newInfoAdapter = null;
        if (!(oldInfo == null || newInfo == null)) {
            oldInfoAdapter = new HealthInfoAdapter();
            oldInfoAdapter.setHealthInfo(oldInfo);
            newInfoAdapter = new HealthInfoAdapter();
            newInfoAdapter.setHealthInfo(newInfo);
        }
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.printBatteryLog(oldInfoAdapter, newInfoAdapter, oldPlugType, isUpdatesStopped);
        }
    }

    /* access modifiers changed from: protected */
    public int alterWirelessTxSwitchInternal(int status) {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            return batteryServiceEx.alterWirelessTxSwitchInternal(status);
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getWirelessTxSwitchInternal() {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            return batteryServiceEx.getWirelessTxSwitchInternal();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean supportWirelessTxChargeInternal() {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            return batteryServiceEx.supportWirelessTxChargeInternal();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleNonStandardChargeLine(int plugInType) {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.handleNonStandardChargeLine(plugInType);
        }
    }

    /* access modifiers changed from: protected */
    public void cancelChargeLineNotification(int titleId) {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.cancelChargeLineNotification(titleId);
        }
    }

    /* access modifiers changed from: protected */
    public void cameraUpdateLight(boolean isEnable) {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.cameraUpdateLight(isEnable);
        }
    }

    public void startAutoPowerOff() {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.startAutoPowerOff();
        }
    }

    public void stopAutoPowerOff() {
        BatteryServiceEx batteryServiceEx = this.mBatteryServiceEx;
        if (batteryServiceEx != null) {
            batteryServiceEx.stopAutoPowerOff();
        }
    }
}
