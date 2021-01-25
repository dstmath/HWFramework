package android.bluetooth;

import android.os.ParcelUuid;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BluetoothUuidAdapterEx {
    public static boolean isUuidPresent(ParcelUuid[] uuidArray, ParcelUuid uuid) {
        return BluetoothUuid.isUuidPresent(uuidArray, uuid);
    }

    public static final ParcelUuid getObexObjectPush() {
        return BluetoothUuid.ObexObjectPush;
    }
}
