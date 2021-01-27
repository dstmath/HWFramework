package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

@SystemApi
public final class HidHost implements ProfileBase {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "HidHost");
    private static HidHost sHidHost;
    private BluetoothHost mBluetoothHost;
    private HidHostProxy mHidHostProxy;

    private HidHost(Context context) {
        this.mHidHostProxy = null;
        this.mHidHostProxy = new HidHostProxy();
        this.mBluetoothHost = BluetoothHost.getDefaultHost(context);
    }

    public static synchronized HidHost getProfile(Context context) {
        HidHost hidHost;
        synchronized (HidHost.class) {
            if (sHidHost == null) {
                sHidHost = new HidHost(context);
            }
            hidHost = sHidHost;
        }
        return hidHost;
    }

    @Override // ohos.bluetooth.ProfileBase
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHidHostProxy.getDevicesByStates(iArr);
        }
        HiLog.info(TAG, "getDevicesByStates when state not on", new Object[0]);
        return new ArrayList();
    }

    @Override // ohos.bluetooth.ProfileBase
    public int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "getDeviceState", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "getDeviceState got null device", new Object[0]);
            return 0;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHidHostProxy.getDeviceState(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getDeviceState when state not on", new Object[0]);
            return 0;
        }
    }

    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "HidHost connect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "HidHost connect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHidHostProxy.connect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "HidHost connect when state not on", new Object[0]);
            return false;
        }
    }

    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "HidHost disconnect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "HidHost disconnect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHidHostProxy.disconnect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "HidHost disconnect when state not on", new Object[0]);
            return false;
        }
    }
}
