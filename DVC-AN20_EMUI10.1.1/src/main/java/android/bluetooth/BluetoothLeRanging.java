package android.bluetooth;

import android.bluetooth.ILeRangingCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BluetoothLeRanging {
    private static final int CODE_START_BLE_RANGE = 1005;
    private static final int CODE_STOP_BLE_RANGE = 1006;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetooth";
    public static final int RANGE_SUCC = 0;
    public static final int START_RANGE_BT_ERR = 1;
    public static final int STOP_RANGE_BT_ERR = 2;
    private static final String TAG = "BluetoothLeRanging";
    private final Map<String, LeRangingCallback> mCallbackMap;
    private final Map<String, LeRangingCallbackWrapper> mCallbackWrappers;
    private final Object mMapLock = new Object();
    private int mRangingType;

    public BluetoothLeRanging(int rangingType) {
        this.mRangingType = rangingType;
        this.mCallbackWrappers = new HashMap();
        this.mCallbackMap = new HashMap();
    }

    public void startLeRanging(LeRangingSettings settings, LeRangingCallback callback) {
        Log.d(TAG, "startLeRanging");
        if (callback == null) {
            Log.e(TAG, "startLeRanging callback is null");
        } else if (settings == null) {
            Log.e(TAG, "startLeRanging settings is null");
            callback.onStartFailure(1);
        } else {
            synchronized (this.mMapLock) {
                String id = UUID.randomUUID().toString();
                LeRangingCallbackWrapper wrapper = new LeRangingCallbackWrapper(callback);
                if (wrapper.startRange(settings, id) != 0) {
                    callback.onStartFailure(1);
                    return;
                }
                this.mCallbackWrappers.put(id, wrapper);
                LeRangeFeeding leRangeFeeding = new LeRangeFeeding(id);
                this.mCallbackMap.put(id, callback);
                Log.d(TAG, "startLeRanging uuid is " + leRangeFeeding.getUuid());
                callback.onStartSucess(leRangeFeeding);
            }
        }
    }

    public void stopLeRanging(String uuid) {
        Log.d(TAG, "stopLeRanging uuid is " + uuid);
        synchronized (this.mMapLock) {
            LeRangingCallback leRangingCallback = this.mCallbackMap.get(uuid);
            if (leRangingCallback == null) {
                Log.e(TAG, "stopLeRanging uuid is invalidble");
                return;
            }
            LeRangingCallbackWrapper wrapper = this.mCallbackWrappers.get(uuid);
            if (wrapper == null) {
                Log.d(TAG, "stopLeRanging wrapper is null");
                leRangingCallback.onStopFailure(2);
            } else if (wrapper.stopRange(uuid) == 0) {
                leRangingCallback.onStopSucess();
                this.mCallbackMap.remove(uuid);
                this.mCallbackWrappers.remove(uuid);
            } else {
                Log.e(TAG, "stopLeRanging error");
                leRangingCallback.onStopFailure(2);
            }
        }
    }

    private class LeRangingCallbackWrapper extends ILeRangingCallback.Stub {
        private final LeRangingCallback mLeRangingCallback;

        public LeRangingCallbackWrapper(LeRangingCallback callback) {
            this.mLeRangingCallback = callback;
        }

        private int transact(int code, Parcel data, Parcel reply) {
            int ret = -1;
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                Log.e(BluetoothLeRanging.TAG, "transact adapter is null");
                return -1;
            }
            try {
                IBluetooth service = adapter.getBluetoothService(null);
                if (service != null) {
                    service.asBinder().transact(code, data, reply, 0);
                    reply.readException();
                    ret = reply.readInt();
                } else {
                    Log.e(BluetoothLeRanging.TAG, "Cannot transact!");
                }
            } catch (RemoteException e) {
                Log.e(BluetoothLeRanging.TAG, "transact exception: " + e.getMessage());
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
            return ret;
        }

        public int startRange(LeRangingSettings settings, String id) {
            Log.d(BluetoothLeRanging.TAG, "startRange");
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(BluetoothLeRanging.DESCRIPTOR);
            settings.writeToParcel(data, 0);
            data.writeStrongBinder(asBinder());
            data.writeString(id);
            return transact(1005, data, reply);
        }

        public int stopRange(String id) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(BluetoothLeRanging.DESCRIPTOR);
            data.writeStrongBinder(asBinder());
            data.writeString(id);
            Log.d(BluetoothLeRanging.TAG, "stopRange");
            return transact(1006, data, reply);
        }

        @Override // android.bluetooth.ILeRangingCallback
        public void onRangeResult(final LeRangingResult rangingResult, final ScanResult scanResult) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                /* class android.bluetooth.BluetoothLeRanging.LeRangingCallbackWrapper.AnonymousClass1 */

                public void run() {
                    Log.d(BluetoothLeRanging.TAG, "onRangeResult");
                    LeRangingCallbackWrapper.this.mLeRangingCallback.onRangeResult(rangingResult, scanResult);
                }
            });
        }
    }
}
