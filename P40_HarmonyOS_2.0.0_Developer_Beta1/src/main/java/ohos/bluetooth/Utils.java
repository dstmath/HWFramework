package ohos.bluetooth;

import java.util.ArrayList;
import ohos.rpc.MessageParcel;

public class Utils {
    static final String HOST_INTERFACE_TOKEN = "ohos.bluetooth.IBluetoothHost";
    static final int PERMISSION_CHECK_FAILED = 3756;
    static final int REMOTE_OPERATION_OK = 1;

    public static ArrayList<BluetoothRemoteDevice> createDeviceList(MessageParcel messageParcel, int i) {
        ArrayList<BluetoothRemoteDevice> arrayList;
        int readInt = messageParcel.readInt();
        if (readInt < 0) {
            arrayList = new ArrayList<>();
        } else {
            arrayList = new ArrayList<>(i);
        }
        for (int i2 = 0; i2 < readInt && messageParcel.getReadableBytes() > 0; i2++) {
            BluetoothRemoteDevice bluetoothRemoteDevice = new BluetoothRemoteDevice();
            messageParcel.readSequenceable(bluetoothRemoteDevice);
            arrayList.add(bluetoothRemoteDevice);
        }
        return arrayList;
    }
}
