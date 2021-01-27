package ohos.bluetooth.ble;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.UUID;
import ohos.app.Context;
import ohos.bluetooth.BluetoothHost;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.utils.SequenceUuid;

public final class BleAdvertiser {
    private static final SequenceUuid BASIC_UUID = SequenceUuid.uuidFromString("00000000-0000-1000-8000-00805F9B34FB");
    private static final int FIELD_HEAD_LENGTH = 2;
    private static final int FLAG_FIELD_LENGTH = 3;
    private static final Object LOCK_STATIC = new Object();
    private static final int MAX_LEGACY_ADV_SIZE = 31;
    private static final int SERVICE_UUID_BYTE_LENGTH_128_BIT = 16;
    private static final int SERVICE_UUID_BYTE_LENGTH_16_BIT = 2;
    private static final int SERVICE_UUID_BYTE_LENGTH_32_BIT = 4;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BleAdvertiser");
    private static final int UUID_TYPE_128_BIT = 3;
    private static final int UUID_TYPE_16_BIT = 1;
    private static final int UUID_TYPE_32_BIT = 2;
    private static HashSet<BleAdvertiseCallback> sCallbacks = new HashSet<>();
    private BleAdvertiseCallback mBleAdvertiserCallback;
    private BleAdvertiserProxy mBleAdvertiserProxy;
    private Context mContext;
    private boolean mIsStarted;

    public BleAdvertiser(Context context, BleAdvertiseCallback bleAdvertiseCallback) {
        if (bleAdvertiseCallback != null) {
            synchronized (LOCK_STATIC) {
                if (!sCallbacks.contains(bleAdvertiseCallback)) {
                    sCallbacks.add(bleAdvertiseCallback);
                } else {
                    throw new IllegalArgumentException("callback is using");
                }
            }
            this.mContext = context;
            this.mBleAdvertiserCallback = bleAdvertiseCallback;
            this.mBleAdvertiserProxy = new BleAdvertiserProxy(this.mBleAdvertiserCallback);
            return;
        }
        throw new IllegalArgumentException("callback cannot be null");
    }

    public void startAdvertising(BleAdvertiseSettings bleAdvertiseSettings, BleAdvertiseData bleAdvertiseData, BleAdvertiseData bleAdvertiseData2) {
        if (this.mBleAdvertiserCallback != null) {
            int btState = BluetoothHost.getDefaultHost(this.mContext).getBtState();
            if (btState != 2 && btState != 5) {
                throw new IllegalStateException("host is not ON state");
            } else if (getAdvDataSize(bleAdvertiseData, bleAdvertiseSettings.isConnectable()) > 31 || getAdvDataSize(bleAdvertiseData2, false) > 31) {
                HiLog.error(TAG, "startAdvertising data too large", new Object[0]);
                this.mBleAdvertiserCallback.startResultEvent(1);
            } else if (this.mIsStarted) {
                HiLog.error(TAG, "startAdvertising already started", new Object[0]);
                this.mBleAdvertiserCallback.startResultEvent(3);
            } else {
                try {
                    this.mBleAdvertiserProxy.startAdvertising(bleAdvertiseSettings, bleAdvertiseData, bleAdvertiseData2);
                    this.mIsStarted = true;
                } catch (RemoteException unused) {
                    HiLog.error(TAG, "startAdvertising call fail", new Object[0]);
                }
            }
        } else {
            throw new IllegalStateException("startAdvertising advertiser already closed");
        }
    }

    public void stopAdvertising() {
        if (this.mBleAdvertiserCallback == null) {
            HiLog.error(TAG, "stopAdvertising advertiser already closed", new Object[0]);
        } else if (!this.mIsStarted) {
            HiLog.error(TAG, "stopAdvertising not started", new Object[0]);
        } else {
            try {
                this.mBleAdvertiserProxy.stopAdvertising();
                this.mIsStarted = false;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "stopAdvertising call fail", new Object[0]);
            }
        }
    }

    public void close() {
        if (this.mBleAdvertiserCallback == null) {
            HiLog.error(TAG, "already closed", new Object[0]);
            return;
        }
        stopAdvertising();
        synchronized (LOCK_STATIC) {
            sCallbacks.remove(this.mBleAdvertiserCallback);
        }
        this.mBleAdvertiserCallback = null;
    }

    private int getAdvDataSize(BleAdvertiseData bleAdvertiseData, boolean z) {
        if (bleAdvertiseData == null) {
            return 0;
        }
        int i = z ? 3 : 0;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        for (SequenceUuid sequenceUuid : bleAdvertiseData.getServiceUuids()) {
            int uuidType = getUuidType(sequenceUuid);
            if (uuidType == 1) {
                i += 2;
                z2 = true;
            } else if (uuidType != 2) {
                i += 16;
                z4 = true;
            } else {
                i += 4;
                z3 = true;
            }
        }
        int i2 = i + (z2 ? 2 : 0) + (z3 ? 2 : 0) + (z4 ? 2 : 0);
        for (SequenceUuid sequenceUuid2 : bleAdvertiseData.getServiceData().keySet()) {
            i2 += uuidToByteArray(sequenceUuid2).length + 2 + getByteArrayLength(bleAdvertiseData.getServiceData().get(sequenceUuid2));
        }
        for (int i3 = 0; i3 < bleAdvertiseData.getManufacturerData().size(); i3++) {
            i2 += getByteArrayLength((byte[]) bleAdvertiseData.getManufacturerData().valueAt(i3)) + 4;
        }
        return i2;
    }

    private static int getUuidType(SequenceUuid sequenceUuid) {
        UUID uuid = sequenceUuid.getUuid();
        if (uuid.getLeastSignificantBits() != BASIC_UUID.getUuid().getLeastSignificantBits()) {
            return 3;
        }
        if ((uuid.getMostSignificantBits() & -281470681743361L) == 4096) {
            return 1;
        }
        if ((uuid.getMostSignificantBits() & 4294967295L) == 4096) {
            return 2;
        }
        return 3;
    }

    private static byte[] uuidToByteArray(SequenceUuid sequenceUuid) {
        if (sequenceUuid == null) {
            return new byte[0];
        }
        long mostSignificantBits = sequenceUuid.getUuid().getMostSignificantBits();
        int i = (int) ((-4294967296L & mostSignificantBits) >>> 32);
        int uuidType = getUuidType(sequenceUuid);
        if (uuidType == 1) {
            return new byte[]{(byte) (i & 255), (byte) ((i & 65280) >> 8)};
        }
        if (uuidType == 2) {
            return new byte[]{(byte) (i & 255), (byte) ((i & 65280) >> 8), (byte) ((16711680 & i) >> 16), (byte) ((-16777216 & i) >> 24)};
        }
        long leastSignificantBits = sequenceUuid.getUuid().getLeastSignificantBits();
        byte[] bArr = new byte[16];
        ByteBuffer order = ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN);
        order.putLong(8, mostSignificantBits);
        order.putLong(0, leastSignificantBits);
        return bArr;
    }

    private int getByteArrayLength(byte[] bArr) {
        if (bArr == null) {
            return 0;
        }
        return bArr.length;
    }
}
