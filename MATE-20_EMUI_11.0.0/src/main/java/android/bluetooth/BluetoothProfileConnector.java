package android.bluetooth;

import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

public abstract class BluetoothProfileConnector<T> {
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        /* class android.bluetooth.BluetoothProfileConnector.AnonymousClass1 */

        @Override // android.bluetooth.IBluetoothStateChangeCallback
        public void onBluetoothStateChange(boolean up) {
            if (up) {
                BluetoothProfileConnector.this.doBind();
            } else {
                BluetoothProfileConnector.this.doUnbind();
            }
        }
    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        /* class android.bluetooth.BluetoothProfileConnector.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothProfileConnector.this.logDebug("Proxy object connected");
            BluetoothProfileConnector bluetoothProfileConnector = BluetoothProfileConnector.this;
            bluetoothProfileConnector.mService = bluetoothProfileConnector.getServiceInterface(service);
            if (BluetoothProfileConnector.this.mServiceListener != null) {
                BluetoothProfileConnector.this.mServiceListener.onServiceConnected(BluetoothProfileConnector.this.mProfileId, BluetoothProfileConnector.this.mProfileProxy);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            BluetoothProfileConnector.this.logDebug("Proxy object disconnected");
            BluetoothProfileConnector.this.doUnbind();
            if (BluetoothProfileConnector.this.mServiceListener != null) {
                BluetoothProfileConnector.this.mServiceListener.onServiceDisconnected(BluetoothProfileConnector.this.mProfileId);
            }
        }
    };
    private Context mContext;
    private final int mProfileId;
    private final String mProfileName;
    private final BluetoothProfile mProfileProxy;
    private volatile T mService;
    private BluetoothProfile.ServiceListener mServiceListener;
    private final String mServiceName;

    public abstract T getServiceInterface(IBinder iBinder);

    BluetoothProfileConnector(BluetoothProfile profile, int profileId, String profileName, String serviceName) {
        this.mProfileId = profileId;
        this.mProfileProxy = profile;
        this.mProfileName = profileName;
        this.mServiceName = serviceName;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean doBind() {
        synchronized (this.mConnection) {
            if (this.mService == null) {
                logDebug("Binding service...");
                try {
                    Intent intent = new Intent(this.mServiceName);
                    ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
                    intent.setComponent(comp);
                    if (comp != null) {
                        if (!this.mContext.bindServiceAsUser(intent, this.mConnection, 0, UserHandle.CURRENT_OR_SELF)) {
                        }
                    }
                    logError("Could not bind to Bluetooth Service with " + intent);
                    return false;
                } catch (SecurityException se) {
                    logError("Failed to bind service. " + se);
                    return false;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doUnbind() {
        synchronized (this.mConnection) {
            if (this.mService != null) {
                logDebug("Unbinding service...");
                try {
                    this.mContext.unbindService(this.mConnection);
                } catch (IllegalArgumentException ie) {
                    logError("Unable to unbind service: " + ie);
                } finally {
                    this.mService = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void connect(Context context, BluetoothProfile.ServiceListener listener) {
        this.mContext = context;
        this.mServiceListener = listener;
        IBluetoothManager mgr = BluetoothAdapter.getDefaultAdapter().getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException re) {
                logError("Failed to register state change callback. " + re);
            }
        }
        doBind();
    }

    /* access modifiers changed from: package-private */
    public void disconnect() {
        this.mServiceListener = null;
        IBluetoothManager mgr = BluetoothAdapter.getDefaultAdapter().getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException re) {
                logError("Failed to unregister state change callback" + re);
            }
        }
        doUnbind();
    }

    /* access modifiers changed from: package-private */
    public T getService() {
        return this.mService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logDebug(String log) {
        Log.d(this.mProfileName, log);
    }

    private void logError(String log) {
        Log.e(this.mProfileName, log);
    }
}
