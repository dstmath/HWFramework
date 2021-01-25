package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class PbapClient implements ProfileBase {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "PbapClient");
    private static PbapClient sPbapClient;
    private BluetoothHost mBluetoothHost;
    private PbapClientProxy mPbapClientProxy;

    private PbapClient(Context context) {
        this.mPbapClientProxy = null;
        this.mPbapClientProxy = new PbapClientProxy();
        this.mBluetoothHost = BluetoothHost.getDefaultHost(context);
    }

    public static synchronized PbapClient getProfile(Context context) {
        PbapClient pbapClient;
        synchronized (PbapClient.class) {
            if (sPbapClient == null) {
                sPbapClient = new PbapClient(context);
            }
            pbapClient = sPbapClient;
        }
        return pbapClient;
    }

    @Override // ohos.bluetooth.ProfileBase
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPbapClientProxy.getDevicesByStates(iArr);
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
            return this.mPbapClientProxy.getDeviceState(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getDeviceState when state not on", new Object[0]);
            return 0;
        }
    }

    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "client connect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "client connect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPbapClientProxy.connect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "client connect when state not on", new Object[0]);
            return false;
        }
    }

    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "clinet disconnect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "clinet disconnect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPbapClientProxy.disconnect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "clinet disconnect when state not on", new Object[0]);
            return false;
        }
    }

    public boolean setConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        HiLog.info(TAG, "PbapClient setConnectStrategy", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "PbapClient setConnectStrategy got null device", new Object[0]);
            return false;
        } else if (i != 1 && i != 0) {
            HiLog.info(TAG, "PbapClient setConnectStrategy got illeagal strategy", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPbapClientProxy.setConnectStrategy(bluetoothRemoteDevice, i);
        } else {
            HiLog.info(TAG, "PbapClient setConnectStrategy when state not on", new Object[0]);
            return false;
        }
    }

    public int getConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "PbapClient getConnectStrategy", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "PbapClient getConnectStrategy got null device", new Object[0]);
            return -1;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPbapClientProxy.getConnectStrategy(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "PbapClient getConnectStrategy when state not on", new Object[0]);
            return -1;
        }
    }
}
