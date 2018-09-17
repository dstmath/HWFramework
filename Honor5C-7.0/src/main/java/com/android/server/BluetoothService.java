package com.android.server;

import android.content.Context;

class BluetoothService extends SystemService {
    private static final String TAG = "BluetoothService";
    private BluetoothManagerService mBluetoothManagerService;

    public BluetoothService(Context context) {
        super(context);
        this.mBluetoothManagerService = new BluetoothManagerService(context);
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            publishBinderService("bluetooth_manager", this.mBluetoothManagerService);
        } else if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
            HwLog.d(TAG, "onBootPhase: PHASE_SYSTEM_SERVICES_READY");
            this.mBluetoothManagerService.handleOnBootPhase();
        }
    }

    public void onSwitchUser(int userHandle) {
        HwLog.d(TAG, "onSwitchUser: switching to user " + userHandle);
        this.mBluetoothManagerService.handleOnSwitchUser(userHandle);
    }

    public void onUnlockUser(int userHandle) {
        this.mBluetoothManagerService.handleOnUnlockUser(userHandle);
    }
}
