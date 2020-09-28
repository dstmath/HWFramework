package android.bluetooth;

import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BluetoothAdapterEx {
    private static final String TAG = "BluetoothAdapterEx";

    public static int startLeRanging(int code, Parcel data, Parcel reply) {
        int ret = -1;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "startLeRanging adapter is null!");
            return -1;
        }
        try {
            IBluetooth service = adapter.getBluetoothService(null);
            if (service != null) {
                service.asBinder().transact(code, data, reply, 0);
                reply.readException();
                ret = reply.readInt();
            } else {
                Log.e(TAG, "Cannot startLeRanging!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "startLeRanging exception: " + e.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return ret;
    }

    public static int stopLeRanging(int code, Parcel data, Parcel reply) {
        int ret = -1;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "stopLeRanging adapter is null!");
            return -1;
        }
        try {
            IBluetooth service = adapter.getBluetoothService(null);
            if (service != null) {
                service.asBinder().transact(code, data, reply, 0);
                reply.readException();
                ret = reply.readInt();
            } else {
                Log.e(TAG, "Cannot stopLeRanging!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "stopLeRanging exception: " + e.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return ret;
    }

    public static boolean feedRssi(int code, Parcel data, Parcel reply) {
        boolean isReadOk = false;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "feedRssi adapter is null!");
            return false;
        }
        try {
            IBluetooth service = adapter.getBluetoothService(null);
            if (service != null) {
                boolean z = false;
                service.asBinder().transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 0) {
                    z = true;
                }
                isReadOk = z;
            } else {
                Log.e(TAG, "Cannot feedRssi!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "feedRssi exception: " + e.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isReadOk;
    }

    public static int getRemainFilterNum() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "getRemainFilterNum adapter is null!");
            return -1;
        }
        try {
            return adapter.getBluetoothService(null).getRemainFilterNum();
        } catch (RemoteException e) {
            Log.e(TAG, "getRemainFilterNum fail");
            return -1;
        }
    }
}
