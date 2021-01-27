package com.android.server;

import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManagerCallback;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.hsm.permission.StubController;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwBluetoothManagerService extends BluetoothManagerService {
    private static final String TAG = "HwBluetoothManagerService";
    private static BluetoothParaManager mBluetoothParaManager = null;

    public /* bridge */ /* synthetic */ boolean bindBluetoothProfileService(int x0, IBluetoothProfileServiceConnection x1) {
        return HwBluetoothManagerService.super.bindBluetoothProfileService(x0, x1);
    }

    public /* bridge */ /* synthetic */ void changeBluetoothStateFromAirplaneModeInner() {
        HwBluetoothManagerService.super.changeBluetoothStateFromAirplaneModeInner();
    }

    public /* bridge */ /* synthetic */ boolean disable(String x0, boolean x1) throws RemoteException {
        return HwBluetoothManagerService.super.disable(x0, x1);
    }

    public /* bridge */ /* synthetic */ void dump(FileDescriptor x0, PrintWriter x1, String[] x2) {
        HwBluetoothManagerService.super.dump(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ boolean enable(String x0) throws RemoteException {
        return HwBluetoothManagerService.super.enable(x0);
    }

    public /* bridge */ /* synthetic */ boolean enableNoAutoConnect(String x0) {
        return HwBluetoothManagerService.super.enableNoAutoConnect(x0);
    }

    public /* bridge */ /* synthetic */ IBluetooth getAdapter() {
        return HwBluetoothManagerService.super.getAdapter();
    }

    public /* bridge */ /* synthetic */ String getAddress() {
        return HwBluetoothManagerService.super.getAddress();
    }

    public /* bridge */ /* synthetic */ IBluetoothGatt getBluetoothGatt() {
        return HwBluetoothManagerService.super.getBluetoothGatt();
    }

    public /* bridge */ /* synthetic */ int getMessageDisableValueInner() {
        return HwBluetoothManagerService.super.getMessageDisableValueInner();
    }

    public /* bridge */ /* synthetic */ int getMessageEnableValueInner() {
        return HwBluetoothManagerService.super.getMessageEnableValueInner();
    }

    public /* bridge */ /* synthetic */ String getName() {
        return HwBluetoothManagerService.super.getName();
    }

    public /* bridge */ /* synthetic */ int getState() {
        return HwBluetoothManagerService.super.getState();
    }

    public /* bridge */ /* synthetic */ void handleOnBootPhase() {
        HwBluetoothManagerService.super.handleOnBootPhase();
    }

    public /* bridge */ /* synthetic */ void handleOnSwitchUser(int x0) {
        HwBluetoothManagerService.super.handleOnSwitchUser(x0);
    }

    public /* bridge */ /* synthetic */ void handleOnUnlockUser(int x0) {
        HwBluetoothManagerService.super.handleOnUnlockUser(x0);
    }

    public /* bridge */ /* synthetic */ boolean isBleAppPresent() {
        return HwBluetoothManagerService.super.isBleAppPresent();
    }

    public /* bridge */ /* synthetic */ boolean isBleScanAlwaysAvailable() {
        return HwBluetoothManagerService.super.isBleScanAlwaysAvailable();
    }

    public /* bridge */ /* synthetic */ boolean isEnabled() {
        return HwBluetoothManagerService.super.isEnabled();
    }

    public /* bridge */ /* synthetic */ boolean isHearingAidProfileSupported() {
        return HwBluetoothManagerService.super.isHearingAidProfileSupported();
    }

    public /* bridge */ /* synthetic */ IBluetooth registerAdapter(IBluetoothManagerCallback x0) {
        return HwBluetoothManagerService.super.registerAdapter(x0);
    }

    public /* bridge */ /* synthetic */ void registerStateChangeCallback(IBluetoothStateChangeCallback x0) {
        HwBluetoothManagerService.super.registerStateChangeCallback(x0);
    }

    public /* bridge */ /* synthetic */ void unbindAndFinish() {
        HwBluetoothManagerService.super.unbindAndFinish();
    }

    public /* bridge */ /* synthetic */ void unbindBluetoothProfileService(int x0, IBluetoothProfileServiceConnection x1) {
        HwBluetoothManagerService.super.unbindBluetoothProfileService(x0, x1);
    }

    public /* bridge */ /* synthetic */ void unregisterAdapter(IBluetoothManagerCallback x0) {
        HwBluetoothManagerService.super.unregisterAdapter(x0);
    }

    public /* bridge */ /* synthetic */ void unregisterStateChangeCallback(IBluetoothStateChangeCallback x0) {
        HwBluetoothManagerService.super.unregisterStateChangeCallback(x0);
    }

    public /* bridge */ /* synthetic */ int updateBleAppCount(IBinder x0, boolean x1, String x2) {
        return HwBluetoothManagerService.super.updateBleAppCount(x0, x1, x2);
    }

    public HwBluetoothManagerService(Context context) {
        super(context);
        if (mBluetoothParaManager == null) {
            mBluetoothParaManager = new BluetoothParaManager(context);
        }
    }

    public boolean checkPrecondition(int uid) {
        return StubController.checkPrecondition(uid);
    }
}
