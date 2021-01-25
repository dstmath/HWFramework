package com.huawei.hardware;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.hardware.ICameraService;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CameraServiceEx {
    private static final String TAG = "CameraServiceAdapter";
    private static final int WIRELESS_OFF = 6;
    private static final int WIRELESS_ON = 7;
    private ICameraService cameraService = null;

    public CameraServiceEx(IBinder cameraServiceBinder) {
        try {
            this.cameraService = ICameraService.Stub.asInterface(cameraServiceBinder);
        } catch (ServiceSpecificException e) {
            this.cameraService = null;
            Log.e(TAG, "ServiceSpecificException while set cameraService.");
        } catch (Exception e2) {
            this.cameraService = null;
            Log.e(TAG, "Exception while set cameraService.");
        }
    }

    public void setCommand(String commandType, String commandValue) {
        ICameraService iCameraService = this.cameraService;
        if (iCameraService == null) {
            Log.e(TAG, "cameraService is null.");
            return;
        }
        try {
            iCameraService.setCommand(commandType, commandValue);
        } catch (ServiceSpecificException e) {
            this.cameraService = null;
            Log.e(TAG, "ServiceSpecificException while setCommand.");
        } catch (RemoteException e2) {
            this.cameraService = null;
            Log.e(TAG, "RemoteException while setCommand.");
        } catch (Exception e3) {
            this.cameraService = null;
            Log.e(TAG, "Exception while setCommand.");
        }
    }

    @UnsupportedAppUsage
    public boolean startWirelessCharging(Context context) {
        if (context == null) {
            Log.w(TAG, "startWirelessCharging failed, cause context is null!");
            return false;
        }
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(BatteryManager.class);
        if (batteryManager == null) {
            Log.w(TAG, "startWirelessCharging failed, cause batteryManager is not valid!");
            return false;
        } else if (!batteryManager.supportWirelessTxCharge()) {
            Log.w(TAG, "startWirelessCharging failed, cause not support wireless charging!");
            return false;
        } else if (batteryManager.getWirelessTxSwitch() % 2 == 0) {
            int res = batteryManager.alterWirelessTxSwitch(7);
            if (res < 0) {
                Log.w(TAG, "startWirelessCharging failed, batteryManager returned result not success!");
            }
            if (res >= 0) {
                return true;
            }
            return false;
        } else {
            Log.i(TAG, "no need startWirelessCharging, cause wireless charging is already on!");
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean stopWirelessCharging(Context context) {
        if (context == null) {
            Log.w(TAG, "stopWirelessCharging failed, cause context is null!");
            return false;
        }
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(BatteryManager.class);
        if (batteryManager == null) {
            Log.w(TAG, "stopWirelessCharging failed, cause batteryManager is not valid!");
            return false;
        } else if (!batteryManager.supportWirelessTxCharge()) {
            Log.w(TAG, "stopWirelessCharging failed, cause not support wireless charging!");
            return false;
        } else if (batteryManager.getWirelessTxSwitch() % 2 == 1) {
            int res = batteryManager.alterWirelessTxSwitch(6);
            if (res < 0) {
                Log.w(TAG, "stopWirelessCharging failed, batteryManager returned result not success!");
            }
            if (res >= 0) {
                return true;
            }
            return false;
        } else {
            Log.i(TAG, "no need stopWirelessCharging, cause wireless charging is already off!");
            return false;
        }
    }
}
