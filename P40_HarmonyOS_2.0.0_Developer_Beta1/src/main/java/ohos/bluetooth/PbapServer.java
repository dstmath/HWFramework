package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

@SystemApi
public final class PbapServer implements ProfileBase {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "PbapServer");
    private static PbapServer sPbapServer;
    private BluetoothHost mBluetoothHost;
    private PbapServerProxy mPbapServerProxy;

    private PbapServer(Context context) {
        this.mPbapServerProxy = null;
        this.mPbapServerProxy = new PbapServerProxy();
        this.mBluetoothHost = BluetoothHost.getDefaultHost(context);
    }

    public static synchronized PbapServer getProfile(Context context) {
        PbapServer pbapServer;
        synchronized (PbapServer.class) {
            if (sPbapServer == null) {
                sPbapServer = new PbapServer(context);
            }
            pbapServer = sPbapServer;
        }
        return pbapServer;
    }

    @Override // ohos.bluetooth.ProfileBase
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPbapServerProxy.getDevicesByStates(iArr);
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
            return this.mPbapServerProxy.getDeviceState(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getDeviceState when state not on", new Object[0]);
            return 0;
        }
    }

    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "server disconnect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "server disconnect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mPbapServerProxy.disconnect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "server disconnect when state not on", new Object[0]);
            return false;
        }
    }
}
