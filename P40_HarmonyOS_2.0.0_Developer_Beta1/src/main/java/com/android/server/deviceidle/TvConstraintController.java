package com.android.server.deviceidle;

import android.content.Context;
import android.os.Handler;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;

public class TvConstraintController implements ConstraintController {
    private final BluetoothConstraint mBluetoothConstraint;
    private final Context mContext;
    private final DeviceIdleController.LocalService mDeviceIdleService = ((DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class));
    private final Handler mHandler;

    public TvConstraintController(Context context, Handler handler) {
        BluetoothConstraint bluetoothConstraint;
        this.mContext = context;
        this.mHandler = handler;
        if (context.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            bluetoothConstraint = new BluetoothConstraint(this.mContext, this.mHandler, this.mDeviceIdleService);
        } else {
            bluetoothConstraint = null;
        }
        this.mBluetoothConstraint = bluetoothConstraint;
    }

    @Override // com.android.server.deviceidle.ConstraintController
    public void start() {
        BluetoothConstraint bluetoothConstraint = this.mBluetoothConstraint;
        if (bluetoothConstraint != null) {
            this.mDeviceIdleService.registerDeviceIdleConstraint(bluetoothConstraint, "bluetooth", 1);
        }
    }

    @Override // com.android.server.deviceidle.ConstraintController
    public void stop() {
        BluetoothConstraint bluetoothConstraint = this.mBluetoothConstraint;
        if (bluetoothConstraint != null) {
            this.mDeviceIdleService.unregisterDeviceIdleConstraint(bluetoothConstraint);
        }
    }
}
