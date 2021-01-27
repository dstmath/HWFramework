package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.SequenceUuid;

public final class BluetoothHost {
    private static final int ADDR_VALID_LENGTH = 17;
    public static final int BLE_CAP_2M_PHY = 1;
    public static final int BLE_CAP_CODED_PHY = 2;
    public static final int BLE_CAP_EXTENDED_ADV = 4;
    public static final int BLE_CAP_MULTIPLE_ADV = 16;
    public static final int BLE_CAP_OFFLOADED_FILTER = 32;
    public static final int BLE_CAP_OFFLOADED_SCAN_BATCH = 64;
    public static final int BLE_CAP_PERIODIC_ADV = 8;
    public static final String EVENT_HOST_DISCOVERY_FINISHED = "usual.event.bluetooth.host.DISCOVERY_FINISHED";
    public static final String EVENT_HOST_DISCOVERY_STARTED = "usual.event.bluetooth.host.DISCOVERY_STARTED";
    public static final String EVENT_HOST_NAME_UPDATE = "usual.event.bluetooth.host.NAME_UPDATE";
    public static final String EVENT_HOST_SCAN_MODE_UPDATE = "usual.event.bluetooth.host.SCAN_MODE_UPDATE";
    public static final String EVENT_HOST_STATE_UPDATE = "usual.event.bluetooth.host.STATE_UPDATE";
    public static final String HOST_PARAM_CUR_STATE = "usual.event.bluetoothhost.PARAM_CUR_STATE";
    public static final String HOST_PARAM_DISCOVERABLE_TERM = "usual.event.bluetoothhost.PARAM_DISCOVERABLE_TERM";
    public static final String HOST_PARAM_HOST_NAME = "usual.event.bluetoothhost.PARAM_HOST_NAME";
    public static final String HOST_PARAM_PRE_STATE = "usual.event.bluetoothhost.PARAM_PRE_STATE";
    public static final String HOST_PARAM_SCAN_METHOD = "usual.event.bluetoothhost.PARAM_SCAN_METHOD";
    public static final int SCAN_MODE_CONNECTABLE = 1;
    public static final int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 3;
    public static final int SCAN_MODE_NONE = 0;
    public static final int STATE_BLE_ON = 5;
    public static final int STATE_BLE_TURNING_OFF = 6;
    public static final int STATE_BLE_TURNING_ON = 4;
    public static final int STATE_OFF = 0;
    public static final int STATE_ON = 2;
    public static final int STATE_TURNING_OFF = 3;
    public static final int STATE_TURNING_ON = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BluetoothHost");
    private static BluetoothHost sBluetoothHost;
    private final BluetoothHostProxy mBluetoothHostProxy = BluetoothHostProxy.getInstace();
    private final Context mContext;

    BluetoothHost(Context context) {
        this.mContext = context;
    }

    public static synchronized BluetoothHost getDefaultHost(Context context) {
        BluetoothHost bluetoothHost;
        synchronized (BluetoothHost.class) {
            if (sBluetoothHost == null) {
                sBluetoothHost = new BluetoothHost(context);
            }
            bluetoothHost = sBluetoothHost;
        }
        return bluetoothHost;
    }

    public boolean enableBt() {
        HiLog.info(TAG, "enable", new Object[0]);
        if (getBtState() == 2) {
            HiLog.info(TAG, "enable : already state on", new Object[0]);
            return true;
        }
        Context context = this.mContext;
        return this.mBluetoothHostProxy.enableBt(context != null ? context.getBundleName() : "");
    }

    public boolean disableBt() {
        HiLog.info(TAG, "disable", new Object[0]);
        Context context = this.mContext;
        return this.mBluetoothHostProxy.disableBt(context != null ? context.getBundleName() : "");
    }

    public int getBtState() {
        HiLog.info(TAG, "getState", new Object[0]);
        return this.mBluetoothHostProxy.getBtState();
    }

    public BluetoothRemoteDevice getRemoteDev(String str) {
        return new BluetoothRemoteDevice(str);
    }

    public Optional<String> getLocalName() {
        HiLog.info(TAG, "getName", new Object[0]);
        return this.mBluetoothHostProxy.getLocalName();
    }

    public boolean setLocalName(String str) {
        HiLog.info(TAG, "setName", new Object[0]);
        if (getBtState() == 2) {
            return this.mBluetoothHostProxy.setLocalName(str);
        }
        HiLog.info(TAG, "setName : state not on", new Object[0]);
        return false;
    }

    public boolean startBtDiscovery() {
        HiLog.info(TAG, "startDiscovery begin", new Object[0]);
        if (getBtState() != 2) {
            HiLog.info(TAG, "startDiscovery : state not on", new Object[0]);
            return false;
        }
        Context context = this.mContext;
        return this.mBluetoothHostProxy.startBtDiscovery(context != null ? context.getBundleName() : "");
    }

    public boolean cancelBtDiscovery() {
        HiLog.info(TAG, "cancelDiscovery", new Object[0]);
        if (getBtState() == 2) {
            return this.mBluetoothHostProxy.cancelBtDiscovery();
        }
        HiLog.info(TAG, "cancelDiscovery : state not on", new Object[0]);
        return false;
    }

    public boolean isBtDiscovering() {
        HiLog.info(TAG, "isDiscovering", new Object[0]);
        if (getBtState() == 2) {
            return this.mBluetoothHostProxy.isBtDiscovering();
        }
        HiLog.info(TAG, "isDiscovering : state not on", new Object[0]);
        return false;
    }

    public int getBtScanMode() {
        HiLog.info(TAG, "getScanMode", new Object[0]);
        if (getBtState() == 2) {
            return this.mBluetoothHostProxy.getBtScanMode();
        }
        HiLog.info(TAG, "getScanMode : state not on", new Object[0]);
        return 0;
    }

    public int getProfileConnState(int i) {
        HiLog.info(TAG, "getProfileConnState : %{public}d", new Object[]{Integer.valueOf(i)});
        if (getBtState() != 2) {
            HiLog.info(TAG, "getProfileConnState : state not on", new Object[0]);
            return 0;
        } else if (i == 1 || i == 3) {
            return this.mBluetoothHostProxy.getProfileConnState(i);
        } else {
            HiLog.info(TAG, "getProfileConnState : profile not supported", new Object[0]);
            return 0;
        }
    }

    public List<BluetoothRemoteDevice> getPairedDevices() {
        HiLog.info(TAG, "getPairedDevices", new Object[0]);
        if (getBtState() == 2) {
            return this.mBluetoothHostProxy.getPairedDevices();
        }
        HiLog.info(TAG, "getPairedDevices, bluetooth is not enabled", new Object[0]);
        return new ArrayList();
    }

    public int getBleCapabilities() {
        HiLog.info(TAG, "getBleCapabilities", new Object[0]);
        if (getBtState() == 5 || getBtState() == 2) {
            return this.mBluetoothHostProxy.getBleCapabilities();
        }
        HiLog.info(TAG, "getBleCapabilities : state not supported", new Object[0]);
        return 0;
    }

    public int getBleMaxAdvertisingDataLength() {
        HiLog.info(TAG, "getBleMaxAdvertisingDataLength", new Object[0]);
        if (getBtState() == 5 || getBtState() == 2) {
            return this.mBluetoothHostProxy.getBleMaxAdvertisingDataLength();
        }
        HiLog.info(TAG, "getBleMaxAdvertisingDataLength : state not supported", new Object[0]);
        return 0;
    }

    public boolean setBtScanMode(int i, int i2) {
        HiLog.info(TAG, "setScanMode", new Object[0]);
        if (getBtState() == 2) {
            return this.mBluetoothHostProxy.setBtScanMode(i, i2);
        }
        HiLog.info(TAG, "setScanMode : state not supported", new Object[0]);
        return false;
    }

    public boolean setDevicePairingConfirmation(BluetoothRemoteDevice bluetoothRemoteDevice, boolean z) {
        HiLog.info(TAG, "setPairingConfirmation", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "setDevicePairingConfirmation got null device", new Object[0]);
            return false;
        } else if (getBtState() == 2) {
            return this.mBluetoothHostProxy.setDevicePairingConfirmation(bluetoothRemoteDevice, z);
        } else {
            HiLog.info(TAG, "setPairingConfirmation : state not supported", new Object[0]);
            return false;
        }
    }

    public boolean removePair(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "removePair", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "removePair got null device", new Object[0]);
            return false;
        } else if (getBtState() == 2) {
            return this.mBluetoothHostProxy.removePair(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "removePair : state not supported", new Object[0]);
            return false;
        }
    }

    public Optional<String> getLocalAddress() {
        HiLog.info(TAG, "getLocalAddress", new Object[0]);
        return this.mBluetoothHostProxy.getLocalAddress();
    }

    public boolean bluetoothFactoryReset() {
        HiLog.info(TAG, "bluetoothFactoryReset", new Object[0]);
        return this.mBluetoothHostProxy.bluetoothFactoryReset();
    }

    public SequenceUuid[] getlocalSupportedUuids() {
        HiLog.info(TAG, "getlocalSupportedUuids", new Object[0]);
        if (getBtState() == 2) {
            return this.mBluetoothHostProxy.getlocalSupportedUuids();
        }
        HiLog.info(TAG, "getlocalSupportedUuids : state not on", new Object[0]);
        return new SequenceUuid[0];
    }

    public long getBtDiscoveryEndMillis() {
        HiLog.info(TAG, "getBtDiscoveryEndMillis", new Object[0]);
        if (getBtState() == 2) {
            return this.mBluetoothHostProxy.getBtDiscoveryEndMillis();
        }
        HiLog.info(TAG, "getBtDiscoveryEndMillis : state not on", new Object[0]);
        return -1;
    }

    public int getMaxNumConnectedAudioDevices() {
        HiLog.info(TAG, "getMaxNumConnectedAudioDevices", new Object[0]);
        if (getBtState() == 2) {
            return this.mBluetoothHostProxy.getMaxNumConnectedAudioDevices();
        }
        HiLog.info(TAG, "getMaxNumConnectedAudioDevices : state not on", new Object[0]);
        return 1;
    }

    public List<Integer> getProfileList() {
        HiLog.info(TAG, "getProfileList", new Object[0]);
        return this.mBluetoothHostProxy.getProfileList();
    }

    public int getBtConnectionState() {
        HiLog.info(TAG, "getBtConnectionState", new Object[0]);
        return this.mBluetoothHostProxy.getBtConnectionState();
    }

    public static boolean isValidBluetoothAddr(String str) {
        if (str == null || str.length() != 17) {
            HiLog.info(TAG, "isValidBluetoothAddr addr length wrong", new Object[0]);
            return false;
        } else if (str.matches("([A-F0-9]{2}[:]){5}[A-F0-9]{2}")) {
            return true;
        } else {
            HiLog.info(TAG, "isValidBluetoothAddr data wrong, input: %{private}s", new Object[]{str});
            return false;
        }
    }
}
