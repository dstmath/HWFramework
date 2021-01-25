package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class AvrcpController implements ProfileBase {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "AvrcpController");
    private static AvrcpController sAvrcpController;
    private AvrcpControllerProxy mAvrcpControllerProxy;
    private BluetoothHost mBluetoothHost;

    private AvrcpController(Context context) {
        this.mAvrcpControllerProxy = null;
        this.mAvrcpControllerProxy = new AvrcpControllerProxy();
        this.mBluetoothHost = BluetoothHost.getDefaultHost(context);
    }

    public static synchronized AvrcpController getProfile(Context context) {
        AvrcpController avrcpController;
        synchronized (AvrcpController.class) {
            if (sAvrcpController == null) {
                sAvrcpController = new AvrcpController(context);
            }
            avrcpController = sAvrcpController;
        }
        return avrcpController;
    }

    @Override // ohos.bluetooth.ProfileBase
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mAvrcpControllerProxy.getDevicesByStates(iArr);
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
            return this.mAvrcpControllerProxy.getDeviceState(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getDeviceState when state not on", new Object[0]);
            return 0;
        }
    }
}
