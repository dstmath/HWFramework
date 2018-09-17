package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothStateChangeCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothPbapClient implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.pbap.profile.action.CONNECTION_STATE_CHANGED";
    private static final boolean DBG = false;
    public static final int RESULT_CANCELED = 2;
    public static final int RESULT_FAILURE = 0;
    public static final int RESULT_SUCCESS = 1;
    public static final int STATE_ERROR = -1;
    private static final String TAG = "BluetoothPbapClient";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback;
    private final ServiceConnection mConnection;
    private final Context mContext;
    private IBluetoothPbapClient mService;
    private ServiceListener mServiceListener;

    BluetoothPbapClient(Context context, ServiceListener l) {
        this.mBluetoothStateChangeCallback = new Stub() {
            public void onBluetoothStateChange(boolean up) {
                ServiceConnection -get0;
                if (up) {
                    -get0 = BluetoothPbapClient.this.mConnection;
                    synchronized (-get0) {
                        try {
                        } catch (Exception re) {
                            Log.e(BluetoothPbapClient.TAG, ProxyInfo.LOCAL_EXCL_LIST, re);
                        }
                    }
                    if (BluetoothPbapClient.this.mService == null) {
                        BluetoothPbapClient.this.doBind();
                    }
                } else {
                    -get0 = BluetoothPbapClient.this.mConnection;
                    synchronized (-get0) {
                        try {
                        } catch (Exception re2) {
                            Log.e(BluetoothPbapClient.TAG, ProxyInfo.LOCAL_EXCL_LIST, re2);
                        }
                    }
                    BluetoothPbapClient.this.mService = null;
                    BluetoothPbapClient.this.mContext.unbindService(BluetoothPbapClient.this.mConnection);
                }
            }
        };
        this.mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                BluetoothPbapClient.this.mService = IBluetoothPbapClient.Stub.asInterface(service);
                if (BluetoothPbapClient.this.mServiceListener != null) {
                    BluetoothPbapClient.this.mServiceListener.onServiceConnected(17, BluetoothPbapClient.this);
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                BluetoothPbapClient.this.mService = null;
                if (BluetoothPbapClient.this.mServiceListener != null) {
                    BluetoothPbapClient.this.mServiceListener.onServiceDisconnected(17);
                }
            }
        };
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

    private boolean doBind() {
        Intent intent = new Intent(IBluetoothPbapClient.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), RESULT_FAILURE);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, this.mConnection, RESULT_FAILURE, Process.myUserHandle())) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth PBAP Client Service with " + intent);
        return DBG;
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public synchronized void close() {
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
    }

    public boolean connect(BluetoothDevice device) {
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.connect(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return DBG;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return DBG;
    }

    public boolean disconnect(BluetoothDevice device) {
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                this.mService.disconnect(device);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return DBG;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return DBG;
    }

    public List<BluetoothDevice> getConnectedDevices() {
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
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return RESULT_FAILURE;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return RESULT_FAILURE;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    private boolean isEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getState() == 12) {
            return true;
        }
        log("Bluetooth is Not enabled");
        return DBG;
    }

    private boolean isValidDevice(BluetoothDevice device) {
        if (device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            return true;
        }
        return DBG;
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        if (this.mService == null || !isEnabled() || !isValidDevice(device)) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return DBG;
        } else if (priority != 0 && priority != 100) {
            return DBG;
        } else {
            try {
                return this.mService.setPriority(device, priority);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return DBG;
            }
        }
    }

    public int getPriority(BluetoothDevice device) {
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getPriority(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return RESULT_FAILURE;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return RESULT_FAILURE;
    }
}
