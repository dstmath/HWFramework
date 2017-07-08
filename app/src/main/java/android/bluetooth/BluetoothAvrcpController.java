package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothStateChangeCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothAvrcpController implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.avrcp-controller.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_PLAYER_SETTING = "android.bluetooth.avrcp-controller.profile.action.PLAYER_SETTING";
    public static final String ACTION_TRACK_EVENT = "android.bluetooth.avrcp-controller.profile.action.TRACK_EVENT";
    private static final boolean DBG = false;
    public static final String EXTRA_METADATA = "android.bluetooth.avrcp-controller.profile.extra.METADATA";
    public static final String EXTRA_PLAYBACK = "android.bluetooth.avrcp-controller.profile.extra.PLAYBACK";
    public static final String EXTRA_PLAYER_SETTING = "android.bluetooth.avrcp-controller.profile.extra.PLAYER_SETTING";
    public static final int KEY_STATE_PRESSED = 0;
    public static final int KEY_STATE_RELEASED = 1;
    public static final int PASS_THRU_CMD_ID_BACKWARD = 76;
    public static final int PASS_THRU_CMD_ID_FF = 73;
    public static final int PASS_THRU_CMD_ID_FORWARD = 75;
    public static final int PASS_THRU_CMD_ID_NEXT_GRP = 0;
    public static final int PASS_THRU_CMD_ID_PAUSE = 70;
    public static final int PASS_THRU_CMD_ID_PLAY = 68;
    public static final int PASS_THRU_CMD_ID_PREV_GRP = 1;
    public static final int PASS_THRU_CMD_ID_REWIND = 72;
    public static final int PASS_THRU_CMD_ID_STOP = 69;
    public static final int PASS_THRU_CMD_ID_VOL_DOWN = 66;
    public static final int PASS_THRU_CMD_ID_VOL_UP = 65;
    private static final String TAG = "BluetoothAvrcpController";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback;
    private final ServiceConnection mConnection;
    private Context mContext;
    private IBluetoothAvrcpController mService;
    private ServiceListener mServiceListener;

    BluetoothAvrcpController(Context context, ServiceListener l) {
        this.mBluetoothStateChangeCallback = new Stub() {
            public void onBluetoothStateChange(boolean up) {
                ServiceConnection -get0;
                if (up) {
                    -get0 = BluetoothAvrcpController.this.mConnection;
                    synchronized (-get0) {
                        try {
                        } catch (Exception re) {
                            Log.e(BluetoothAvrcpController.TAG, ProxyInfo.LOCAL_EXCL_LIST, re);
                        }
                    }
                    if (BluetoothAvrcpController.this.mService == null) {
                        BluetoothAvrcpController.this.doBind();
                    }
                } else {
                    -get0 = BluetoothAvrcpController.this.mConnection;
                    synchronized (-get0) {
                        try {
                        } catch (Exception re2) {
                            Log.e(BluetoothAvrcpController.TAG, ProxyInfo.LOCAL_EXCL_LIST, re2);
                        }
                    }
                    BluetoothAvrcpController.this.mService = null;
                    BluetoothAvrcpController.this.mContext.unbindService(BluetoothAvrcpController.this.mConnection);
                }
            }
        };
        this.mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                BluetoothAvrcpController.this.mService = IBluetoothAvrcpController.Stub.asInterface(service);
                if (BluetoothAvrcpController.this.mServiceListener != null) {
                    BluetoothAvrcpController.this.mServiceListener.onServiceConnected(12, BluetoothAvrcpController.this);
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                BluetoothAvrcpController.this.mService = null;
                if (BluetoothAvrcpController.this.mServiceListener != null) {
                    BluetoothAvrcpController.this.mServiceListener.onServiceDisconnected(12);
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

    boolean doBind() {
        Intent intent = new Intent(IBluetoothAvrcpController.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), PASS_THRU_CMD_ID_NEXT_GRP);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, this.mConnection, PASS_THRU_CMD_ID_NEXT_GRP, Process.myUserHandle())) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth AVRCP Controller Service with " + intent);
        return DBG;
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
    }

    public void finalize() {
        close();
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
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
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
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
                return PASS_THRU_CMD_ID_NEXT_GRP;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return PASS_THRU_CMD_ID_NEXT_GRP;
    }

    public void sendPassThroughCmd(BluetoothDevice device, int keyCode, int keyState) {
        if (this.mService == null || !isEnabled()) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return;
        }
        try {
            this.mService.sendPassThroughCmd(device, keyCode, keyState);
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in sendPassThroughCmd()", e);
        }
    }

    public BluetoothAvrcpPlayerSettings getPlayerSettings(BluetoothDevice device) {
        BluetoothAvrcpPlayerSettings settings = null;
        if (this.mService != null && isEnabled()) {
            try {
                settings = this.mService.getPlayerSettings(device);
            } catch (RemoteException e) {
                Log.e(TAG, "Error talking to BT service in getMetadata() " + e);
                return null;
            }
        }
        return settings;
    }

    public MediaMetadata getMetadata(BluetoothDevice device) {
        MediaMetadata metadata = null;
        if (this.mService != null && isEnabled()) {
            try {
                metadata = this.mService.getMetadata(device);
            } catch (RemoteException e) {
                Log.e(TAG, "Error talking to BT service in getMetadata() " + e);
                return null;
            }
        }
        return metadata;
    }

    public PlaybackState getPlaybackState(BluetoothDevice device) {
        PlaybackState playbackState = null;
        if (this.mService != null && isEnabled()) {
            try {
                playbackState = this.mService.getPlaybackState(device);
            } catch (RemoteException e) {
                Log.e(TAG, "Error talking to BT service in getPlaybackState() " + e);
                return null;
            }
        }
        return playbackState;
    }

    public boolean setPlayerApplicationSetting(BluetoothAvrcpPlayerSettings plAppSetting) {
        if (this.mService == null || !isEnabled()) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return DBG;
        }
        try {
            return this.mService.setPlayerApplicationSetting(plAppSetting);
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in setPlayerApplicationSetting() " + e);
            return DBG;
        }
    }

    public void sendGroupNavigationCmd(BluetoothDevice device, int keyCode, int keyState) {
        Log.d(TAG, "sendGroupNavigationCmd dev = " + device + " key " + keyCode + " State = " + keyState);
        if (this.mService == null || !isEnabled()) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return;
        }
        try {
            this.mService.sendGroupNavigationCmd(device, keyCode, keyState);
        } catch (RemoteException e) {
            Log.e(TAG, "Error talking to BT service in sendGroupNavigationCmd()", e);
        }
    }

    private boolean isEnabled() {
        if (this.mAdapter.getState() == 12) {
            return true;
        }
        return DBG;
    }

    private boolean isValidDevice(BluetoothDevice device) {
        if (device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            return true;
        }
        return DBG;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
