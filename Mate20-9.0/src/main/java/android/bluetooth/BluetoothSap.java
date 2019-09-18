package android.bluetooth;

import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothSap;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothSap implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.sap.profile.action.CONNECTION_STATE_CHANGED";
    private static final boolean DBG = true;
    public static final int RESULT_CANCELED = 2;
    public static final int RESULT_SUCCESS = 1;
    public static final int STATE_ERROR = -1;
    private static final String TAG = "BluetoothSap";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
        public void onBluetoothStateChange(boolean up) {
            Log.d(BluetoothSap.TAG, "onBluetoothStateChange: up=" + up);
            if (!up) {
                synchronized (BluetoothSap.this.mConnection) {
                    try {
                        IBluetoothSap unused = BluetoothSap.this.mService = null;
                        BluetoothSap.this.mContext.unbindService(BluetoothSap.this.mConnection);
                    } catch (Exception re) {
                        Log.e(BluetoothSap.TAG, "", re);
                    }
                }
                return;
            }
            synchronized (BluetoothSap.this.mConnection) {
                try {
                    if (BluetoothSap.this.mService == null) {
                        BluetoothSap.this.doBind();
                    }
                } catch (Exception re2) {
                    Log.e(BluetoothSap.TAG, "", re2);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothSap.log("Proxy object connected");
            IBluetoothSap unused = BluetoothSap.this.mService = IBluetoothSap.Stub.asInterface(Binder.allowBlocking(service));
            if (BluetoothSap.this.mServiceListener != null) {
                BluetoothSap.this.mServiceListener.onServiceConnected(10, BluetoothSap.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            BluetoothSap.log("Proxy object disconnected");
            synchronized (BluetoothSap.this.mConnection) {
                if (BluetoothSap.this.mService != null) {
                    try {
                        IBluetoothSap unused = BluetoothSap.this.mService = null;
                        BluetoothSap.this.mContext.unbindService(BluetoothSap.this.mConnection);
                    } catch (Exception re) {
                        Log.e(BluetoothSap.TAG, "", re);
                    }
                }
            }
            if (BluetoothSap.this.mServiceListener != null) {
                BluetoothSap.this.mServiceListener.onServiceDisconnected(10);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public volatile IBluetoothSap mService;
    /* access modifiers changed from: private */
    public BluetoothProfile.ServiceListener mServiceListener;

    BluetoothSap(Context context, BluetoothProfile.ServiceListener l) {
        Log.d(TAG, "Create BluetoothSap proxy object");
        this.mContext = context;
        this.mServiceListener = l;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }
        doBind();
    }

    /* access modifiers changed from: package-private */
    public boolean doBind() {
        Intent intent = new Intent(IBluetoothSap.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, this.mConnection, 0, this.mContext.getUser())) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth SAP Service with " + intent);
        return false;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
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
                Log.e(TAG, "", e);
            }
        }
        synchronized (this.mConnection) {
            if (this.mService != null) {
                try {
                    this.mService = null;
                    this.mContext.unbindService(this.mConnection);
                } catch (Exception re) {
                    Log.e(TAG, "", re);
                }
            }
        }
        this.mServiceListener = null;
    }

    public int getState() {
        IBluetoothSap service = this.mService;
        if (service != null) {
            try {
                return service.getState();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            log(Log.getStackTraceString(new Throwable()));
            return -1;
        }
    }

    public BluetoothDevice getClient() {
        IBluetoothSap service = this.mService;
        if (service != null) {
            try {
                return service.getClient();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            log(Log.getStackTraceString(new Throwable()));
            return null;
        }
    }

    public boolean isConnected(BluetoothDevice device) {
        IBluetoothSap service = this.mService;
        if (service != null) {
            try {
                return service.isConnected(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            log(Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean connect(BluetoothDevice device) {
        log("connect(" + device + ")not supported for SAPS");
        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        log("disconnect(" + device + ")");
        IBluetoothSap service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.disconnect(device);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public List<BluetoothDevice> getConnectedDevices() {
        log("getConnectedDevices()");
        IBluetoothSap service = this.mService;
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

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        log("getDevicesMatchingStates()");
        IBluetoothSap service = this.mService;
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

    public int getConnectionState(BluetoothDevice device) {
        log("getConnectionState(" + device + ")");
        IBluetoothSap service = this.mService;
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
        log("setPriority(" + device + ", " + priority + ")");
        IBluetoothSap service = this.mService;
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
        IBluetoothSap service = this.mService;
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

    /* access modifiers changed from: private */
    public static void log(String msg) {
        Log.d(TAG, msg);
    }

    private boolean isEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getState() == 12) {
            return true;
        }
        log("Bluetooth is Not enabled");
        return false;
    }

    private static boolean isValidDevice(BluetoothDevice device) {
        return device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress());
    }
}
