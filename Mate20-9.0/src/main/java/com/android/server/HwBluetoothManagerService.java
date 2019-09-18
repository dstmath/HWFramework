package com.android.server;

import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManagerCallback;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.huawei.hsm.permission.StubController;
import com.huawei.ncdft.HwNcDftConnManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwBluetoothManagerService extends BluetoothManagerService {
    private static final short BT_EXCEPTION_INFO = 0;
    private static final int DOMAIN_BT = 3;
    private static final String TAG = "HwBluetoothManagerService";
    private static BluetoothParaManager mBluetoothParaManager = null;
    private HwNcDftConnManager hwNcDftConnManager;

    public /* bridge */ /* synthetic */ boolean bindBluetoothProfileService(int x0, IBluetoothProfileServiceConnection x1) {
        return HwBluetoothManagerService.super.bindBluetoothProfileService(x0, x1);
    }

    public /* bridge */ /* synthetic */ boolean disable(String x0, boolean x1) throws RemoteException {
        return HwBluetoothManagerService.super.disable(x0, x1);
    }

    public /* bridge */ /* synthetic */ boolean disableRadio() {
        return HwBluetoothManagerService.super.disableRadio();
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

    public /* bridge */ /* synthetic */ boolean enableRadio() {
        return HwBluetoothManagerService.super.enableRadio();
    }

    public /* bridge */ /* synthetic */ String getAddress() {
        return HwBluetoothManagerService.super.getAddress();
    }

    public /* bridge */ /* synthetic */ IBluetoothGatt getBluetoothGatt() {
        return HwBluetoothManagerService.super.getBluetoothGatt();
    }

    public /* bridge */ /* synthetic */ String getName() {
        return HwBluetoothManagerService.super.getName();
    }

    public /* bridge */ /* synthetic */ void getNameAndAddress() {
        HwBluetoothManagerService.super.getNameAndAddress();
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

    public /* bridge */ /* synthetic */ boolean isRadioEnabled() {
        return HwBluetoothManagerService.super.isRadioEnabled();
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
        this.hwNcDftConnManager = new HwNcDftConnManager(context);
    }

    public boolean checkPrecondition(int uid) {
        return StubController.checkPrecondition(uid);
    }

    public void reportBtServiceChrToDft(int code, int subcode, String apkName) {
        HwLog.e(TAG, " reportBtServiceChrToDft enter");
        Bundle data = new Bundle();
        data.putString("btErrorCode", String.valueOf(code));
        data.putString("btSubErrorCode", String.valueOf(subcode));
        data.putString(HwSecDiagnoseConstant.MALAPP_APK_NAME, apkName);
        if (this.hwNcDftConnManager != null) {
            this.hwNcDftConnManager.reportToDft(3, 0, data);
        } else {
            HwLog.e(TAG, "reportBtServiceChrToDft,mClient is null");
        }
    }
}
