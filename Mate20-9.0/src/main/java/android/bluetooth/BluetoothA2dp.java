package android.bluetooth;

import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothA2dp;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class BluetoothA2dp implements BluetoothProfile {
    public static final int A2DP_CODEC_STATUS_LDAC_ERROR = 1001;
    public static final String ACTION_ACTIVE_DEVICE_CHANGED = "android.bluetooth.a2dp.profile.action.ACTIVE_DEVICE_CHANGED";
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
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        public void onBluetoothStateChange(boolean up) {
            Log.d(BluetoothA2dp.TAG, "onBluetoothStateChange: up=" + up);
            if (!up) {
                try {
                    BluetoothA2dp.this.mServiceLock.writeLock().lock();
                    IBluetoothA2dp unused = BluetoothA2dp.this.mService = null;
                    BluetoothA2dp.this.mContext.unbindService(BluetoothA2dp.this.mConnection);
                } catch (Exception re) {
                    Log.e(BluetoothA2dp.TAG, "", re);
                } catch (Throwable th) {
                    BluetoothA2dp.this.mServiceLock.writeLock().unlock();
                    throw th;
                }
                BluetoothA2dp.this.mServiceLock.writeLock().unlock();
                return;
            }
            try {
                BluetoothA2dp.this.mServiceLock.readLock().lock();
                if (BluetoothA2dp.this.mService == null) {
                    BluetoothA2dp.this.doBind();
                }
            } catch (Exception re2) {
                Log.e(BluetoothA2dp.TAG, "", re2);
            } catch (Throwable th2) {
                BluetoothA2dp.this.mServiceLock.readLock().unlock();
                throw th2;
            }
            BluetoothA2dp.this.mServiceLock.readLock().unlock();
        }
    };
    /* access modifiers changed from: private */
    public final ServiceConnection mConnection = new ServiceConnection() {
        /* JADX INFO: finally extract failed */
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(BluetoothA2dp.TAG, "Proxy object connected");
            try {
                BluetoothA2dp.this.mServiceLock.writeLock().lock();
                IBluetoothA2dp unused = BluetoothA2dp.this.mService = IBluetoothA2dp.Stub.asInterface(Binder.allowBlocking(service));
                BluetoothA2dp.this.mServiceLock.writeLock().unlock();
                if (BluetoothA2dp.this.mServiceListener != null) {
                    BluetoothA2dp.this.mServiceListener.onServiceConnected(2, BluetoothA2dp.this);
                }
            } catch (Throwable th) {
                BluetoothA2dp.this.mServiceLock.writeLock().unlock();
                throw th;
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(BluetoothA2dp.TAG, "Proxy object disconnected");
            try {
                BluetoothA2dp.this.mServiceLock.writeLock().lock();
                if (BluetoothA2dp.this.mService != null) {
                    IBluetoothA2dp unused = BluetoothA2dp.this.mService = null;
                    BluetoothA2dp.this.mContext.unbindService(BluetoothA2dp.this.mConnection);
                }
            } catch (Exception e) {
                Log.e(BluetoothA2dp.TAG, "", e);
            } catch (Throwable th) {
                BluetoothA2dp.this.mServiceLock.writeLock().unlock();
                throw th;
            }
            BluetoothA2dp.this.mServiceLock.writeLock().unlock();
            if (BluetoothA2dp.this.mServiceListener != null) {
                BluetoothA2dp.this.mServiceListener.onServiceDisconnected(2);
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    @GuardedBy("mServiceLock")
    public IBluetoothA2dp mService;
    /* access modifiers changed from: private */
    public BluetoothProfile.ServiceListener mServiceListener;
    /* access modifiers changed from: private */
    public final ReentrantReadWriteLock mServiceLock = new ReentrantReadWriteLock();

    BluetoothA2dp(Context context, BluetoothProfile.ServiceListener l) {
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
        Intent intent = new Intent(IBluetoothA2dp.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, this.mConnection, 0, this.mContext.getUser())) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth A2DP Service with " + intent);
        return false;
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
        log("connect(" + device + ")");
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
        log("disconnect(" + device + ")");
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
        log("setActiveDevice(" + device + ")");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && (device == null || isValidDevice(device))) {
                return this.mService.setActiveDevice(device);
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

    public BluetoothDevice getActiveDevice() {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.getActiveDevice();
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return null;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:13:0x004d=Splitter:B:13:0x004d, B:17:0x005d=Splitter:B:17:0x005d} */
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

    public boolean isAvrcpAbsoluteVolumeSupported() {
        Log.d(TAG, "isAvrcpAbsoluteVolumeSupported");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.isAvrcpAbsoluteVolumeSupported();
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in isAvrcpAbsoluteVolumeSupported()", e);
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
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
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in setAvrcpAbsoluteVolume()", e);
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            throw th;
        }
        this.mServiceLock.readLock().unlock();
    }

    public boolean isA2dpPlaying(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                return this.mService.isA2dpPlaying(device);
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

    public BluetoothCodecStatus getCodecStatus(BluetoothDevice device) {
        Log.d(TAG, "getCodecStatus(" + device + ")");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.getCodecStatus(device);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in getCodecStatus()", e);
            return null;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isSupportPlayLDAC(String deviceMac) {
        Log.d(TAG, "isSupportPlayLDAC");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.isSupportPlayLDAC(deviceMac);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in isSupportPlayLDAC()", e);
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isSupportHighQuality(BluetoothDevice device) {
        Log.d(TAG, "isSupportHighQuality");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.isSupportHighQuality(device);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in isSupportHighQuality()", e);
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getLdacQualityValue() {
        Log.d(TAG, "getLdacQualityValue");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.getLdacQualityValue();
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return 1001;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in getLdacQualityValue()", e);
            return 1001;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean getHighQualityDefaultConfigValue(BluetoothDevice device) {
        Log.d(TAG, "getHighQualityDefaultConfigValue");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.getHighQualityDefaultConfigValue(device);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in getHighQualityDefaultConfigValue()", e);
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean setHighQualityDefaultConfigValue(BluetoothDevice device, boolean enable) {
        Log.d(TAG, "setHighQualityDefaultConfigValue");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                return this.mService.setHighQualityDefaultConfigValue(device, enable);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in setHighQualityDefaultConfigValue()", e);
            return false;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public void setCodecConfigPreference(BluetoothDevice device, BluetoothCodecConfig codecConfig) {
        Log.d(TAG, "setCodecConfigPreference(" + device + ")");
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                this.mService.setCodecConfigPreference(device, codecConfig);
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in setCodecConfigPreference()", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public void enableOptionalCodecs(BluetoothDevice device) {
        Log.d(TAG, "enableOptionalCodecs(" + device + ")");
        enableDisableOptionalCodecs(device, true);
    }

    public void disableOptionalCodecs(BluetoothDevice device) {
        Log.d(TAG, "disableOptionalCodecs(" + device + ")");
        enableDisableOptionalCodecs(device, false);
    }

    private void enableDisableOptionalCodecs(BluetoothDevice device, boolean enable) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled()) {
                if (enable) {
                    this.mService.enableOptionalCodecs(device);
                } else {
                    this.mService.disableOptionalCodecs(device);
                }
            }
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in enableDisableOptionalCodecs()", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int supportsOptionalCodecs(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                return this.mService.supportsOptionalCodecs(device);
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
        }
    }

    public int getOptionalCodecsEnabled(BluetoothDevice device) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null && isEnabled() && isValidDevice(device)) {
                return this.mService.getOptionalCodecsEnabled(device);
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
