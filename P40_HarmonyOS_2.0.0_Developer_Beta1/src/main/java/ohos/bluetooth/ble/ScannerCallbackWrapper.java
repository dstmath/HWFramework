package ohos.bluetooth.ble;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public class ScannerCallbackWrapper extends RemoteObject implements IBleCallback {
    private static final String BASE_UUID = "00000000-0000-1000-8000-00805F9B34FB";
    private static final int DATA_MASK = 255;
    private static final int GROUP_SCAN_RESULT = 3;
    private static final int INVALID_DATA_FLAG = 0;
    private static final int MASK_FLAGS = 1;
    private static final int MASK_MANUFACTURER_SPECIFIC_DATA = 255;
    private static final int MASK_SERVICE_DATA_128_BIT = 33;
    private static final int MASK_SERVICE_DATA_16_BIT = 22;
    private static final int MASK_SERVICE_DATA_32_BIT = 32;
    private static final int MASK_SERVICE_UUIDS_128_BIT = 6;
    private static final int MASK_SERVICE_UUIDS_128_BIT_END = 7;
    private static final int MASK_SERVICE_UUIDS_16_BIT = 2;
    private static final int MASK_SERVICE_UUIDS_16_BIT_END = 3;
    private static final int MASK_SERVICE_UUIDS_32_BIT = 4;
    private static final int MASK_SERVICE_UUIDS_32_BIT_END = 5;
    private static final int SCANNER_ADD_FINISH = 1;
    private static final int SCAN_MANAGER_ERROR = 5;
    private static final int SCAN_RESULT = 2;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "ScannerCallbackWrapper");
    private static final int UUID_BYTES_128_BIT_LEN = 16;
    private static final int UUID_BYTES_16_BIT_LEN = 2;
    private static final int UUID_BYTES_32_BIT_LEN = 4;
    private static final int VALID_DATA_FLAG = 1;
    private BleCentralManager mBleCentralManager;

    public IRemoteObject asObject() {
        return this;
    }

    public ScannerCallbackWrapper(BleCentralManager bleCentralManager) {
        super("ohos.bluetooth.ble.ScannerCallbackWrapper");
        this.mBleCentralManager = bleCentralManager;
    }

    private void enforceInterface(MessageParcel messageParcel) {
        messageParcel.readInt();
        messageParcel.readInt();
        messageParcel.readString();
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        boolean z = true;
        HiLog.info(TAG, "onRemoteRequest code %{public}d", new Object[]{Integer.valueOf(i)});
        if (this.mBleCentralManager == null) {
            HiLog.error(TAG, "no central manager instance", new Object[0]);
            return ScannerCallbackWrapper.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        if (i == 1) {
            enforceInterface(messageParcel);
            this.mBleCentralManager.onScannerAddFinish(messageParcel.readInt(), messageParcel.readInt());
        } else if (i == 2) {
            enforceInterface(messageParcel);
            Optional<BleScanResult> empty = Optional.empty();
            if (messageParcel.readInt() != 0) {
                String readString = messageParcel.readInt() == 1 ? messageParcel.readString() : "";
                if (messageParcel.readInt() == 1) {
                    empty = readFromBytes(messageParcel.readByteArray(), new BlePeripheralDevice(readString));
                }
                int readInt = messageParcel.readInt();
                long readLong = messageParcel.readLong();
                int readInt2 = messageParcel.readInt();
                messageParcel.readInt();
                messageParcel.readInt();
                messageParcel.readInt();
                messageParcel.readInt();
                messageParcel.readInt();
                if (empty.isPresent()) {
                    empty.get().setRssi(readInt);
                    empty.get().setTime(readLong);
                    BleScanResult bleScanResult = empty.get();
                    if ((readInt2 & 1) == 0) {
                        z = false;
                    }
                    bleScanResult.setIsConnectable(z);
                }
            }
            this.mBleCentralManager.onScanResult(empty.orElse(null));
        } else if (i == 3) {
            enforceInterface(messageParcel);
            int readInt3 = messageParcel.readInt();
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < readInt3; i2++) {
                if (messageParcel.getReadableBytes() <= 0) {
                    HiLog.warn(TAG, "unmashalling failed due to data size mismatch", new Object[0]);
                    return true;
                }
                if (messageParcel.readInt() != 0) {
                    Optional<BlePeripheralDevice> empty2 = Optional.empty();
                    if (messageParcel.readInt() == 1) {
                        empty2 = createDeviceFromParcel(messageParcel);
                    }
                    Optional<BleScanResult> empty3 = Optional.empty();
                    if (messageParcel.readInt() == 1) {
                        byte[] readByteArray = messageParcel.readByteArray();
                        if (empty2.isPresent()) {
                            empty3 = readFromBytes(readByteArray, empty2.get());
                        }
                    }
                    if (empty3.isPresent()) {
                        empty3.get().setRssi(messageParcel.readInt());
                        empty3.get().setTime(messageParcel.readLong());
                        empty3.get().setIsConnectable((messageParcel.readInt() & 1) != 0);
                        messageParcel.readInt();
                        messageParcel.readInt();
                        messageParcel.readInt();
                        messageParcel.readInt();
                        messageParcel.readInt();
                        messageParcel.readInt();
                        arrayList.add(empty3.get());
                    }
                }
            }
            this.mBleCentralManager.groupScanResultsEvent(arrayList);
        } else if (i != 5) {
            HiLog.info(TAG, "this callback is not implemented", new Object[0]);
        } else {
            enforceInterface(messageParcel);
            this.mBleCentralManager.scanFailedEvent(messageParcel.readInt());
        }
        return ScannerCallbackWrapper.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
    }

    private void parseRecord(RecordSet recordSet, Map<Integer, byte[]> map, Map<UUID, byte[]> map2, List<UUID> list) {
        int i = recordSet.fieldType;
        int i2 = recordSet.dataLength;
        int i3 = recordSet.currentPos;
        byte[] bArr = recordSet.scanRecord;
        if (i != 22) {
            if (i == 255) {
                int i4 = i3 + 1;
                if (i4 >= bArr.length) {
                    HiLog.error(TAG, "parseRecord got manufacture data fail for data lenght error", new Object[0]);
                    return;
                }
                map.put(Integer.valueOf(((bArr[i4] & 255) << 8) + (255 & bArr[i3])), copyBytes(bArr, i3 + 2, i2 - 2));
                return;
            } else if (!(i == 32 || i == 33)) {
                switch (i) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        parseServiceUuid(bArr, i3, i, i2, list);
                        return;
                    default:
                        return;
                }
            }
        }
        parseServiceData(bArr, i3, i, i2, map2);
    }

    private Optional<BleScanResult> readFromBytes(byte[] bArr, BlePeripheralDevice blePeripheralDevice) {
        int i;
        if (bArr == null) {
            return Optional.empty();
        }
        int i2 = 0;
        ArrayList arrayList = new ArrayList();
        HashMap hashMap = new HashMap();
        HashMap hashMap2 = new HashMap();
        int i3 = -1;
        while (true) {
            int i4 = i2 + 1;
            if (i4 >= bArr.length || (i = bArr[i2] & 255) == 0) {
                break;
            }
            int i5 = i - 1;
            int i6 = i4 + 1;
            int i7 = bArr[i4] & 255;
            if (i7 == 1) {
                i3 = bArr[i6] & 255;
            } else {
                parseRecord(new RecordSet(i7, i6, i5, bArr), hashMap, hashMap2, arrayList);
            }
            i2 = i5 + i6;
        }
        return Optional.ofNullable(new BleScanResult(blePeripheralDevice, hashMap, hashMap2, arrayList, i3, -1));
    }

    private void parseServiceData(byte[] bArr, int i, int i2, int i3, Map<UUID, byte[]> map) {
        if (bArr == null) {
            HiLog.warn(TAG, "bytes error when parsing service data", new Object[0]);
            return;
        }
        int i4 = 2;
        if (i2 == 32) {
            i4 = 4;
        } else if (i2 == 33) {
            i4 = 16;
        } else {
            HiLog.info(TAG, "no need parse", new Object[0]);
        }
        map.put(parseUuidFrom(copyBytes(bArr, i, i4)), copyBytes(bArr, i + i4, i3 - i4));
    }

    private void parseServiceUuid(byte[] bArr, int i, int i2, int i3, List<UUID> list) {
        int i4 = 4;
        if (i2 == 2 || i2 == 3) {
            i4 = 2;
        } else if (!(i2 == 4 || i2 == 5)) {
            if (i2 == 6 || i2 == 7) {
                i4 = 16;
            } else {
                HiLog.warn(TAG, "type error when parsing uuids", new Object[0]);
                return;
            }
        }
        while (i3 > 0) {
            list.add(parseUuidFrom(copyBytes(bArr, i, i4)));
            i3 -= i4;
            i += i4;
        }
    }

    private byte[] copyBytes(byte[] bArr, int i, int i2) {
        byte[] bArr2 = new byte[i2];
        System.arraycopy(bArr, i, bArr2, 0, i2);
        return bArr2;
    }

    private UUID parseUuidFrom(byte[] bArr) {
        long j;
        if (bArr != null) {
            int length = bArr.length;
            if (length != 2 && length != 4 && length != 16) {
                throw new IllegalArgumentException("uuidBytes length invalid - " + length);
            } else if (length == 16) {
                ByteBuffer order = ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN);
                return new UUID(order.getLong(8), order.getLong(0));
            } else {
                if (length == 2) {
                    j = ((long) (bArr[0] & 255)) + ((long) ((bArr[1] & 255) << 8));
                } else {
                    j = ((long) (bArr[0] & 255)) + ((long) ((bArr[1] & 255) << 8)) + ((long) ((bArr[2] & 255) << 16)) + ((long) ((bArr[3] & 255) << 24));
                }
                return new UUID(UUID.fromString(BASE_UUID).getMostSignificantBits() + (j << 32), UUID.fromString(BASE_UUID).getLeastSignificantBits());
            }
        } else {
            throw new IllegalArgumentException("uuidBytes cannot be null");
        }
    }

    private Optional<BlePeripheralDevice> createDeviceFromParcel(MessageParcel messageParcel) {
        if (messageParcel != null) {
            return Optional.ofNullable(new BlePeripheralDevice(messageParcel.readString()));
        }
        return Optional.empty();
    }

    /* access modifiers changed from: package-private */
    public static final class RecordSet {
        int currentPos;
        int dataLength;
        int fieldType;
        byte[] scanRecord;

        RecordSet(int i, int i2, int i3, byte[] bArr) {
            this.fieldType = i;
            this.currentPos = i2;
            this.dataLength = i3;
            this.scanRecord = bArr;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ScanParameter {
        private int mMatchMode = 1;
        private int mScanMode = 0;
        private long mTime = 0;

        ScanParameter(int i, int i2, long j) {
            this.mScanMode = i;
            this.mMatchMode = i2;
            this.mTime = j;
        }

        /* access modifiers changed from: package-private */
        public void writeToParcel(MessageParcel messageParcel) {
            messageParcel.writeInt(1);
            messageParcel.writeInt(this.mScanMode);
            messageParcel.writeInt(1);
            messageParcel.writeInt(0);
            messageParcel.writeLong(this.mTime);
            messageParcel.writeInt(this.mMatchMode);
            messageParcel.writeInt(3);
            messageParcel.writeInt(1);
            messageParcel.writeInt(1);
            messageParcel.writeInt(0);
            messageParcel.writeInt(0);
            messageParcel.writeInt(0);
            messageParcel.writeInt(0);
            messageParcel.writeInt(0);
            messageParcel.writeInt(0);
            messageParcel.writeInt(0);
            messageParcel.writeInt(0);
        }
    }
}
