package android.bluetooth;

public class BluetoothDeviceUtils {
    public static IBluetooth getService(BluetoothDevice device) {
        return BluetoothDevice.getService();
    }
}
