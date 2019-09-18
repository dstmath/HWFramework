package android.bluetooth;

import android.bluetooth.IBluetoothPbap;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
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
        /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
        public void onBluetoothStateChange(boolean up) {
            BluetoothPbap.log("onBluetoothStateChange: up=" + up);
            if (!up) {
                BluetoothPbap.log("Unbinding service...");
                synchronized (BluetoothPbap.this.mConnection) {
                    try {
                        IBluetoothPbap unused = BluetoothPbap.this.mService = null;
                        BluetoothPbap.this.mContext.unbindService(BluetoothPbap.this.mConnection);
                    } catch (Exception re) {
                        Log.e(BluetoothPbap.TAG, "", re);
                    }
                }
                return;
            }
            synchronized (BluetoothPbap.this.mConnection) {
                try {
                    if (BluetoothPbap.this.mService == null) {
                        BluetoothPbap.log("Binding service...");
                        BluetoothPbap.this.doBind();
                    }
                } catch (Exception re2) {
                    Log.e(BluetoothPbap.TAG, "", re2);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothPbap.log("Proxy object connected");
            IBluetoothPbap unused = BluetoothPbap.this.mService = IBluetoothPbap.Stub.asInterface(service);
            if (BluetoothPbap.this.mServiceListener != null) {
                BluetoothPbap.this.mServiceListener.onServiceConnected(BluetoothPbap.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            BluetoothPbap.log("Proxy object disconnected");
            IBluetoothPbap unused = BluetoothPbap.this.mService = null;
            if (BluetoothPbap.this.mServiceListener != null) {
                BluetoothPbap.this.mServiceListener.onServiceDisconnected();
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public volatile IBluetoothPbap mService;
    /* access modifiers changed from: private */
    public ServiceListener mServiceListener;

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
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }
        doBind();
    }

    /* access modifiers changed from: package-private */
    public boolean doBind() {
        Intent intent = new Intent(IBluetoothPbap.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, this.mConnection, 0, this.mContext.getUser())) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth Pbap Service with " + intent);
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
