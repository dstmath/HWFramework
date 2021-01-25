package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class A2dpSource implements ProfileBase {
    public static final int OPTIONAL_CODECS_OPTION_DISABLE = 0;
    public static final int OPTIONAL_CODECS_OPTION_ENABLE = 1;
    public static final int OPTIONAL_CODECS_OPTION_UNKNOWN = -1;
    public static final int OPTIONAL_CODECS_SUPPORT_NONE = 0;
    public static final int OPTIONAL_CODECS_SUPPORT_SUPPORTED = 1;
    public static final int OPTIONAL_CODECS_SUPPORT_UNKNOWN = -1;
    public static final int STATE_NOT_PLAYING = 0;
    public static final int STATE_PLAYING = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "A2dpSource");
    private static A2dpSource sA2dpSource;
    private final A2dpProxy mA2dpProxy;
    private BluetoothHost mBluetoothHost;

    A2dpSource(Context context) {
        this.mA2dpProxy = A2dpProxy.getInstace(context);
        this.mBluetoothHost = BluetoothHost.getDefaultHost(context);
    }

    public static synchronized A2dpSource getProfile(Context context) {
        A2dpSource a2dpSource;
        synchronized (A2dpSource.class) {
            if (sA2dpSource == null) {
                sA2dpSource = new A2dpSource(context);
            }
            a2dpSource = sA2dpSource;
        }
        return a2dpSource;
    }

    @Override // ohos.bluetooth.ProfileBase
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.getDevicesByStatesForSource(iArr);
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
            return this.mA2dpProxy.getDeviceStateForSource(bluetoothRemoteDevice);
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
            return this.mA2dpProxy.getPlayingStateForSource(bluetoothRemoteDevice);
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
            return this.mA2dpProxy.connectSinkDevice(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "connect when host state not on", new Object[0]);
            return false;
        }
    }

    public boolean disconenct(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "disconenct", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "disconenct got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.disconnectSinkDevice(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "disconenct when host state not on", new Object[0]);
            return false;
        }
    }

    public boolean setActiveSinkDevice(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "setActiveSinkDevice", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "setActiveSinkDevice got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.setActiveDeviceForSource(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "setActiveSinkDevice when host state not on", new Object[0]);
            return false;
        }
    }

    public Optional<BluetoothRemoteDevice> getActiveSinkDevice() {
        HiLog.info(TAG, "getActiveSinkDevice", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.getActiveDeviceForSource();
        }
        HiLog.info(TAG, "getActiveSinkDevice when host state not on", new Object[0]);
        return Optional.empty();
    }

    public boolean setConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        HiLog.info(TAG, "A2dpSource setConnectStrategy", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "A2dpSource setConnectStrategy got null device", new Object[0]);
            return false;
        } else if (i != 1 && i != 0) {
            HiLog.info(TAG, "A2dpSource setConnectStrategy got illeagal strategy", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.setConnectStrategyForSource(bluetoothRemoteDevice, i);
        } else {
            HiLog.info(TAG, "A2dpSource setConnectStrategy when state not on", new Object[0]);
            return false;
        }
    }

    public int getConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "A2dpSource getConnectStrategy", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "A2dpSource getConnectStrategy got null device", new Object[0]);
            return -1;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.getConnectStrategyForSource(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "A2dpSource getConnectStrategy when state not on", new Object[0]);
            return -1;
        }
    }

    public Optional<A2dpCodecStatus> getCodecStatus(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "getCodecStatus", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.getCodecStatusForSource(bluetoothRemoteDevice);
        }
        HiLog.info(TAG, "getCodecStatus when state not on", new Object[0]);
        return Optional.empty();
    }

    public void setCodecPreference(BluetoothRemoteDevice bluetoothRemoteDevice, A2dpCodecInfo a2dpCodecInfo) {
        HiLog.info(TAG, "setCodecPreference", new Object[0]);
        if (bluetoothRemoteDevice == null || a2dpCodecInfo == null) {
            HiLog.info(TAG, "setCodecPreference got null para", new Object[0]);
        } else if (this.mBluetoothHost.getBtState() != 2) {
            HiLog.info(TAG, "setCodecPreference when state not on", new Object[0]);
        } else {
            this.mA2dpProxy.setCodecPreferenceForSource(bluetoothRemoteDevice, a2dpCodecInfo);
        }
    }

    public void switchOptionalCodecs(BluetoothRemoteDevice bluetoothRemoteDevice, boolean z) {
        HiLog.info(TAG, "switchOptionalCodecs", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "switchOptionalCodecs got null device", new Object[0]);
        } else if (this.mBluetoothHost.getBtState() != 2) {
            HiLog.info(TAG, "switchOptionalCodecs when state not on", new Object[0]);
        } else {
            this.mA2dpProxy.switchOptionalCodecsForSource(bluetoothRemoteDevice, z);
        }
    }

    public int getOptionalCodecsSupportState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "getOptionalCodecsSupportState", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "getOptionalCodecsSupportState got null device", new Object[0]);
            return -1;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.getOptionalCodecsSupportStateForSource(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getOptionalCodecsSupportState when state not on", new Object[0]);
            return -1;
        }
    }

    public int getOptionalCodecsOption(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "getOptionalCodecsOption", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "getOptionalCodecsOption got null device", new Object[0]);
            return -1;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mA2dpProxy.getOptionalCodecsOptionForSource(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getOptionalCodecsOption when state not on", new Object[0]);
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public void setOptionalCodecsOption(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        HiLog.info(TAG, "setOptionalCodecsOption", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "setOptionalCodecsOption got null device", new Object[0]);
        } else if (i != -1 && i != 0 && i != 1) {
            HiLog.info(TAG, "setOptionalCodecsOption got error option value", new Object[0]);
        } else if (this.mBluetoothHost.getBtState() != 2) {
            HiLog.info(TAG, "setOptionalCodecsOption when state not on", new Object[0]);
        } else {
            this.mA2dpProxy.setOptionalCodecsOptionForSource(bluetoothRemoteDevice, i);
        }
    }
}
