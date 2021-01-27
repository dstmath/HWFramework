package ohos.bluetooth;

import java.util.Objects;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.SequenceUuid;
import ohos.utils.Sequenceable;

public final class BluetoothRemoteDevice implements Sequenceable {
    public static final String EVENT_DEVICE_DISCOVERED = "usual.event.bluetooth.remotedevice.DISCOVERED";
    public static final String EVENT_DEVICE_PAIRING_REQ = "usual.event.bluetooth.remotedevice.PAIRING_REQ";
    public static final String EVENT_DEVICE_PAIR_STATE = "usual.event.bluetooth.remotedevice.PAIR_STATE";
    public static final int PAIR_STATE_NONE = 0;
    public static final int PAIR_STATE_PAIRED = 2;
    public static final int PAIR_STATE_PAIRING = 1;
    public static final int PERMISSION_ALLOW = 1;
    public static final int PERMISSION_REJECT = 2;
    public static final int PERMISSION_UNKNOWN = 0;
    public static final String REMOTE_DEVICE_PARAM_DEVICE = "usual.event.remotedevice.PARAM_DEVICE";
    public static final String REMOTE_DEVICE_PARAM_PAIRING_FORMAT = "usual.event.remotedevice.PARAM_PAIRING_FORMAT";
    public static final String REMOTE_DEVICE_PARAM_PAIRING_KEY = "usual.event.remotedevice.PARAM_PAIRING_KEY";
    public static final String REMOTE_DEVICE_PARAM_PAIR_STATE = "usual.event.remotedevice.PARAM_PAIR_STATE";
    public static final String REMOTE_DEVICE_PARAM_PREV_PAIR_STATE = "usual.event.remotedevice.PARAM_PREV_PAIR_STATE";
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BluetoothRemoteDevice");
    public static final int TYPE_CLASSIC = 1;
    public static final int TYPE_DUAL = 3;
    public static final int TYPE_LE = 2;
    public static final int TYPE_UNKNOWN = 0;
    private static volatile BluetoothHostProxy sBluetoothHostProxy;
    private String mAddr;

    BluetoothRemoteDevice(String str) {
        if (BluetoothHost.isValidBluetoothAddr(str)) {
            checkProxy();
            this.mAddr = str;
            return;
        }
        throw new IllegalArgumentException("can not create BluetoothRemoteDevice with addr : " + str);
    }

    BluetoothRemoteDevice() {
        checkProxy();
    }

    private static void checkProxy() {
        if (sBluetoothHostProxy == null) {
            sBluetoothHostProxy = BluetoothHostProxy.getInstace();
        }
    }

    public String getDeviceAddr() {
        return this.mAddr;
    }

    public Optional<String> getDeviceName() {
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy == null) {
            return Optional.empty();
        }
        return bluetoothHostProxy.getRemoteName(this);
    }

    public int getDeviceType() {
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy == null) {
            return 0;
        }
        return bluetoothHostProxy.getDeviceType(this);
    }

    public Optional<BluetoothDeviceClass> getDeviceClass() {
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(new BluetoothDeviceClass(bluetoothHostProxy.getRemoteDeviceClass(this)));
    }

    public int getPairState() {
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy == null) {
            return 0;
        }
        return bluetoothHostProxy.getPairState(this);
    }

    public boolean startPair() {
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy == null) {
            return false;
        }
        return bluetoothHostProxy.startPair(this);
    }

    public boolean setDevicePin(byte[] bArr) {
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy == null) {
            return false;
        }
        return bluetoothHostProxy.setDevicePin(this, bArr);
    }

    public Optional<String> getDeviceAlias() {
        HiLog.info(TAG, "getDeviceAlias", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.getRemoteAlias(this);
        }
        HiLog.error(TAG, "getDeviceAlias proxy null or state not on", new Object[0]);
        return Optional.empty();
    }

    public boolean setDeviceAlias(String str) {
        HiLog.info(TAG, "setDeviceAlias", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.setRemoteAlias(this, str);
        }
        HiLog.error(TAG, "setDeviceAlias proxy null or state not on", new Object[0]);
        return false;
    }

    public int getDeviceBatteryLevel() {
        HiLog.info(TAG, "getDeviceBatteryLevel", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.getDeviceBatteryLevel(this);
        }
        HiLog.error(TAG, "getDeviceBatteryLevel proxy null or state not on", new Object[0]);
        return -1;
    }

    public boolean isBondedFromLocal() {
        HiLog.info(TAG, "isBondedFromLocal", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.isBondedFromLocal(this);
        }
        HiLog.error(TAG, "isBondedFromLocal proxy null or state not on", new Object[0]);
        return false;
    }

    public boolean cancelPairing() {
        HiLog.info(TAG, "cancelPairing", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.cancelPairing(this);
        }
        HiLog.error(TAG, "cancelPairing proxy null or state not on", new Object[0]);
        return false;
    }

    public boolean isAclConnected() {
        HiLog.info(TAG, "isAclConnected", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.isAclConnected(this);
        }
        HiLog.error(TAG, "isAclConnected proxy null or state not on", new Object[0]);
        return false;
    }

    public boolean isAclEncrypted() {
        HiLog.info(TAG, "isAclEncrypted", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.isAclEncrypted(this);
        }
        HiLog.error(TAG, "isAclEncrypted proxy null or state not on", new Object[0]);
        return false;
    }

    public SequenceUuid[] getDeviceUuids() {
        HiLog.info(TAG, "getDeviceUuids", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.getDeviceUuids(this);
        }
        HiLog.error(TAG, "getDeviceUuids proxy null or state not on", new Object[0]);
        return new SequenceUuid[0];
    }

    public int getPhonebookPermission() {
        HiLog.info(TAG, "getPhonebookPermission", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.getPhonebookPermission(this);
        }
        HiLog.error(TAG, "getPhonebookPermission proxy null or state not on", new Object[0]);
        return 0;
    }

    public boolean setPhonebookPermission(int i) {
        HiLog.info(TAG, "setPhonebookPermission", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.setPhonebookPermission(this, i);
        }
        HiLog.error(TAG, "setPhonebookPermission proxy null or state not on", new Object[0]);
        return false;
    }

    public int getMessagePermission() {
        HiLog.info(TAG, "getMessagePermission", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.getMessagePermission(this);
        }
        HiLog.error(TAG, "getMessagePermission proxy null or state not on", new Object[0]);
        return 0;
    }

    public boolean setMessagePermission(int i) {
        HiLog.info(TAG, "setMessagePermission", new Object[0]);
        BluetoothHostProxy bluetoothHostProxy = sBluetoothHostProxy;
        if (bluetoothHostProxy != null && bluetoothHostProxy.getBtState() == 2) {
            return bluetoothHostProxy.setMessagePermission(this, i);
        }
        HiLog.error(TAG, "setMessagePermission proxy null or state not on", new Object[0]);
        return false;
    }

    public String toString() {
        return this.mAddr;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.mAddr);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        String readString = parcel.readString();
        if (!BluetoothHost.isValidBluetoothAddr(readString)) {
            return false;
        }
        this.mAddr = readString;
        return true;
    }

    public int hashCode() {
        return this.mAddr.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof BluetoothRemoteDevice) {
            return Objects.equals(((BluetoothRemoteDevice) obj).getDeviceAddr(), this.mAddr);
        }
        return false;
    }
}
