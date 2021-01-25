package android.bluetooth;

import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class LeRangeFeeding {
    private static final int CODE_FEED_RSSI = 1007;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetooth";
    private static final String TAG = "LeRangeFeeding";
    public String mUuid;

    public LeRangeFeeding(String uuid) {
        this.mUuid = uuid;
    }

    private boolean transact(int code, Parcel data, Parcel reply) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        boolean z = false;
        if (adapter == null) {
            Log.e(TAG, "adapter is null!");
            return false;
        }
        boolean isReadOk = false;
        try {
            IBluetooth service = adapter.getBluetoothService(null);
            if (service != null) {
                service.asBinder().transact(code, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 0) {
                    z = true;
                }
                isReadOk = z;
            } else {
                Log.e(TAG, "Cannot feedrssi!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "feedrssi exception: " + e.getMessage());
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isReadOk;
    }

    public String getUuid() {
        Log.d(TAG, "getUuid");
        return this.mUuid;
    }

    public void feedRssi(ScanResult result) {
        Log.d(TAG, "feedRssi");
        if (result == null) {
            Log.e(TAG, "feedRssi result is null");
            return;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(DESCRIPTOR);
        result.writeToParcel(data, 0);
        data.writeString(this.mUuid);
        if (!transact(1007, data, reply)) {
            Log.e(TAG, "feedRssi error");
        }
    }
}
