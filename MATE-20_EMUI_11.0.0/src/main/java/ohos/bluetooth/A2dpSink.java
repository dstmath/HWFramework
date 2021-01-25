package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class A2dpSink implements ProfileBase {
    public static final int STATE_NOT_PLAYING = 0;
    public static final int STATE_PLAYING = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "A2dpSink");
    private static A2dpSink sA2dpSink;
    private final A2dpProxy mA2dpProxy;
    private BluetoothHost mBluetoothHost;

    A2dpSink(Context context) {
        this.mA2dpProxy = A2dpProxy.getInstace(context);
        this.mBluetoothHost = BluetoothHost.getDefaultHost(context);
    }

    public static synchronized A2dpSink getProfile(Context context) {
        A2dpSink a2dpSink;
        synchronized (A2dpSink.class) {
            if (sA2dpSink == null) {
                sA2dpSink = new A2dpSink(context);
            }
            a2dpSink = sA2dpSink;
        }
        return a2dpSink;
    }

    @Override // ohos.bluetooth.ProfileBase
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.getDevicesByStatesForSink(iArr);
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
            return this.mA2dpProxy.getDeviceStateForSink(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getDeviceState when state not on", new Object[0]);
            return 0;
        }
    }

    public int getPlayingState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "getPlayingState", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "getPlayingState got null device", new Object[0]);
            return 0;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.getPlayingStateForSink(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getPlayingState when host state not on", new Object[0]);
            return 0;
        }
    }

    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "connect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "connect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.connectSourceDevice(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "connect operation when host state not on", new Object[0]);
            return false;
        }
    }

    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "disconnect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "disconnect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.disconnectSourceDevice(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "disconnect operation when host state not on", new Object[0]);
            return false;
        }
    }

    public boolean setConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        HiLog.info(TAG, "A2dpSink setConnectStrategy", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "A2dpSink setConnectStrategy got null device", new Object[0]);
            return false;
        } else if (i != 1 && i != 0) {
            HiLog.info(TAG, "A2dpSink setConnectStrategy got illeagal strategy", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.setConnectStrategyForSink(bluetoothRemoteDevice, i);
        } else {
            HiLog.info(TAG, "A2dpSink setConnectStrategy when state not on", new Object[0]);
            return false;
        }
    }

    public int getConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "A2dpSink getConnectStrategy", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "A2dpSink getConnectStrategy got null device", new Object[0]);
            return -1;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.getConnectStrategyForSink(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "A2dpSink getConnectStrategy when state not on", new Object[0]);
            return -1;
        }
    }
}
