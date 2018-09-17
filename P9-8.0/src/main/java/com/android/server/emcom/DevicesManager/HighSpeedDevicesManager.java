package com.android.server.emcom.DevicesManager;

import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.telephony.HwTelephonyManager;
import android.util.Log;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.huawei.android.util.NoExtAPIException;

public class HighSpeedDevicesManager {
    private static final String CONNECTED_DEVICE_STATE_IN = "in";
    private static final String CONNECTED_DEVICE_STATE_OFF = "off";
    private static final String CONNECTED_DEVICE_STATE_ON = "on";
    private static final String CONNECTED_DEVICE_STATE_OUT = "out";
    private static final String CONNECTED_DEVICE_TYPE_DP = "DP";
    private static final String CONNECTED_DEVICE_TYPE_LCD = "LCD";
    private static final String CONNECTED_DEVICE_TYPE_USB3 = "USB3";
    private static final String TAG = "HighSpeedDevicesManager";
    private static volatile HighSpeedDevicesManager sInstance;
    private boolean mIsDPConnected = false;
    private boolean mIsUSB3Connected = false;
    private final HighSpeedDevicesObserver mObserver = new HighSpeedDevicesObserver();

    class HighSpeedDevicesObserver extends UEventObserver {
        HighSpeedDevicesObserver() {
        }

        public void onUEvent(UEvent event) {
            Log.d(HighSpeedDevicesManager.TAG, "HighSpeedDevicesObserver onUEvent.");
            String DP_STATE = event.get("DP_STATE");
            String USB3_STATE = event.get("USB3_STATE");
            Log.d(HighSpeedDevicesManager.TAG, "DP_STATE: " + DP_STATE + "; USB3_STATE: " + USB3_STATE + ".");
            if (AwareJobSchedulerConstants.BAR_STATUS_ON.equals(DP_STATE)) {
                HighSpeedDevicesManager.this.onDPConnectedChanged(true);
            } else if (AwareJobSchedulerConstants.BAR_STATUS_OFF.equals(DP_STATE)) {
                HighSpeedDevicesManager.this.onDPConnectedChanged(false);
            }
            if (AwareJobSchedulerConstants.BAR_STATUS_ON.equals(USB3_STATE)) {
                HighSpeedDevicesManager.this.onUSB3ConectedChanged(true);
            } else if (AwareJobSchedulerConstants.BAR_STATUS_OFF.equals(USB3_STATE)) {
                HighSpeedDevicesManager.this.onUSB3ConectedChanged(false);
            }
        }
    }

    public static HighSpeedDevicesManager getInstance() {
        if (sInstance == null) {
            synchronized (HighSpeedDevicesManager.class) {
                if (sInstance == null) {
                    sInstance = new HighSpeedDevicesManager();
                }
            }
        }
        return sInstance;
    }

    public void startObserving() {
        this.mObserver.startObserving("DEVPATH=/devices/virtual/hw_typec/typec");
        Log.d(TAG, "HighSpeedDevicesManager startObserving.");
    }

    private void onDPConnectedChanged(boolean isDPConnected) {
        Log.d(TAG, "mIsDPConnected, mIsDPConnected: " + this.mIsDPConnected + " -> " + isDPConnected);
        if (this.mIsDPConnected != isDPConnected) {
            this.mIsDPConnected = isDPConnected;
            reportDPConnected();
        }
    }

    private void onUSB3ConectedChanged(boolean isUSB3Connected) {
        Log.d(TAG, "mIsUSB3Connected, mIsUSB3Connected: " + this.mIsUSB3Connected + " -> " + isUSB3Connected);
        if (this.mIsUSB3Connected != isUSB3Connected) {
            this.mIsUSB3Connected = isUSB3Connected;
            reportUSB3Conected();
        }
    }

    public boolean mIsDPConnected() {
        return this.mIsDPConnected;
    }

    public boolean mIsUSB3Connected() {
        return this.mIsUSB3Connected;
    }

    private void reportDPConnected() {
        try {
            if (mIsDPConnected()) {
                HwTelephonyManager.getDefault().notifyDeviceState(CONNECTED_DEVICE_TYPE_DP, CONNECTED_DEVICE_STATE_IN, "");
                Log.d(TAG, "DPManager notifyDeviceState CONNECTED_DEVICE_STATE_IN.");
                return;
            }
            HwTelephonyManager.getDefault().notifyDeviceState(CONNECTED_DEVICE_TYPE_DP, CONNECTED_DEVICE_STATE_OUT, "");
            Log.d(TAG, "DPManager notifyDeviceState CONNECTED_DEVICE_STATE_OUT.");
        } catch (NoExtAPIException e) {
            Log.e(TAG, "notifyDeviceState - NoExtAPIException! " + e.toString());
        }
    }

    private void reportUSB3Conected() {
        try {
            if (mIsUSB3Connected()) {
                HwTelephonyManager.getDefault().notifyDeviceState(CONNECTED_DEVICE_TYPE_USB3, CONNECTED_DEVICE_STATE_IN, "");
                Log.d(TAG, "USB3Manager notifyDeviceState CONNECTED_DEVICE_STATE_IN.");
                return;
            }
            HwTelephonyManager.getDefault().notifyDeviceState(CONNECTED_DEVICE_TYPE_USB3, CONNECTED_DEVICE_STATE_OUT, "");
            Log.d(TAG, "USB3Manager notifyDeviceState CONNECTED_DEVICE_STATE_OUT.");
        } catch (NoExtAPIException e) {
            Log.e(TAG, "notifyDeviceState - NoExtAPIException! " + e.toString());
        }
    }

    public void reportScreenState(boolean state) {
        if (state) {
            try {
                HwTelephonyManager.getDefault().notifyDeviceState(CONNECTED_DEVICE_TYPE_LCD, "on", "");
                Log.d(TAG, "reportScreenState CONNECTED_DEVICE_STATE_ON.");
                return;
            } catch (NoExtAPIException e) {
                Log.e(TAG, "notifyDeviceState - NoExtAPIException! " + e.toString());
                return;
            }
        }
        HwTelephonyManager.getDefault().notifyDeviceState(CONNECTED_DEVICE_TYPE_LCD, CONNECTED_DEVICE_STATE_OFF, "");
        Log.d(TAG, "reportScreenState CONNECTED_DEVICE_STATE_OFF.");
    }
}
