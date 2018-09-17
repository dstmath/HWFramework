package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothHealthCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothHealth implements BluetoothProfile {
    public static final int APP_CONFIG_REGISTRATION_FAILURE = 1;
    public static final int APP_CONFIG_REGISTRATION_SUCCESS = 0;
    public static final int APP_CONFIG_UNREGISTRATION_FAILURE = 3;
    public static final int APP_CONFIG_UNREGISTRATION_SUCCESS = 2;
    public static final int CHANNEL_TYPE_ANY = 12;
    public static final int CHANNEL_TYPE_RELIABLE = 10;
    public static final int CHANNEL_TYPE_STREAMING = 11;
    private static final boolean DBG = true;
    public static final int HEALTH_OPERATION_ERROR = 6001;
    public static final int HEALTH_OPERATION_GENERIC_FAILURE = 6003;
    public static final int HEALTH_OPERATION_INVALID_ARGS = 6002;
    public static final int HEALTH_OPERATION_NOT_ALLOWED = 6005;
    public static final int HEALTH_OPERATION_NOT_FOUND = 6004;
    public static final int HEALTH_OPERATION_SUCCESS = 6000;
    public static final int SINK_ROLE = 2;
    public static final int SOURCE_ROLE = 1;
    public static final int STATE_CHANNEL_CONNECTED = 2;
    public static final int STATE_CHANNEL_CONNECTING = 1;
    public static final int STATE_CHANNEL_DISCONNECTED = 0;
    public static final int STATE_CHANNEL_DISCONNECTING = 3;
    private static final String TAG = "BluetoothHealth";
    private static final boolean VDBG = false;
    BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback;
    private final ServiceConnection mConnection;
    private Context mContext;
    private IBluetoothHealth mService;
    private ServiceListener mServiceListener;

    private static class BluetoothHealthCallbackWrapper extends Stub {
        private BluetoothHealthCallback mCallback;

        public BluetoothHealthCallbackWrapper(BluetoothHealthCallback callback) {
            this.mCallback = callback;
        }

        public void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration config, int status) {
            this.mCallback.onHealthAppConfigurationStatusChange(config, status);
        }

        public void onHealthChannelStateChange(BluetoothHealthAppConfiguration config, BluetoothDevice device, int prevState, int newState, ParcelFileDescriptor fd, int channelId) {
            this.mCallback.onHealthChannelStateChange(config, device, prevState, newState, fd, channelId);
        }
    }

    public boolean registerSinkAppConfiguration(String name, int dataType, BluetoothHealthCallback callback) {
        if (!isEnabled() || name == null) {
            return false;
        }
        return registerAppConfiguration(name, dataType, STATE_CHANNEL_CONNECTED, CHANNEL_TYPE_ANY, callback);
    }

    public boolean registerAppConfiguration(String name, int dataType, int role, int channelType, BluetoothHealthCallback callback) {
        boolean result = false;
        if (!isEnabled() || !checkAppParam(name, role, channelType, callback)) {
            return result;
        }
        log("registerApplication(" + name + ":" + dataType + ")");
        BluetoothHealthCallbackWrapper wrapper = new BluetoothHealthCallbackWrapper(callback);
        BluetoothHealthAppConfiguration config = new BluetoothHealthAppConfiguration(name, dataType, role, channelType);
        if (this.mService != null) {
            try {
                result = this.mService.registerAppConfiguration(config, wrapper);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return result;
    }

    public boolean unregisterAppConfiguration(BluetoothHealthAppConfiguration config) {
        boolean result = false;
        infolog("unregisterAppConfiguration");
        if (this.mService == null || !isEnabled() || config == null) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                result = this.mService.unregisterAppConfiguration(config);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return result;
    }

    public boolean connectChannelToSource(BluetoothDevice device, BluetoothHealthAppConfiguration config) {
        if (this.mService == null || !isEnabled() || !isValidDevice(device) || config == null) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                infolog("connectChannelToSource");
                return this.mService.connectChannelToSource(device, config);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public boolean connectChannelToSink(BluetoothDevice device, BluetoothHealthAppConfiguration config, int channelType) {
        if (this.mService == null || !isEnabled() || !isValidDevice(device) || config == null) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                infolog("connectChannelToSink");
                return this.mService.connectChannelToSink(device, config, channelType);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public boolean disconnectChannel(BluetoothDevice device, BluetoothHealthAppConfiguration config, int channelId) {
        if (this.mService == null || !isEnabled() || !isValidDevice(device) || config == null) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                infolog("disconnectChannel channelId = " + channelId);
                return this.mService.disconnectChannel(device, config, channelId);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public ParcelFileDescriptor getMainChannelFd(BluetoothDevice device, BluetoothHealthAppConfiguration config) {
        if (this.mService == null || !isEnabled() || !isValidDevice(device) || config == null) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return this.mService.getMainChannelFd(device, config);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return null;
    }

    public int getConnectionState(BluetoothDevice device) {
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getHealthDeviceConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        return STATE_CHANNEL_DISCONNECTED;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (this.mService == null || !isEnabled()) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return this.mService.getConnectedHealthDevices();
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
            return this.mService.getHealthDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    BluetoothHealth(Context context, ServiceListener l) {
        this.mBluetoothStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
            public void onBluetoothStateChange(boolean up) {
                Log.d(BluetoothHealth.TAG, "onBluetoothStateChange: up=" + up);
                ServiceConnection -get0;
                if (up) {
                    -get0 = BluetoothHealth.this.mConnection;
                    synchronized (-get0) {
                        try {
                        } catch (Exception re) {
                            Log.e(BluetoothHealth.TAG, ProxyInfo.LOCAL_EXCL_LIST, re);
                        }
                    }
                    if (BluetoothHealth.this.mService == null) {
                        BluetoothHealth.this.doBind();
                    }
                } else {
                    -get0 = BluetoothHealth.this.mConnection;
                    synchronized (-get0) {
                        try {
                        } catch (Exception re2) {
                            Log.e(BluetoothHealth.TAG, ProxyInfo.LOCAL_EXCL_LIST, re2);
                        }
                    }
                    BluetoothHealth.this.mService = null;
                    BluetoothHealth.this.mContext.unbindService(BluetoothHealth.this.mConnection);
                }
            }
        };
        this.mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.d(BluetoothHealth.TAG, "Proxy object connected");
                BluetoothHealth.this.mService = IBluetoothHealth.Stub.asInterface(service);
                if (BluetoothHealth.this.mServiceListener != null) {
                    BluetoothHealth.this.mServiceListener.onServiceConnected(BluetoothHealth.STATE_CHANNEL_DISCONNECTING, BluetoothHealth.this);
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                Log.d(BluetoothHealth.TAG, "Proxy object disconnected");
                BluetoothHealth.this.mService = null;
                if (BluetoothHealth.this.mServiceListener != null) {
                    BluetoothHealth.this.mServiceListener.onServiceDisconnected(BluetoothHealth.STATE_CHANNEL_DISCONNECTING);
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
        Intent intent = new Intent(IBluetoothHealth.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), STATE_CHANNEL_DISCONNECTED);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, this.mConnection, STATE_CHANNEL_DISCONNECTED, Process.myUserHandle())) {
            return DBG;
        }
        Log.e(TAG, "Could not bind to Bluetooth Health Service with " + intent);
        return false;
    }

    void close() {
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

    private boolean isEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.getState() == CHANNEL_TYPE_ANY) {
            return DBG;
        }
        log("Bluetooth is Not enabled");
        return false;
    }

    private boolean isValidDevice(BluetoothDevice device) {
        if (device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            return DBG;
        }
        return false;
    }

    private boolean checkAppParam(String name, int role, int channelType, BluetoothHealthCallback callback) {
        if (name == null || ((role != STATE_CHANNEL_CONNECTING && role != STATE_CHANNEL_CONNECTED) || ((channelType != CHANNEL_TYPE_RELIABLE && channelType != CHANNEL_TYPE_STREAMING && channelType != CHANNEL_TYPE_ANY) || callback == null))) {
            return false;
        }
        return (role == STATE_CHANNEL_CONNECTING && channelType == CHANNEL_TYPE_ANY) ? false : DBG;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    private static void infolog(String msg) {
        Log.i(TAG, msg);
    }
}
