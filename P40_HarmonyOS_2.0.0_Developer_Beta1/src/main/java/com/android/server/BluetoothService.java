package com.android.server;

import android.content.Context;
import com.android.internal.os.RoSystemProperties;

/* access modifiers changed from: package-private */
public class BluetoothService extends SystemService {
    private BluetoothManagerService mBluetoothManagerService;
    private boolean mInitialized = false;

    public BluetoothService(Context context) {
        super(context);
        this.mBluetoothManagerService = new BluetoothManagerService(context);
    }

    private void initialize() {
        if (!this.mInitialized) {
            this.mBluetoothManagerService.handleOnBootPhase();
            this.mInitialized = true;
        }
    }

    @Override // com.android.server.SystemService
    public void onStart() {
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.BluetoothService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v3, types: [android.os.IBinder, com.android.server.BluetoothManagerService] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            publishBinderService("bluetooth_manager", this.mBluetoothManagerService);
        } else if (phase == 550 && !RoSystemProperties.MULTIUSER_HEADLESS_SYSTEM_USER) {
            initialize();
        }
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userHandle) {
        initialize();
        this.mBluetoothManagerService.handleOnSwitchUser(userHandle);
    }

    @Override // com.android.server.SystemService
    public void onUnlockUser(int userHandle) {
        this.mBluetoothManagerService.handleOnUnlockUser(userHandle);
    }
}
