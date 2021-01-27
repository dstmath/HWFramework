package android.bluetooth;

import java.util.List;

public interface IBluetoothAdvCallback {
    void onDeviceInfoReport(List<BluetoothAdvDeviceInfo> list);
}
