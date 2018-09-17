package com.android.server;

import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManagerCallback;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.Context;
import android.os.IBinder;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwBluetoothManagerService extends BluetoothManagerService {
    private static final String TAG = "HwBluetoothManagerService";
    private static BluetoothParaManager mBluetoothParaManager = null;

    public /* bridge */ /* synthetic */ boolean bindBluetoothProfileService(int i, IBluetoothProfileServiceConnection iBluetoothProfileServiceConnection) {
        return super.bindBluetoothProfileService(i, iBluetoothProfileServiceConnection);
    }

    public /* bridge */ /* synthetic */ boolean disable(String str, boolean z) {
        return super.disable(str, z);
    }

    public /* bridge */ /* synthetic */ boolean disableRadio() {
        return super.disableRadio();
    }

    public /* bridge */ /* synthetic */ void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(fileDescriptor, printWriter, strArr);
    }

    public /* bridge */ /* synthetic */ boolean enable(String str) {
        return super.enable(str);
    }

    public /* bridge */ /* synthetic */ boolean enableNoAutoConnect(String str) {
        return super.enableNoAutoConnect(str);
    }

    public /* bridge */ /* synthetic */ boolean enableRadio() {
        return super.enableRadio();
    }

    public /* bridge */ /* synthetic */ String getAddress() {
        return super.getAddress();
    }

    public /* bridge */ /* synthetic */ IBluetoothGatt getBluetoothGatt() {
        return super.getBluetoothGatt();
    }

    public /* bridge */ /* synthetic */ String getName() {
        return super.getName();
    }

    public /* bridge */ /* synthetic */ void getNameAndAddress() {
        super.getNameAndAddress();
    }

    public /* bridge */ /* synthetic */ int getState() {
        return super.getState();
    }

    public /* bridge */ /* synthetic */ void handleOnBootPhase() {
        super.handleOnBootPhase();
    }

    public /* bridge */ /* synthetic */ void handleOnSwitchUser(int i) {
        super.handleOnSwitchUser(i);
    }

    public /* bridge */ /* synthetic */ void handleOnUnlockUser(int i) {
        super.handleOnUnlockUser(i);
    }

    public /* bridge */ /* synthetic */ boolean isBleAppPresent() {
        return super.isBleAppPresent();
    }

    public /* bridge */ /* synthetic */ boolean isBleScanAlwaysAvailable() {
        return super.isBleScanAlwaysAvailable();
    }

    public /* bridge */ /* synthetic */ boolean isEnabled() {
        return super.isEnabled();
    }

    public /* bridge */ /* synthetic */ boolean isRadioEnabled() {
        return super.isRadioEnabled();
    }

    public /* bridge */ /* synthetic */ IBluetooth registerAdapter(IBluetoothManagerCallback iBluetoothManagerCallback) {
        return super.registerAdapter(iBluetoothManagerCallback);
    }

    public /* bridge */ /* synthetic */ void registerStateChangeCallback(IBluetoothStateChangeCallback iBluetoothStateChangeCallback) {
        super.registerStateChangeCallback(iBluetoothStateChangeCallback);
    }

    public /* bridge */ /* synthetic */ void unbindAndFinish() {
        super.unbindAndFinish();
    }

    public /* bridge */ /* synthetic */ void unbindBluetoothProfileService(int i, IBluetoothProfileServiceConnection iBluetoothProfileServiceConnection) {
        super.unbindBluetoothProfileService(i, iBluetoothProfileServiceConnection);
    }

    public /* bridge */ /* synthetic */ void unregisterAdapter(IBluetoothManagerCallback iBluetoothManagerCallback) {
        super.unregisterAdapter(iBluetoothManagerCallback);
    }

    public /* bridge */ /* synthetic */ void unregisterStateChangeCallback(IBluetoothStateChangeCallback iBluetoothStateChangeCallback) {
        super.unregisterStateChangeCallback(iBluetoothStateChangeCallback);
    }

    public /* bridge */ /* synthetic */ int updateBleAppCount(IBinder iBinder, boolean z, String str) {
        return super.updateBleAppCount(iBinder, z, str);
    }

    public HwBluetoothManagerService(Context context) {
        super(context);
        if (mBluetoothParaManager == null) {
            mBluetoothParaManager = new BluetoothParaManager(context);
        }
    }
}
