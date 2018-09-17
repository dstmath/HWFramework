package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothStateChangeCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class BluetoothA2dp implements BluetoothProfile {
    public static final int A2DP_CODEC_STATUS_LDAC_ERROR = 1001;
    public static final String ACTION_AVRCP_CONNECTION_STATE_CHANGED = "android.bluetooth.a2dp.profile.action.AVRCP_CONNECTION_STATE_CHANGED";
    public static final String ACTION_CODEC_CONFIG_CHANGED = "android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED";
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_PLAYING_STATE_CHANGED = "android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED";
    private static final boolean DBG = true;
    public static final int OPTIONAL_CODECS_NOT_SUPPORTED = 0;
    public static final int OPTIONAL_CODECS_PREF_DISABLED = 0;
    public static final int OPTIONAL_CODECS_PREF_ENABLED = 1;
    public static final int OPTIONAL_CODECS_PREF_UNKNOWN = -1;
    public static final int OPTIONAL_CODECS_SUPPORTED = 1;
    public static final int OPTIONAL_CODECS_SUPPORT_UNKNOWN = -1;
    public static final int STATE_NOT_PLAYING = 11;
    public static final int STATE_PLAYING = 10;
    private static final String TAG = "BluetoothA2dp";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new Stub() {
        public void onBluetoothStateChange(boolean up) {
            Log.d(BluetoothA2dp.TAG, "onBluetoothStateChange: up=" + up);
            if (up) {
                try {
                    BluetoothA2dp.this.mServiceLock.readLock().lock();
                    if (BluetoothA2dp.this.mService == null) {
                        BluetoothA2dp.this.doBind();
                    }
                    BluetoothA2dp.this.mServiceLock.readLock().unlock();
                    return;
                } catch (Exception re) {
                    Log.e(BluetoothA2dp.TAG, ProxyInfo.LOCAL_EXCL_LIST, re);
                    BluetoothA2dp.this.mServiceLock.readLock().unlock();
                    return;
                } catch (Throwable th) {
                    BluetoothA2dp.this.mServiceLock.readLock().unlock();
                    throw th;
                }
            }
            try {
                BluetoothA2dp.this.mServiceLock.writeLock().lock();
                BluetoothA2dp.this.mService = null;
                BluetoothA2dp.this.mContext.unbindService(BluetoothA2dp.this.mConnection);
                BluetoothA2dp.this.mServiceLock.writeLock().unlock();
            } catch (Exception re2) {
                Log.e(BluetoothA2dp.TAG, ProxyInfo.LOCAL_EXCL_LIST, re2);
                BluetoothA2dp.this.mServiceLock.writeLock().unlock();
            } catch (Throwable th2) {
                BluetoothA2dp.this.mServiceLock.writeLock().unlock();
                throw th2;
            }
        }
    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(BluetoothA2dp.TAG, "Proxy object connected");
            try {
                BluetoothA2dp.this.mServiceLock.writeLock().lock();
                BluetoothA2dp.this.mService = IBluetoothA2dp.Stub.asInterface(Binder.allowBlocking(service));
                if (BluetoothA2dp.this.mServiceListener != null) {
                    BluetoothA2dp.this.mServiceListener.onServiceConnected(2, BluetoothA2dp.this);
                }
            } finally {
                BluetoothA2dp.this.mServiceLock.writeLock().unlock();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(BluetoothA2dp.TAG, "Proxy object disconnected");
            try {
                BluetoothA2dp.this.mServiceLock.writeLock().lock();
                BluetoothA2dp.this.mService = null;
                if (BluetoothA2dp.this.mServiceListener != null) {
                    BluetoothA2dp.this.mServiceListener.onServiceDisconnected(2);
                }
            } finally {
                BluetoothA2dp.this.mServiceLock.writeLock().unlock();
            }
        }
    };
    private Context mContext;
    @GuardedBy("mServiceLock")
    private IBluetoothA2dp mService;
    private ServiceListener mServiceListener;
    private final ReentrantReadWriteLock mServiceLock = new ReentrantReadWriteLock();

    BluetoothA2dp(Context context, ServiceListener l) {
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
        Intent intent = new Intent(IBluetoothA2dp.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && (this.mContext.bindServiceAsUser(intent, this.mConnection, 0, Process.myUserHandle()) ^ 1) == 0) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth A2DP Service with " + intent);
        return false;
    }

    void close() {
        this.mServiceListener = null;
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (Exception e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            }
        }
        try {
            this.mServiceLock.writeLock().lock();
            if (this.mService != null) {
                this.mService = null;
                this.mContext.unbindService(this.mConnection);
            }
            this.mServiceLock.writeLock().unlock();
        } catch (Exception re) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, re);
            this.mServiceLock.writeLock().unlock();
        } catch (Throwable th) {
            this.mServiceLock.writeLock().unlock();
            throw th;
        }
    }

    public void finalize() {
    }

    public boolean connect(BluetoothDevice device) {
        log("connect(" + device + ")");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                boolean connect = this.mService.connect(device);
                return connect;
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
            return false;
        }
    }

    public boolean disconnect(BluetoothDevice device) {
        log("disconnect(" + device + ")");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                boolean disconnect = this.mService.disconnect(device);
                return disconnect;
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
            return false;
        }
    }

    public List<BluetoothDevice> getConnectedDevices() {
        List<BluetoothDevice> connectedDevices;
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null || !isEnabled()) {
                if (this.mService == null) {
                    Log.w(TAG, "Proxy not attached to service");
                }
                List arrayList = new ArrayList();
                this.mServiceLock.readLock().unlock();
                return arrayList;
            }
            connectedDevices = this.mService.getConnectedDevices();
            return connectedDevices;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            connectedDevices = new ArrayList();
            return connectedDevices;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        List<BluetoothDevice> devicesMatchingConnectionStates;
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null || !isEnabled()) {
                if (this.mService == null) {
                    Log.w(TAG, "Proxy not attached to service");
                }
                List arrayList = new ArrayList();
                this.mServiceLock.readLock().unlock();
                return arrayList;
            }
            devicesMatchingConnectionStates = this.mService.getDevicesMatchingConnectionStates(states);
            return devicesMatchingConnectionStates;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            devicesMatchingConnectionStates = new ArrayList();
            return devicesMatchingConnectionStates;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                int connectionState = this.mService.getConnectionState(device);
                return connectionState;
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
            return 0;
        }
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        log("setPriority(" + device + ", " + priority + ")");
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
                int priority = this.mService.getPriority(device);
                return priority;
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
            return 0;
        }
    }

    public boolean isAvrcpAbsoluteVolumeSupported() {
        Log.d(TAG, "isAvrcpAbsoluteVolumeSupported");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null || !isEnabled()) {
                if (this.mService == null) {
                    Log.w(TAG, "Proxy not attached to service");
                }
                this.mServiceLock.readLock().unlock();
                return false;
            }
            boolean isAvrcpAbsoluteVolumeSupported = this.mService.isAvrcpAbsoluteVolumeSupported();
            return isAvrcpAbsoluteVolumeSupported;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in isAvrcpAbsoluteVolumeSupported()", e);
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
            return false;
        }
    }

    public void adjustAvrcpAbsoluteVolume(int direction) {
        Log.d(TAG, "adjustAvrcpAbsoluteVolume");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                this.mService.adjustAvrcpAbsoluteVolume(direction);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in adjustAvrcpAbsoluteVolume()", e);
            this.mServiceLock.readLock().unlock();
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            throw th;
        }
    }

    public void setAvrcpAbsoluteVolume(int volume) {
        Log.d(TAG, "setAvrcpAbsoluteVolume");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                this.mService.setAvrcpAbsoluteVolume(volume);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in setAvrcpAbsoluteVolume()", e);
            this.mServiceLock.readLock().unlock();
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            throw th;
        }
    }

    public boolean isA2dpPlaying(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                boolean isA2dpPlaying = this.mService.isA2dpPlaying(device);
                return isA2dpPlaying;
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
            return false;
        }
    }

    public boolean shouldSendVolumeKeys(BluetoothDevice device) {
        if (isEnabled() && isValidDevice(device)) {
            ParcelUuid[] uuids = device.getUuids();
            if (uuids == null) {
                return false;
            }
            for (ParcelUuid uuid : uuids) {
                if (BluetoothUuid.isAvrcpTarget(uuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    public BluetoothCodecStatus getCodecStatus() {
        Log.d(TAG, "getCodecStatus");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null || !isEnabled()) {
                if (this.mService == null) {
                    Log.w(TAG, "Proxy not attached to service");
                }
                this.mServiceLock.readLock().unlock();
                return null;
            }
            BluetoothCodecStatus codecStatus = this.mService.getCodecStatus();
            return codecStatus;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in getCodecStatus()", e);
            return null;
        } finally {
            this.mServiceLock.readLock().unlock();
            return null;
        }
    }

    public boolean isSupportPlayLDAC(String deviceMac) {
        Log.d(TAG, "isSupportPlayLDAC");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null || !isEnabled()) {
                if (this.mService == null) {
                    Log.w(TAG, "Proxy not attached to service");
                }
                this.mServiceLock.readLock().unlock();
                return false;
            }
            boolean isSupportPlayLDAC = this.mService.isSupportPlayLDAC(deviceMac);
            return isSupportPlayLDAC;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in isSupportPlayLDAC()", e);
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
            return false;
        }
    }

    public int getLdacQualityValue() {
        Log.d(TAG, "getLdacQualityValue");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null || !isEnabled()) {
                if (this.mService == null) {
                    Log.w(TAG, "Proxy not attached to service");
                }
                this.mServiceLock.readLock().unlock();
                return 1001;
            }
            int ldacQualityValue = this.mService.getLdacQualityValue();
            return ldacQualityValue;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in getLdacQualityValue()", e);
            return 1001;
        } finally {
            this.mServiceLock.readLock().unlock();
            return 1001;
        }
    }

    public void setCodecConfigPreference(BluetoothCodecConfig codecConfig) {
        Log.d(TAG, "setCodecConfigPreference");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                this.mService.setCodecConfigPreference(codecConfig);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in setCodecConfigPreference()", e);
            this.mServiceLock.readLock().unlock();
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            throw th;
        }
    }

    public void enableOptionalCodecs() {
        Log.d(TAG, "enableOptionalCodecs");
        enableDisableOptionalCodecs(true);
    }

    public void disableOptionalCodecs() {
        Log.d(TAG, "disableOptionalCodecs");
        enableDisableOptionalCodecs(false);
    }

    private void enableDisableOptionalCodecs(boolean enable) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                if (enable) {
                    this.mService.enableOptionalCodecs();
                } else {
                    this.mService.disableOptionalCodecs();
                }
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in enableDisableOptionalCodecs()", e);
            this.mServiceLock.readLock().unlock();
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            throw th;
        }
    }

    public int supportsOptionalCodecs(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                int supportsOptionalCodecs = this.mService.supportsOptionalCodecs(device);
                return supportsOptionalCodecs;
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in getSupportsOptionalCodecs()", e);
            return -1;
        } finally {
            this.mServiceLock.readLock().unlock();
            return -1;
        }
    }

    public int getOptionalCodecsEnabled(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                int optionalCodecsEnabled = this.mService.getOptionalCodecsEnabled(device);
                return optionalCodecsEnabled;
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in getSupportsOptionalCodecs()", e);
            return -1;
        } finally {
            this.mServiceLock.readLock().unlock();
            return -1;
        }
    }

    public void setOptionalCodecsEnabled(BluetoothDevice device, int value) {
        if (value == -1 || value == 0 || value == 1) {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                this.mService.setOptionalCodecsEnabled(device, value);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return;
        }
        try {
            Log.e(TAG, "Invalid value passed to setOptionalCodecsEnabled: " + value);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
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
                return "connected";
            case 3:
                return "disconnecting";
            case 10:
                return "playing";
            case 11:
                return "not playing";
            default:
                return "<unknown state " + state + ">";
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

    private static void infolog(String msg) {
        Log.i(TAG, msg);
    }
}
