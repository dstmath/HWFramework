package android.bluetooth;

import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothHearingAid;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class BluetoothHearingAid implements BluetoothProfile {
    public static final String ACTION_ACTIVE_DEVICE_CHANGED = "android.bluetooth.hearingaid.profile.action.ACTIVE_DEVICE_CHANGED";
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.hearingaid.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_PLAYING_STATE_CHANGED = "android.bluetooth.hearingaid.profile.action.PLAYING_STATE_CHANGED";
    private static final boolean DBG = false;
    public static final long HI_SYNC_ID_INVALID = 0;
    public static final int MODE_BINAURAL = 1;
    public static final int MODE_MONAURAL = 0;
    public static final int SIDE_LEFT = 0;
    public static final int SIDE_RIGHT = 1;
    public static final int STATE_NOT_PLAYING = 11;
    public static final int STATE_PLAYING = 10;
    private static final String TAG = "BluetoothHearingAid";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        public void onBluetoothStateChange(boolean up) {
            if (!up) {
                try {
                    BluetoothHearingAid.this.mServiceLock.writeLock().lock();
                    IBluetoothHearingAid unused = BluetoothHearingAid.this.mService = null;
                    BluetoothHearingAid.this.mContext.unbindService(BluetoothHearingAid.this.mConnection);
                } catch (Exception re) {
                    Log.e(BluetoothHearingAid.TAG, "", re);
                } catch (Throwable th) {
                    BluetoothHearingAid.this.mServiceLock.writeLock().unlock();
                    throw th;
                }
                BluetoothHearingAid.this.mServiceLock.writeLock().unlock();
                return;
            }
            try {
                BluetoothHearingAid.this.mServiceLock.readLock().lock();
                if (BluetoothHearingAid.this.mService == null) {
                    BluetoothHearingAid.this.doBind();
                }
            } catch (Exception re2) {
                Log.e(BluetoothHearingAid.TAG, "", re2);
            } catch (Throwable th2) {
                BluetoothHearingAid.this.mServiceLock.readLock().unlock();
                throw th2;
            }
            BluetoothHearingAid.this.mServiceLock.readLock().unlock();
        }
    };
    /* access modifiers changed from: private */
    public final ServiceConnection mConnection = new ServiceConnection() {
        /* JADX INFO: finally extract failed */
        public void onServiceConnected(ComponentName className, IBinder service) {
            try {
                BluetoothHearingAid.this.mServiceLock.writeLock().lock();
                IBluetoothHearingAid unused = BluetoothHearingAid.this.mService = IBluetoothHearingAid.Stub.asInterface(Binder.allowBlocking(service));
                BluetoothHearingAid.this.mServiceLock.writeLock().unlock();
                if (BluetoothHearingAid.this.mServiceListener != null) {
                    BluetoothHearingAid.this.mServiceListener.onServiceConnected(21, BluetoothHearingAid.this);
                }
            } catch (Throwable th) {
                BluetoothHearingAid.this.mServiceLock.writeLock().unlock();
                throw th;
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            try {
                BluetoothHearingAid.this.mServiceLock.writeLock().lock();
                if (BluetoothHearingAid.this.mService != null) {
                    IBluetoothHearingAid unused = BluetoothHearingAid.this.mService = null;
                    BluetoothHearingAid.this.mContext.unbindService(BluetoothHearingAid.this.mConnection);
                }
            } catch (Exception e) {
                Log.e(BluetoothHearingAid.TAG, "", e);
            } catch (Throwable th) {
                BluetoothHearingAid.this.mServiceLock.writeLock().unlock();
                throw th;
            }
            BluetoothHearingAid.this.mServiceLock.writeLock().unlock();
            if (BluetoothHearingAid.this.mServiceListener != null) {
                BluetoothHearingAid.this.mServiceListener.onServiceDisconnected(21);
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    @GuardedBy("mServiceLock")
    public IBluetoothHearingAid mService;
    /* access modifiers changed from: private */
    public BluetoothProfile.ServiceListener mServiceListener;
    /* access modifiers changed from: private */
    public final ReentrantReadWriteLock mServiceLock = new ReentrantReadWriteLock();

    BluetoothHearingAid(Context context, BluetoothProfile.ServiceListener l) {
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
    public void doBind() {
        Intent intent = new Intent(IBluetoothHearingAid.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp == null || !this.mContext.bindServiceAsUser(intent, this.mConnection, 0, Process.myUserHandle())) {
            Log.e(TAG, "Could not bind to Bluetooth Hearing Aid Service with " + intent);
        }
    }

    /* access modifiers changed from: package-private */
    public void close() {
        this.mServiceListener = null;
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
        try {
            this.mServiceLock.writeLock().lock();
            if (this.mService != null) {
                this.mService = null;
                this.mContext.unbindService(this.mConnection);
            }
        } catch (Exception re) {
            Log.e(TAG, "", re);
        } catch (Throwable th) {
            this.mServiceLock.writeLock().unlock();
            throw th;
        }
        this.mServiceLock.writeLock().unlock();
    }

    public void finalize() {
    }

    public boolean connect(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                return this.mService.connect(device);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean disconnect(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                return this.mService.disconnect(device);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public List<BluetoothDevice> getConnectedDevices() {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.getConnectedDevices();
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            ArrayList arrayList = new ArrayList();
            this.mServiceLock.readLock().unlock();
            return arrayList;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.getDevicesMatchingConnectionStates(states);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            ArrayList arrayList = new ArrayList();
            this.mServiceLock.readLock().unlock();
            return arrayList;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                return this.mService.getConnectionState(device);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return 0;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean setActiveDevice(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null || !isEnabled() || (device != null && !isValidDevice(device))) {
                if (this.mService == null) {
                    Log.w(TAG, "Proxy not attached to service");
                }
                this.mServiceLock.readLock().unlock();
                return false;
            }
            this.mService.setActiveDevice(device);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public List<BluetoothDevice> getActiveDevices() {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.getActiveDevices();
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            ArrayList arrayList = new ArrayList();
            this.mServiceLock.readLock().unlock();
            return arrayList;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:13:0x002b=Splitter:B:13:0x002b, B:17:0x003b=Splitter:B:17:0x003b} */
    public boolean setPriority(BluetoothDevice device, int priority) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null || !isEnabled() || !isValidDevice(device)) {
                if (this.mService == null) {
                    Log.w(TAG, "Proxy not attached to service");
                }
                this.mServiceLock.readLock().unlock();
                return false;
            } else if (priority != 0 && priority != 100) {
                return false;
            } else {
                boolean priority2 = this.mService.setPriority(device, priority);
                this.mServiceLock.readLock().unlock();
                return priority2;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getPriority(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                return this.mService.getPriority(device);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return 0;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public static String stateToString(int state) {
        switch (state) {
            case 0:
                return "disconnected";
            case 1:
                return "connecting";
            case 2:
                return UsbManager.USB_CONNECTED;
            case 3:
                return "disconnecting";
            default:
                switch (state) {
                    case 10:
                        return "playing";
                    case 11:
                        return "not playing";
                    default:
                        return "<unknown state " + state + ">";
                }
        }
    }

    public int getVolume() {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.getVolume();
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return 0;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public void adjustVolume(int direction) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
                this.mServiceLock.readLock().unlock();
            } else if (!isEnabled()) {
                this.mServiceLock.readLock().unlock();
            } else {
                this.mService.adjustVolume(direction);
                this.mServiceLock.readLock().unlock();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            throw th;
        }
    }

    public void setVolume(int volume) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
                this.mServiceLock.readLock().unlock();
            } else if (!isEnabled()) {
                this.mServiceLock.readLock().unlock();
            } else {
                this.mService.setVolume(volume);
                this.mServiceLock.readLock().unlock();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            throw th;
        }
    }

    public long getHiSyncId(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
                return 0;
            }
            if (isEnabled()) {
                if (isValidDevice(device)) {
                    long hiSyncId = this.mService.getHiSyncId(device);
                    this.mServiceLock.readLock().unlock();
                    return hiSyncId;
                }
            }
            this.mServiceLock.readLock().unlock();
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return 0;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getDeviceSide(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                return this.mService.getDeviceSide(device);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return 0;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getDeviceMode(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                return this.mService.getDeviceMode(device);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return 0;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    private boolean isEnabled() {
        if (this.mAdapter.getState() == 12) {
            return true;
        }
        return false;
    }

    private boolean isValidDevice(BluetoothDevice device) {
        if (device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            return true;
        }
        return false;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
