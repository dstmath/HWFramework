package android.bluetooth;

import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothAvrcpController;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothAvrcpController implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.avrcp-controller.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_PLAYER_SETTING = "android.bluetooth.avrcp-controller.profile.action.PLAYER_SETTING";
    private static final boolean DBG = false;
    public static final String EXTRA_PLAYER_SETTING = "android.bluetooth.avrcp-controller.profile.extra.PLAYER_SETTING";
    private static final String TAG = "BluetoothAvrcpController";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BluetoothProfileConnector<IBluetoothAvrcpController> mProfileConnector = new BluetoothProfileConnector(this, 12, TAG, IBluetoothAvrcpController.class.getName()) {
        /* class android.bluetooth.BluetoothAvrcpController.AnonymousClass1 */

        @Override // android.bluetooth.BluetoothProfileConnector
        public IBluetoothAvrcpController getServiceInterface(IBinder service) {
            return IBluetoothAvrcpController.Stub.asInterface(Binder.allowBlocking(service));
        }
    };

    BluetoothAvrcpController(Context context, BluetoothProfile.ServiceListener listener) {
        this.mProfileConnector.connect(context, listener);
    }

    /* access modifiers changed from: package-private */
    public void close() {
        this.mProfileConnector.disconnect();
    }

    private IBluetoothAvrcpController getService() {
        return this.mProfileConnector.getService();
    }

    public void finalize() {
        close();
    }

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getConnectedDevices() {
        IBluetoothAvrcpController service = getService();
        if (service == null || !isEnabled()) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return service.getConnectedDevices();
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        IBluetoothAvrcpController service = getService();
        if (service == null || !isEnabled()) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return service.getDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    @Override // android.bluetooth.BluetoothProfile
    public int getConnectionState(BluetoothDevice device) {
        IBluetoothAvrcpController service = getService();
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return 0;
        }
        try {
            return service.getConnectionState(device);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return 0;
        }
    }

    public BluetoothAvrcpPlayerSettings getPlayerSettings(BluetoothDevice device) {
        IBluetoothAvrcpController service = getService();
        if (service == null || !isEnabled()) {
            return null;
        }
        try {
            return service.getPlayerSettings(device);
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in getMetadata() " + e);
            return null;
        }
    }

    public boolean setPlayerApplicationSetting(BluetoothAvrcpPlayerSettings plAppSetting) {
        IBluetoothAvrcpController service = getService();
        if (service == null || !isEnabled()) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.setPlayerApplicationSetting(plAppSetting);
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in setPlayerApplicationSetting() " + e);
            return false;
        }
    }

    public void sendGroupNavigationCmd(BluetoothDevice device, int keyCode, int keyState) {
        Log.d(TAG, "sendGroupNavigationCmd dev = " + device + " key " + keyCode + " State = " + keyState);
        IBluetoothAvrcpController service = getService();
        if (service != null && isEnabled()) {
            try {
                service.sendGroupNavigationCmd(device, keyCode, keyState);
            } catch (RemoteException e) {
                Log.e(TAG, "Error talking to BT service in sendGroupNavigationCmd()", e);
            }
        } else if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
    }

    private boolean isEnabled() {
        return this.mAdapter.getState() == 12;
    }

    private static boolean isValidDevice(BluetoothDevice device) {
        return device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress());
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
