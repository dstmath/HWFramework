package android.bluetooth;

import android.bluetooth.IBluetoothAdvFilterCallbackEx;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothAdvFilterCallbackEx {
    private static final int CODE_SET_ADVERTIS_FILTER_CALLBACK = 2104;
    private static final int CODE_UPDATE_ADVERTIS_ACTIVE_DEVICE = 2105;
    private static final String TAG = "BluetoothAdvFilterCallbackEx";
    private static UUID sCallbackKey = UUID.randomUUID();
    private static BluetoothAdvFilterCallbackEx sInstance = new BluetoothAdvFilterCallbackEx();

    public static BluetoothAdvFilterCallbackEx getInstance() {
        return sInstance;
    }

    public boolean registAdvFilterCallback(UUID uuid, IBluetoothAdvCallback filterCallback) {
        return new BluetoothAdvFilterCallbackWrapper().registAdvFilterCallback(uuid, filterCallback);
    }

    public static class BluetoothAdvFilterCallbackBinder extends Binder {
        private BluetoothAdvFilterCallbackWrapper mCallbackWrapper;

        BluetoothAdvFilterCallbackBinder(BluetoothAdvFilterCallbackWrapper callbackWrapper) {
            this.mCallbackWrapper = callbackWrapper;
        }

        /* access modifiers changed from: protected */
        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != BluetoothAdvFilterCallbackEx.CODE_UPDATE_ADVERTIS_ACTIVE_DEVICE) {
                return super.onTransact(code, data, reply, flags);
            }
            int filterIdx = data.readInt();
            Log.i(BluetoothAdvFilterCallbackEx.TAG, "onTransact filter index:" + filterIdx);
            List<BluetoothAdvDeviceInfo> deviceInfoList = new ArrayList<>();
            int itemNum = data.readInt();
            for (int i = 0; i < itemNum; i++) {
                byte[] devId = new byte[8];
                data.readByteArray(devId);
                deviceInfoList.add(new BluetoothAdvDeviceInfo(devId, (short) data.readInt(), data.readInt()));
            }
            this.mCallbackWrapper.onDeviceInfoReport(deviceInfoList);
            reply.writeNoException();
            return true;
        }
    }

    private class BluetoothAdvFilterCallbackWrapper extends IBluetoothAdvFilterCallbackEx.Stub {
        private IBluetoothAdvCallback mFilterCallback;

        private BluetoothAdvFilterCallbackWrapper() {
        }

        public boolean registAdvFilterCallback(UUID uuid, IBluetoothAdvCallback filterCallback) {
            Log.i(BluetoothAdvFilterCallbackEx.TAG, "registAdvFilterCallback: " + uuid);
            this.mFilterCallback = filterCallback;
            return registAdvertisFilterCallback(uuid, new BluetoothAdvFilterCallbackBinder(this));
        }

        public void onDeviceInfoReport(List<BluetoothAdvDeviceInfo> devInfo) {
            IBluetoothAdvCallback iBluetoothAdvCallback = this.mFilterCallback;
            if (iBluetoothAdvCallback != null) {
                iBluetoothAdvCallback.onDeviceInfoReport(devInfo);
                Log.i(BluetoothAdvFilterCallbackEx.TAG, "onDeviceInfoReport filter");
                return;
            }
            Log.e(BluetoothAdvFilterCallbackEx.TAG, "callback is null");
        }

        public boolean registAdvertisFilterCallback(UUID uuid, BluetoothAdvFilterCallbackBinder callbackBinder) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean retVal = true;
            try {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) {
                    Log.e(BluetoothAdvFilterCallbackEx.TAG, "Cannot registAdvertisFilterCallback, adapter is null!");
                    reply.recycle();
                    data.recycle();
                    return false;
                }
                IBluetooth service = adapter.getBluetoothService(null);
                if (service == null) {
                    Log.e(BluetoothAdvFilterCallbackEx.TAG, "registAdvertisFilterCallback got null service");
                    reply.recycle();
                    data.recycle();
                    return false;
                }
                data.writeLong(BluetoothAdvFilterCallbackEx.sCallbackKey.getMostSignificantBits());
                data.writeLong(BluetoothAdvFilterCallbackEx.sCallbackKey.getLeastSignificantBits());
                data.writeLong(uuid.getMostSignificantBits());
                data.writeLong(uuid.getLeastSignificantBits());
                data.writeStrongBinder(callbackBinder);
                if (service.asBinder().transact(BluetoothAdvFilterCallbackEx.CODE_SET_ADVERTIS_FILTER_CALLBACK, data, reply, 0)) {
                    reply.readException();
                    if (reply.readInt() == 0) {
                        Log.e(BluetoothAdvFilterCallbackEx.TAG, "registAdvertisFilterCallback transact reply error");
                        retVal = false;
                    }
                    Log.e(BluetoothAdvFilterCallbackEx.TAG, "registAdvertisFilterCallback transact reply success");
                } else {
                    Log.e(BluetoothAdvFilterCallbackEx.TAG, "registAdvertisFilterCallback got transact error");
                }
                reply.recycle();
                data.recycle();
                return retVal;
            } catch (RemoteException e) {
                Log.e(BluetoothAdvFilterCallbackEx.TAG, "registAdvertisFilterCallback got error");
                retVal = false;
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
    }
}
