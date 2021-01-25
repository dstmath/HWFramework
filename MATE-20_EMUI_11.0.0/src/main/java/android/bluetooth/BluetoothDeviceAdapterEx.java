package android.bluetooth;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.bluetooth.HwBindDevice;
import com.huawei.android.bluetooth.HwFindDevice;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;

@HwSystemApi
public class BluetoothDeviceAdapterEx {
    private static final int CODE_CONTROL_DEVICE_ACTION = 1016;
    private static final int CODE_GET_BIND_DEVICE_LIST = 1013;
    private static final int CODE_GET_HW_BATTERY_INFO = 1004;
    private static final int CODE_GET_HW_REMOTE_DEVICE_INFO = 1011;
    private static final int CODE_SEND_FIND_DEVICE_LIST = 1014;
    private static final int CODE_START_SEARCH = 1015;
    private static final int CODE_UPDATE_HW_REMOTE_DEVICE_INFO = 1012;
    private static final int DEFAULT_CAPACITY = 1;
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    private static final String HW_BATTERY_INFO_ERROR = "-2";
    private static final String HW_BATTERY_INFO_UNKNOWN = "-1";
    private static final String HW_REMOTE_DEVICE_INFO_STR_ERROR = "";
    private static final String HW_REMOTE_DEVICE_INFO_STR_UNKNOWN = "-1";
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

    public static String getDeviceInfo(BluetoothDevice device, int type) {
        String hwRemoteDeviceInfoStr;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (device == null) {
            try {
                Log.d(TAG, "getDeviceInfo got null device");
                reply.recycle();
                data.recycle();
                return "";
            } catch (RemoteException e) {
                Log.e(TAG, "getDeviceInfo got error");
                hwRemoteDeviceInfoStr = "";
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        } else {
            IBluetooth service = BluetoothDevice.getService();
            if (service == null) {
                Log.d(TAG, "getDeviceInfo got null service");
                reply.recycle();
                data.recycle();
                return "";
            }
            data.writeInt(type);
            device.writeToParcel(data, 0);
            if (service.asBinder().transact(1011, data, reply, 0)) {
                reply.readException();
                hwRemoteDeviceInfoStr = reply.readString();
            } else {
                Log.e(TAG, "getDeviceInfo got transact error");
                hwRemoteDeviceInfoStr = "";
            }
            reply.recycle();
            data.recycle();
            return hwRemoteDeviceInfoStr;
        }
    }

    public static boolean updateDeviceInfoByType(BluetoothDevice device, int type, String value) {
        boolean ret = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = false;
        if (device == null) {
            try {
                Log.d(TAG, "updateDeviceInfoByType got null device");
                reply.recycle();
                data.recycle();
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "updateDeviceInfoByType got error");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        } else {
            IBluetooth service = BluetoothDevice.getService();
            if (service == null) {
                Log.d(TAG, "updateDeviceInfoByType got null service");
                reply.recycle();
                data.recycle();
                return false;
            }
            IBinder binder = service.asBinder();
            data.writeInt(type);
            data.writeString(value);
            device.writeToParcel(data, 0);
            if (binder.transact(1012, data, reply, 0)) {
                reply.readException();
                if (reply.readInt() != 0) {
                    z = true;
                }
                ret = z;
            } else {
                Log.e(TAG, "updateDeviceInfoByType got transact error");
            }
            reply.recycle();
            data.recycle();
            return ret;
        }
    }

    public static ArrayList<HwBindDevice> getBindDeviceList() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<HwBindDevice> hwBindDeviceList = new ArrayList<>(1);
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                Log.e(TAG, "Cannot getBindDeviceList, adapter is null!");
                reply.recycle();
                data.recycle();
                return hwBindDeviceList;
            }
            IBluetooth service = adapter.getBluetoothService(null);
            if (service == null) {
                Log.d(TAG, "getBindDeviceList got null service");
                reply.recycle();
                data.recycle();
                return hwBindDeviceList;
            }
            if (service.asBinder().transact(1013, data, reply, 0)) {
                reply.readException();
                hwBindDeviceList = reply.readArrayList(Integer.class.getClassLoader());
            } else {
                Log.e(TAG, "getBindDeviceList got transact error");
            }
            reply.recycle();
            data.recycle();
            return hwBindDeviceList;
        } catch (RemoteException e) {
            Log.e(TAG, "getBindDeviceList got error");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static void sendFindDevList(ArrayList<HwFindDevice> devList) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                Log.e(TAG, "Cannot sendFindDevList, adapter is null!");
                reply.recycle();
                data.recycle();
            } else if (devList == null) {
                Log.d(TAG, "sendFindDevList is null");
                reply.recycle();
                data.recycle();
            } else {
                IBluetooth service = adapter.getBluetoothService(null);
                if (service == null) {
                    Log.d(TAG, "sendFindDevList got null service");
                    reply.recycle();
                    data.recycle();
                    return;
                }
                IBinder binder = service.asBinder();
                data.writeList(devList);
                if (binder.transact(1014, data, reply, 0)) {
                    reply.readException();
                } else {
                    Log.e(TAG, "sendFindDevList got transact error");
                }
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "sendFindDevList got error");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static void startSearch(String deviceId, long searchTime, long reportInterval, JSONObject threshold) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                Log.e(TAG, "Cannot startSearch, adapter is null!");
                reply.recycle();
                data.recycle();
            } else if (deviceId == null) {
                Log.d(TAG, "startSearch: device is null");
                reply.recycle();
                data.recycle();
            } else {
                IBluetooth service = adapter.getBluetoothService(null);
                if (service == null) {
                    Log.d(TAG, "startSearch got null service");
                    reply.recycle();
                    data.recycle();
                    return;
                }
                IBinder binder = service.asBinder();
                data.writeString(deviceId);
                data.writeLong(searchTime);
                data.writeLong(reportInterval);
                data.writeString(threshold.toString());
                if (binder.transact(1015, data, reply, 0)) {
                    reply.readException();
                } else {
                    Log.e(TAG, "startSearch got transact error");
                }
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "startSearch got error");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }

    public static void controlDevAction(String deviceId, long controlTime, HashMap<Integer, Integer> controlType, JSONObject deviceObject) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                Log.e(TAG, "Cannot controlDevAction, adapter is null!");
                reply.recycle();
                data.recycle();
            } else if (deviceId == null) {
                Log.d(TAG, "controlDevAction: device is null");
                reply.recycle();
                data.recycle();
            } else {
                IBluetooth service = adapter.getBluetoothService(null);
                if (service == null) {
                    Log.d(TAG, "controlDevAction got null service");
                    reply.recycle();
                    data.recycle();
                    return;
                }
                IBinder binder = service.asBinder();
                data.writeString(deviceId);
                data.writeLong(controlTime);
                data.writeMap(controlType);
                data.writeString(deviceObject.toString());
                if (binder.transact(1016, data, reply, 0)) {
                    reply.readException();
                } else {
                    Log.e(TAG, "controlDevAction got transact error");
                }
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "controlDevAction got error");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }
}
