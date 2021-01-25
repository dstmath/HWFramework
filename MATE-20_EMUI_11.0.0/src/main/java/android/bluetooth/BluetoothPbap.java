package android.bluetooth;

import android.annotation.UnsupportedAppUsage;
import android.bluetooth.IBluetoothPbap;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BluetoothPbap implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.pbap.profile.action.CONNECTION_STATE_CHANGED";
    private static final boolean DBG = false;
    public static final int RESULT_CANCELED = 2;
    public static final int RESULT_FAILURE = 0;
    public static final int RESULT_SUCCESS = 1;
    private static final String TAG = "BluetoothPbap";
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        /* class android.bluetooth.BluetoothPbap.AnonymousClass1 */

        @Override // android.bluetooth.IBluetoothStateChangeCallback
        public void onBluetoothStateChange(boolean up) {
            BluetoothPbap.log("onBluetoothStateChange: up=" + up);
            if (!up) {
                BluetoothPbap.this.doUnbind();
            } else {
                BluetoothPbap.this.doBind();
            }
        }
    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        /* class android.bluetooth.BluetoothPbap.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothPbap.log("Proxy object connected");
            BluetoothPbap.this.mService = IBluetoothPbap.Stub.asInterface(service);
            if (BluetoothPbap.this.mServiceListener != null) {
                BluetoothPbap.this.mServiceListener.onServiceConnected(BluetoothPbap.this);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            BluetoothPbap.log("Proxy object disconnected");
            BluetoothPbap.this.doUnbind();
            if (BluetoothPbap.this.mServiceListener != null) {
                BluetoothPbap.this.mServiceListener.onServiceDisconnected();
            }
        }
    };
    private final Context mContext;
    private volatile IBluetoothPbap mService;
    private ServiceListener mServiceListener;

    public interface ServiceListener {
        void onServiceConnected(BluetoothPbap bluetoothPbap);

        void onServiceDisconnected();
    }

    public BluetoothPbap(Context context, ServiceListener l) {
        this.mContext = context;
        this.mServiceListener = l;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException re) {
                Log.e(TAG, "", re);
            }
        }
        doBind();
    }

    /* access modifiers changed from: package-private */
    public boolean doBind() {
        synchronized (this.mConnection) {
            try {
                if (this.mService == null) {
                    log("Binding service...");
                    Intent intent = new Intent(IBluetoothPbap.class.getName());
                    ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
                    intent.setComponent(comp);
                    if (comp == null || !this.mContext.bindServiceAsUser(intent, this.mConnection, 0, UserHandle.CURRENT_OR_SELF)) {
                        Log.e(TAG, "Could not bind to Bluetooth Pbap Service with " + intent);
                        return false;
                    }
                }
                return true;
            } catch (SecurityException se) {
                Log.e(TAG, "", se);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doUnbind() {
        synchronized (this.mConnection) {
            if (this.mService != null) {
                log("Unbinding service...");
                try {
                    this.mContext.unbindService(this.mConnection);
                } catch (IllegalArgumentException ie) {
                    Log.e(TAG, "", ie);
                } finally {
                    this.mService = null;
                }
            }
        }
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
            } catch (RemoteException re) {
                Log.e(TAG, "", re);
            }
        }
        doUnbind();
        this.mServiceListener = null;
    }

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getConnectedDevices() {
        log("getConnectedDevices()");
        IBluetoothPbap service = this.mService;
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
            return new ArrayList();
        }
        try {
            return service.getConnectedDevices();
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return new ArrayList();
        }
    }

    @Override // android.bluetooth.BluetoothProfile
    public int getConnectionState(BluetoothDevice device) {
        log("getConnectionState: device=" + device);
        IBluetoothPbap service = this.mService;
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
            return 0;
        }
        try {
            return service.getConnectionState(device);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        log("getDevicesMatchingConnectionStates: states=" + Arrays.toString(states));
        IBluetoothPbap service = this.mService;
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
            return new ArrayList();
        }
        try {
            return service.getDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return new ArrayList();
        }
    }

    public boolean isConnected(BluetoothDevice device) {
        return getConnectionState(device) == 2;
    }

    @UnsupportedAppUsage
    public boolean disconnect(BluetoothDevice device) {
        log("disconnect()");
        IBluetoothPbap service = this.mService;
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
        try {
            service.disconnect(device);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static void log(String msg) {
    }
}
