package android.bluetooth;

import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothMapClient;
import android.content.Context;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothMapClient implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.mapmce.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_MESSAGE_DELIVERED_SUCCESSFULLY = "android.bluetooth.mapmce.profile.action.MESSAGE_DELIVERED_SUCCESSFULLY";
    public static final String ACTION_MESSAGE_RECEIVED = "android.bluetooth.mapmce.profile.action.MESSAGE_RECEIVED";
    public static final String ACTION_MESSAGE_SENT_SUCCESSFULLY = "android.bluetooth.mapmce.profile.action.MESSAGE_SENT_SUCCESSFULLY";
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    public static final String EXTRA_MESSAGE_HANDLE = "android.bluetooth.mapmce.profile.extra.MESSAGE_HANDLE";
    public static final String EXTRA_MESSAGE_READ_STATUS = "android.bluetooth.mapmce.profile.extra.MESSAGE_READ_STATUS";
    public static final String EXTRA_MESSAGE_TIMESTAMP = "android.bluetooth.mapmce.profile.extra.MESSAGE_TIMESTAMP";
    public static final String EXTRA_SENDER_CONTACT_NAME = "android.bluetooth.mapmce.profile.extra.SENDER_CONTACT_NAME";
    public static final String EXTRA_SENDER_CONTACT_URI = "android.bluetooth.mapmce.profile.extra.SENDER_CONTACT_URI";
    public static final int RESULT_CANCELED = 2;
    public static final int RESULT_FAILURE = 0;
    public static final int RESULT_SUCCESS = 1;
    public static final int STATE_ERROR = -1;
    private static final String TAG = "BluetoothMapClient";
    private static final int UPLOADING_FEATURE_BITMASK = 8;
    private static final boolean VDBG = Log.isLoggable(TAG, 2);
    private BluetoothAdapter mAdapter;
    private final BluetoothProfileConnector<IBluetoothMapClient> mProfileConnector = new BluetoothProfileConnector(this, 18, TAG, IBluetoothMapClient.class.getName()) {
        /* class android.bluetooth.BluetoothMapClient.AnonymousClass1 */

        @Override // android.bluetooth.BluetoothProfileConnector
        public IBluetoothMapClient getServiceInterface(IBinder service) {
            return IBluetoothMapClient.Stub.asInterface(Binder.allowBlocking(service));
        }
    };

    BluetoothMapClient(Context context, BluetoothProfile.ServiceListener listener) {
        if (DBG) {
            Log.d(TAG, "Create BluetoothMapClient proxy object");
        }
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mProfileConnector.connect(context, listener);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public void close() {
        this.mProfileConnector.disconnect();
    }

    private IBluetoothMapClient getService() {
        return this.mProfileConnector.getService();
    }

    public boolean isConnected(BluetoothDevice device) {
        if (VDBG) {
            Log.d(TAG, "isConnected(" + getPartAddress(device) + ")");
        }
        IBluetoothMapClient service = getService();
        if (service != null) {
            try {
                return service.isConnected(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (!DBG) {
                return false;
            }
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean connect(BluetoothDevice device) {
        if (DBG) {
            Log.d(TAG, "connect(" + getPartAddress(device) + ")for MAPS MCE");
        }
        IBluetoothMapClient service = getService();
        if (service != null) {
            try {
                return service.connect(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (!DBG) {
                return false;
            }
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean disconnect(BluetoothDevice device) {
        if (DBG) {
            Log.d(TAG, "disconnect(" + getPartAddress(device) + ")");
        }
        IBluetoothMapClient service = getService();
        if (service != null && isEnabled() && isValidDevice(device)) {
            try {
                return service.disconnect(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service != null) {
            return false;
        }
        Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getConnectedDevices() {
        if (DBG) {
            Log.d(TAG, "getConnectedDevices()");
        }
        IBluetoothMapClient service = getService();
        if (service == null || !isEnabled()) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return service.getConnectedDevices();
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        if (DBG) {
            Log.d(TAG, "getDevicesMatchingStates()");
        }
        IBluetoothMapClient service = getService();
        if (service == null || !isEnabled()) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return service.getDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    @Override // android.bluetooth.BluetoothProfile
    public int getConnectionState(BluetoothDevice device) {
        if (DBG) {
            Log.d(TAG, "getConnectionState(" + getPartAddress(device) + ")");
        }
        IBluetoothMapClient service = getService();
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return 0;
        }
        try {
            return service.getConnectionState(device);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return 0;
        }
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        if (DBG) {
            Log.d(TAG, "setPriority(" + getPartAddress(device) + ", " + priority + ")");
        }
        IBluetoothMapClient service = getService();
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        } else if (priority != 0 && priority != 100) {
            return false;
        } else {
            try {
                return service.setPriority(device, priority);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
    }

    public int getPriority(BluetoothDevice device) {
        if (VDBG) {
            Log.d(TAG, "getPriority(" + getPartAddress(device) + ")");
        }
        IBluetoothMapClient service = getService();
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return 0;
        }
        try {
            return service.getPriority(device);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return 0;
        }
    }

    @UnsupportedAppUsage
    public boolean sendMessage(BluetoothDevice device, Uri[] contacts, String message, PendingIntent sentIntent, PendingIntent deliveredIntent) {
        if (DBG) {
            Log.d(TAG, "sendMessage(" + getPartAddress(device) + ", " + contacts + ", " + message);
        }
        IBluetoothMapClient service = getService();
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            return false;
        }
        try {
            return service.sendMessage(device, contacts, message, sentIntent, deliveredIntent);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean getUnreadMessages(BluetoothDevice device) {
        if (DBG) {
            Log.d(TAG, "getUnreadMessages(" + getPartAddress(device) + ")");
        }
        IBluetoothMapClient service = getService();
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            return false;
        }
        try {
            return service.getUnreadMessages(device);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean isUploadingSupported(BluetoothDevice device) {
        IBluetoothMapClient service = getService();
        if (service == null) {
            return false;
        }
        try {
            if (!isEnabled() || !isValidDevice(device) || (service.getSupportedFeatures(device) & 8) <= 0) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    private boolean isEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getState() == 12) {
            return true;
        }
        if (!DBG) {
            return false;
        }
        Log.d(TAG, "Bluetooth is Not enabled");
        return false;
    }

    private static boolean isValidDevice(BluetoothDevice device) {
        return device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress());
    }

    private static String getPartAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return "";
        }
        return address.substring(0, address.length() / 2) + ":**:**:**";
    }

    private static String getPartAddress(BluetoothDevice device) {
        if (device == null) {
            return "";
        }
        return getPartAddress(device.getAddress());
    }
}
