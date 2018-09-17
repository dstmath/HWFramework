package android.bluetooth;

import android.app.PendingIntent;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothStateChangeCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
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
    public static final String EXTRA_SENDER_CONTACT_NAME = "android.bluetooth.mapmce.profile.extra.SENDER_CONTACT_NAME";
    public static final String EXTRA_SENDER_CONTACT_URI = "android.bluetooth.mapmce.profile.extra.SENDER_CONTACT_URI";
    public static final int RESULT_CANCELED = 2;
    public static final int RESULT_FAILURE = 0;
    public static final int RESULT_SUCCESS = 1;
    public static final int STATE_ERROR = -1;
    private static final String TAG = "BluetoothMapClient";
    private static final boolean VDBG = Log.isLoggable(TAG, 2);
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new Stub() {
        public void onBluetoothStateChange(boolean up) {
            if (BluetoothMapClient.DBG) {
                Log.d(BluetoothMapClient.TAG, "onBluetoothStateChange: up=" + up);
            }
            ServiceConnection -get2;
            if (up) {
                -get2 = BluetoothMapClient.this.mConnection;
                synchronized (-get2) {
                    try {
                        if (BluetoothMapClient.this.mService == null) {
                            if (BluetoothMapClient.VDBG) {
                                Log.d(BluetoothMapClient.TAG, "Binding service...");
                            }
                            BluetoothMapClient.this.doBind();
                        }
                    } catch (Exception re) {
                        Log.e(BluetoothMapClient.TAG, ProxyInfo.LOCAL_EXCL_LIST, re);
                    }
                }
            } else {
                if (BluetoothMapClient.VDBG) {
                    Log.d(BluetoothMapClient.TAG, "Unbinding service...");
                }
                -get2 = BluetoothMapClient.this.mConnection;
                synchronized (-get2) {
                    try {
                        BluetoothMapClient.this.mService = null;
                        BluetoothMapClient.this.mContext.unbindService(BluetoothMapClient.this.mConnection);
                    } catch (Exception re2) {
                        Log.e(BluetoothMapClient.TAG, ProxyInfo.LOCAL_EXCL_LIST, re2);
                    }
                }
            }
            return;
        }
    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (BluetoothMapClient.DBG) {
                Log.d(BluetoothMapClient.TAG, "Proxy object connected");
            }
            BluetoothMapClient.this.mService = IBluetoothMapClient.Stub.asInterface(service);
            if (BluetoothMapClient.this.mServiceListener != null) {
                BluetoothMapClient.this.mServiceListener.onServiceConnected(18, BluetoothMapClient.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (BluetoothMapClient.DBG) {
                Log.d(BluetoothMapClient.TAG, "Proxy object disconnected");
            }
            BluetoothMapClient.this.mService = null;
            if (BluetoothMapClient.this.mServiceListener != null) {
                BluetoothMapClient.this.mServiceListener.onServiceDisconnected(18);
            }
        }
    };
    private final Context mContext;
    private IBluetoothMapClient mService;
    private ServiceListener mServiceListener;

    BluetoothMapClient(Context context, ServiceListener l) {
        if (DBG) {
            Log.d(TAG, "Create BluetoothMapClient proxy object");
        }
        this.mContext = context;
        this.mServiceListener = l;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            }
        }
        doBind();
    }

    boolean doBind() {
        Intent intent = new Intent(IBluetoothMapClient.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && (this.mContext.bindServiceAsUser(intent, this.mConnection, 0, Process.myUserHandle()) ^ 1) == 0) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth MAP MCE Service with " + intent);
        return false;
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public void close() {
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (Exception e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            }
        }
        synchronized (this.mConnection) {
            if (this.mService != null) {
                try {
                    this.mService = null;
                    this.mContext.unbindService(this.mConnection);
                } catch (Exception re) {
                    Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, re);
                }
            }
        }
        this.mServiceListener = null;
        return;
    }

    public boolean isConnected(BluetoothDevice device) {
        if (VDBG) {
            Log.d(TAG, "isConnected(" + device + ")");
        }
        if (this.mService != null) {
            try {
                return this.mService.isConnected(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) {
                Log.d(TAG, Log.getStackTraceString(new Throwable()));
            }
            return false;
        }
    }

    public boolean connect(BluetoothDevice device) {
        if (DBG) {
            Log.d(TAG, "connect(" + device + ")" + "for MAPS MCE");
        }
        if (this.mService != null) {
            try {
                return this.mService.connect(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            if (DBG) {
                Log.d(TAG, Log.getStackTraceString(new Throwable()));
            }
            return false;
        }
    }

    public boolean disconnect(BluetoothDevice device) {
        if (DBG) {
            Log.d(TAG, "disconnect(" + device + ")");
        }
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.disconnect(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (DBG) {
            Log.d(TAG, "getConnectedDevices()");
        }
        if (this.mService == null || !isEnabled()) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return this.mService.getConnectedDevices();
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        if (DBG) {
            Log.d(TAG, "getDevicesMatchingStates()");
        }
        if (this.mService == null || !isEnabled()) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return this.mService.getDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        if (DBG) {
            Log.d(TAG, "getConnectionState(" + device + ")");
        }
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return 0;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return 0;
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        if (DBG) {
            Log.d(TAG, "setPriority(" + device + ", " + priority + ")");
        }
        if (this.mService == null || !isEnabled() || !isValidDevice(device)) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        } else if (priority != 0 && priority != 100) {
            return false;
        } else {
            try {
                return this.mService.setPriority(device, priority);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
    }

    public int getPriority(BluetoothDevice device) {
        if (VDBG) {
            Log.d(TAG, "getPriority(" + device + ")");
        }
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getPriority(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return 0;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return 0;
    }

    public boolean sendMessage(BluetoothDevice device, Uri[] contacts, String message, PendingIntent sentIntent, PendingIntent deliveredIntent) {
        if (DBG) {
            Log.d(TAG, "sendMessage(" + device + ", " + contacts + ", " + message);
        }
        if (this.mService == null || !isEnabled() || !isValidDevice(device)) {
            return false;
        }
        try {
            return this.mService.sendMessage(device, contacts, message, sentIntent, deliveredIntent);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean getUnreadMessages(BluetoothDevice device) {
        if (DBG) {
            Log.d(TAG, "getUnreadMessages(" + device + ")");
        }
        if (this.mService == null || !isEnabled() || !isValidDevice(device)) {
            return false;
        }
        try {
            return this.mService.getUnreadMessages(device);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    private boolean isEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getState() == 12) {
            return true;
        }
        if (DBG) {
            Log.d(TAG, "Bluetooth is Not enabled");
        }
        return false;
    }

    private boolean isValidDevice(BluetoothDevice device) {
        if (device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            return true;
        }
        return false;
    }
}
