package android.bluetooth;

import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BluetoothDeviceAdapterEx {
    private static final int CODE_GET_HW_BATTERY_INFO = 1004;
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    private static final String HW_BATTERY_INFO_ERROR = "-2";
    private static final String HW_BATTERY_INFO_UNKNOWN = "-1";
    private static final String TAG = "BluetoothDeviceAdapterEx";
    private static final int TWS_WEAR_DETECT_SUPPORT_SYSTEM_ERROR = -2;
    private static final int TWS_WEAR_STATE_FAIL_SYSTEM_ERROR = -2;

    public static void transactData(BluetoothDevice device, int code, Parcel data, Parcel reply, String msg) {
        try {
            IBluetooth sService = BluetoothDevice.getService();
            if (sService != null) {
                sService.asBinder().transact(code, data, reply, 0);
                reply.readException();
            } else {
                Log.e(TAG, msg);
            }
        } catch (RemoteException localRemoteException) {
            Log.e(TAG, "transact exception: " + localRemoteException.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    public static boolean readRssiAdapter(BluetoothDevice device, int code, Parcel data, Parcel reply) {
        boolean isReadOk = false;
        try {
            IBluetooth sService = BluetoothDevice.getService();
            if (sService != null) {
                boolean z = false;
                sService.asBinder().transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 0) {
                    z = true;
                }
                isReadOk = z;
            } else {
                Log.e(TAG, "Cannot readRssi!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "readRssiAdapter exception: " + e.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isReadOk;
    }

    public static void setWearDetectionSupport(BluetoothDevice device, int support) {
        IBluetooth service = BluetoothDevice.getService();
        if (service == null) {
            Log.d(TAG, "setTwsWearDetectionSupport got null service");
            return;
        }
        try {
            service.setWearDetectionSupport(device, support);
        } catch (RemoteException e) {
            Log.e(TAG, "setTwsWearDetectionSupport got RemoteException: " + e.getMessage());
        }
    }

    public static int getTwsWearDetectionSupport(BluetoothDevice device) {
        IBluetooth service = BluetoothDevice.getService();
        if (service == null) {
            Log.d(TAG, "getTwsWearDetectionSupport got null service");
            return -2;
        }
        try {
            return service.getWearDetectionSupport(device);
        } catch (RemoteException e) {
            Log.d(TAG, "getTwsWearDetectionSupport got RemoteException: " + e.getMessage());
            return -2;
        }
    }

    public static int getTwsWearState(BluetoothDevice device, int side) {
        IBluetooth service = BluetoothDevice.getService();
        if (service == null) {
            Log.d(TAG, "getTwsWearState got null service");
            return -2;
        }
        try {
            return service.getWearState(device, side);
        } catch (RemoteException e) {
            Log.e(TAG, "getTwsWearState got RemoteException: " + e.getMessage());
            return -2;
        }
    }

    public static boolean isEncrypted(BluetoothDevice bluetoothDevice) {
        return bluetoothDevice.isEncrypted();
    }

    public static boolean isConnected(BluetoothDevice bluetoothDevice) {
        return bluetoothDevice.isConnected();
    }

    public static String getHwBatteryInfo(BluetoothDevice device) {
        String hwBatteryInfoStr;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (device == null) {
            try {
                Log.d(TAG, "getHwBatteryInfo got null device");
                reply.recycle();
                data.recycle();
                return HW_BATTERY_INFO_ERROR;
            } catch (RemoteException e) {
                Log.e(TAG, "getHwBatteryInfo got error");
                hwBatteryInfoStr = HW_BATTERY_INFO_ERROR;
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        } else {
            IBluetooth service = BluetoothDevice.getService();
            if (service == null) {
                Log.d(TAG, "getHwBatteryInfo got null service");
                reply.recycle();
                data.recycle();
                return HW_BATTERY_INFO_ERROR;
            }
            device.writeToParcel(data, 0);
            if (service.asBinder().transact(1004, data, reply, 0)) {
                reply.readException();
                hwBatteryInfoStr = reply.readString();
            } else {
                Log.e(TAG, "getHwBatteryInfo got transact error");
                hwBatteryInfoStr = HW_BATTERY_INFO_ERROR;
            }
            reply.recycle();
            data.recycle();
            return hwBatteryInfoStr;
        }
    }
}
