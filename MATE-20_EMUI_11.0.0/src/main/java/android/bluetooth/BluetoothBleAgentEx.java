package android.bluetooth;

import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BluetoothBleAgentEx {
    private static final String TAG = "BluetoothBleAgentEx";

    public static int updateBleAgentInfo(int code, Parcel data, Parcel reply) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        int ret = -1;
        if (adapter == null) {
            Log.e(TAG, "Cannot updateBleAgentInformation adapter is null!");
            return -1;
        }
        try {
            IBluetooth service = adapter.getBluetoothService(null);
            if (service != null) {
                service.asBinder().transact(code, data, reply, 0);
                reply.readException();
                ret = reply.readInt();
            } else {
                Log.e(TAG, "Cannot updateBleAgentInformation!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "updateBleAgentInformation fail");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return ret;
    }
}
