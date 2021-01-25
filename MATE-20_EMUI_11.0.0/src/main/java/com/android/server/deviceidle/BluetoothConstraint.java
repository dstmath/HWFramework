package com.android.server.deviceidle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.DeviceIdleController;

public class BluetoothConstraint implements IDeviceIdleConstraint {
    private static final long INACTIVITY_TIMEOUT_MS = 1200000;
    private static final String TAG = BluetoothConstraint.class.getSimpleName();
    private final BluetoothManager mBluetoothManager;
    private volatile boolean mConnected = true;
    private final Context mContext;
    private final Handler mHandler;
    private final DeviceIdleController.LocalService mLocalService;
    private volatile boolean mMonitoring = false;
    @VisibleForTesting
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.deviceidle.BluetoothConstraint.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.device.action.ACL_CONNECTED".equals(intent.getAction())) {
                BluetoothConstraint.this.mLocalService.exitIdle("bluetooth");
            } else {
                BluetoothConstraint.this.updateAndReportActiveLocked();
            }
        }
    };
    private final Runnable mTimeoutCallback = new Runnable() {
        /* class com.android.server.deviceidle.$$Lambda$BluetoothConstraint$ix_cz35TWbQB96MNXR8MW8BQtww */

        @Override // java.lang.Runnable
        public final void run() {
            BluetoothConstraint.this.lambda$new$0$BluetoothConstraint();
        }
    };

    public BluetoothConstraint(Context context, Handler handler, DeviceIdleController.LocalService localService) {
        this.mContext = context;
        this.mHandler = handler;
        this.mLocalService = localService;
        this.mBluetoothManager = (BluetoothManager) this.mContext.getSystemService(BluetoothManager.class);
    }

    @Override // com.android.server.deviceidle.IDeviceIdleConstraint
    public synchronized void startMonitoring() {
        this.mConnected = true;
        this.mMonitoring = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        filter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, this.mTimeoutCallback), INACTIVITY_TIMEOUT_MS);
        updateAndReportActiveLocked();
    }

    @Override // com.android.server.deviceidle.IDeviceIdleConstraint
    public synchronized void stopMonitoring() {
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mHandler.removeCallbacks(this.mTimeoutCallback);
        this.mMonitoring = false;
    }

    /* access modifiers changed from: private */
    /* renamed from: cancelMonitoringDueToTimeout */
    public synchronized void lambda$new$0$BluetoothConstraint() {
        if (this.mMonitoring) {
            this.mMonitoring = false;
            this.mLocalService.onConstraintStateChanged(this, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"this"})
    private void updateAndReportActiveLocked() {
        boolean connected = isBluetoothConnected(this.mBluetoothManager);
        if (connected != this.mConnected) {
            this.mConnected = connected;
            this.mLocalService.onConstraintStateChanged(this, this.mConnected);
        }
    }

    @VisibleForTesting
    static boolean isBluetoothConnected(BluetoothManager bluetoothManager) {
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        if (adapter == null || !adapter.isEnabled() || bluetoothManager.getConnectedDevices(7).size() <= 0) {
            return false;
        }
        return true;
    }
}
