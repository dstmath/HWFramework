package android.bluetooth;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothManager {
    private static final boolean DBG = true;
    private static final String TAG = "BluetoothManager";
    private static final boolean VDBG = true;
    private final BluetoothAdapter mAdapter;

    public BluetoothManager(Context context) {
        if (context.getApplicationContext() != null) {
            this.mAdapter = BluetoothAdapter.getDefaultAdapter();
            return;
        }
        throw new IllegalArgumentException("context not associated with any application (using a mock context?)");
    }

    public BluetoothAdapter getAdapter() {
        return this.mAdapter;
    }

    public int getConnectionState(BluetoothDevice device, int profile) {
        Log.d(TAG, "getConnectionState()");
        for (BluetoothDevice connectedDevice : getConnectedDevices(profile)) {
            if (device.equals(connectedDevice)) {
                return 2;
            }
        }
        return 0;
    }

    public List<BluetoothDevice> getConnectedDevices(int profile) {
        Log.d(TAG, "getConnectedDevices");
        if (profile == 7 || profile == 8) {
            List<BluetoothDevice> connectedDevices = new ArrayList<>();
            try {
                IBluetoothGatt iGatt = this.mAdapter.getBluetoothManager().getBluetoothGatt();
                if (iGatt == null) {
                    return connectedDevices;
                }
                connectedDevices = iGatt.getDevicesMatchingConnectionStates(new int[]{2});
                return connectedDevices;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        } else {
            throw new IllegalArgumentException("Profile not supported: " + profile);
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int profile, int[] states) {
        Log.d(TAG, "getDevicesMatchingConnectionStates");
        if (profile == 7 || profile == 8) {
            List<BluetoothDevice> devices = new ArrayList<>();
            try {
                IBluetoothGatt iGatt = this.mAdapter.getBluetoothManager().getBluetoothGatt();
                if (iGatt == null) {
                    return devices;
                }
                devices = iGatt.getDevicesMatchingConnectionStates(states);
                return devices;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        } else {
            throw new IllegalArgumentException("Profile not supported: " + profile);
        }
    }

    public BluetoothGattServer openGattServer(Context context, BluetoothGattServerCallback callback) {
        return openGattServer(context, callback, 0);
    }

    public BluetoothGattServer openGattServer(Context context, BluetoothGattServerCallback callback, int transport) {
        if (context == null || callback == null) {
            throw new IllegalArgumentException("null parameter: " + context + " " + callback);
        }
        Log.i(TAG, "openGattServer");
        BluetoothGattServer bluetoothGattServer = null;
        try {
            IBluetoothGatt iGatt = this.mAdapter.getBluetoothManager().getBluetoothGatt();
            if (iGatt == null) {
                Log.e(TAG, "Fail to get GATT Server connection");
                return null;
            }
            BluetoothGattServer mGattServer = new BluetoothGattServer(iGatt, transport);
            if (Boolean.valueOf(mGattServer.registerCallback(callback)).booleanValue()) {
                bluetoothGattServer = mGattServer;
            }
            return bluetoothGattServer;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }
}
