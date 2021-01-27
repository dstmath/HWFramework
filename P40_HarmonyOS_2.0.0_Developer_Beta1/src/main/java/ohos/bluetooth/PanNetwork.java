package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

@SystemApi
public final class PanNetwork implements ProfileBase {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "PanNetwork");
    private static PanNetwork sPanNetwork;
    private BluetoothHost mBluetoothHost;
    private PanNetworkProxy mPanNetworkProxy;

    private PanNetwork(Context context) {
        this.mPanNetworkProxy = null;
        this.mPanNetworkProxy = new PanNetworkProxy();
        this.mBluetoothHost = BluetoothHost.getDefaultHost(context);
    }

    public static synchronized PanNetwork getProfile(Context context) {
        PanNetwork panNetwork;
        synchronized (PanNetwork.class) {
            if (sPanNetwork == null) {
                sPanNetwork = new PanNetwork(context);
            }
            panNetwork = sPanNetwork;
        }
        return panNetwork;
    }

    @Override // ohos.bluetooth.ProfileBase
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPanNetworkProxy.getDevicesByStates(iArr);
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
            return this.mPanNetworkProxy.getDeviceState(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getDeviceState when state not on", new Object[0]);
            return 0;
        }
    }

    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "PanNetwork connect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "PanNetwork connect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPanNetworkProxy.connect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "PanNetwork connect when state not on", new Object[0]);
            return false;
        }
    }

    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "PanNetwork disconnect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "PanNetwork disconnect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPanNetworkProxy.disconnect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "PanNetwork disconnect when state not on", new Object[0]);
            return false;
        }
    }
}
