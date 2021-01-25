package huawei.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDeviceAdapterEx;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class HwBluetoothDeviceEx {
    private static final int CODE_ADD_NFC_PAIRING_WHITE_LIST = 1001;
    private static final int CODE_CLEAR_NFC_PAIRING_WHITE_LIST = 1003;
    private static final int CODE_READ_RSSI = 2001;
    private static final int CODE_READ_RSSI_CALLBACK = 2002;
    private static final int CODE_REMOVE_NFC_PAIRING_WHITE_LIST = 1002;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetooth";
    private static final String TAG = "HwBluetoothDeviceEx";
    private static HwBluetoothDeviceEx mInstance = new HwBluetoothDeviceEx();

    public interface IReadRssiCallback {
        void onReadRssi(int i, int i2, BluetoothDevice bluetoothDevice);
    }

    public static HwBluetoothDeviceEx getDefault() {
        return mInstance;
    }

    public void addNfcPairingWhiteList(BluetoothDevice device, String address) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(DESCRIPTOR);
        data.writeString(address);
        BluetoothDeviceAdapterEx.transactData(device, (int) CODE_ADD_NFC_PAIRING_WHITE_LIST, data, reply, "BT not enabled. Cannot addNfcPairingWhiteList to Remote Device");
    }

    public void removeNfcPairingWhiteList(BluetoothDevice device, String deviceAddress) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(DESCRIPTOR);
        data.writeString(deviceAddress);
        BluetoothDeviceAdapterEx.transactData(device, (int) CODE_REMOVE_NFC_PAIRING_WHITE_LIST, data, reply, "BT not enabled. Cannot removeNfcPairingWhiteList to Remote Device");
    }

    public void clearNfcPairingWhiteList(BluetoothDevice device) {
        Log.d(TAG, "clearNfcPairingWhiteList");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(DESCRIPTOR);
        BluetoothDeviceAdapterEx.transactData(device, (int) CODE_CLEAR_NFC_PAIRING_WHITE_LIST, data, reply, "BT not enabled. Cannot clearNfcPairingWhiteList to Remote Device");
    }

    public boolean readRssi(BluetoothDevice device, IReadRssiCallback callback) {
        Log.d(TAG, "readRssi");
        if (device == null || callback == null) {
            Log.e(TAG, "Invalid args in readRssi(): device = " + getPartAddress(device) + "callback: " + callback);
            return false;
        }
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(DESCRIPTOR);
        data.writeInt(1);
        device.writeToParcel(data, 0);
        data.writeStrongBinder(new ReadRssiCallbackBinder(callback));
        return BluetoothDeviceAdapterEx.readRssiAdapter(device, (int) CODE_READ_RSSI, data, Parcel.obtain());
    }

    private static class ReadRssiCallbackBinder extends Binder {
        private IReadRssiCallback mCallback;

        ReadRssiCallbackBinder(IReadRssiCallback callback) {
            this.mCallback = callback;
        }

        /* access modifiers changed from: protected */
        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BluetoothDevice device;
            if (code != HwBluetoothDeviceEx.CODE_READ_RSSI_CALLBACK) {
                return super.onTransact(code, data, reply, flags);
            }
            int status = data.readInt();
            int rssi = data.readInt();
            if (data.readInt() != 0) {
                device = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
            } else {
                device = null;
            }
            Log.d(HwBluetoothDeviceEx.TAG, "ReadRssiCallback: status = " + status + " rssi = " + rssi + " device = " + HwBluetoothDeviceEx.getPartAddress(device) + " mCallback = " + this.mCallback);
            if (this.mCallback != null) {
                this.mCallback.onReadRssi(status, rssi, device);
            }
            reply.writeNoException();
            reply.writeInt(1);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static String getPartAddress(BluetoothDevice device) {
        String address;
        if (device == null || (address = device.getAddress()) == null) {
            return "";
        }
        return address.substring(0, address.length() / 2) + ":**:**:**";
    }

    public static HwDeviceBatteryInfo getHwDeviceBatteryInfo(BluetoothDevice device) {
        if (device != null) {
            return new HwDeviceBatteryInfo(BluetoothDeviceAdapterEx.getHwBatteryInfo(device));
        }
        Log.d(TAG, "getHwDeviceBatteryInfo got null device");
        return new HwDeviceBatteryInfo(HwDeviceBatteryInfo.HW_BATTERY_INFO_STR_UNKNOWN);
    }
}
