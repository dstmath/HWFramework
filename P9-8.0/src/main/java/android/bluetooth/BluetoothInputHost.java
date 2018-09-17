package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothStateChangeCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BluetoothInputHost implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.inputhost.profile.action.CONNECTION_STATE_CHANGED";
    public static final byte ERROR_RSP_INVALID_PARAM = (byte) 4;
    public static final byte ERROR_RSP_INVALID_RPT_ID = (byte) 2;
    public static final byte ERROR_RSP_NOT_READY = (byte) 1;
    public static final byte ERROR_RSP_SUCCESS = (byte) 0;
    public static final byte ERROR_RSP_UNKNOWN = (byte) 14;
    public static final byte ERROR_RSP_UNSUPPORTED_REQ = (byte) 3;
    public static final byte PROTOCOL_BOOT_MODE = (byte) 0;
    public static final byte PROTOCOL_REPORT_MODE = (byte) 1;
    public static final byte REPORT_TYPE_FEATURE = (byte) 3;
    public static final byte REPORT_TYPE_INPUT = (byte) 1;
    public static final byte REPORT_TYPE_OUTPUT = (byte) 2;
    public static final byte SUBCLASS1_COMBO = (byte) -64;
    public static final byte SUBCLASS1_KEYBOARD = (byte) 64;
    public static final byte SUBCLASS1_MOUSE = Byte.MIN_VALUE;
    public static final byte SUBCLASS1_NONE = (byte) 0;
    public static final byte SUBCLASS2_CARD_READER = (byte) 6;
    public static final byte SUBCLASS2_DIGITIZER_TABLED = (byte) 5;
    public static final byte SUBCLASS2_GAMEPAD = (byte) 2;
    public static final byte SUBCLASS2_JOYSTICK = (byte) 1;
    public static final byte SUBCLASS2_REMOTE_CONTROL = (byte) 3;
    public static final byte SUBCLASS2_SENSING_DEVICE = (byte) 4;
    public static final byte SUBCLASS2_UNCATEGORIZED = (byte) 0;
    private static final String TAG = BluetoothInputHost.class.getSimpleName();
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new Stub() {
        public void onBluetoothStateChange(boolean up) {
            Log.d(BluetoothInputHost.TAG, "onBluetoothStateChange: up=" + up);
            synchronized (BluetoothInputHost.this.mConnection) {
                if (up) {
                    try {
                        if (BluetoothInputHost.this.mService == null) {
                            Log.d(BluetoothInputHost.TAG, "Binding HID Device service...");
                            BluetoothInputHost.this.doBind();
                        }
                    } catch (IllegalStateException e) {
                        Log.e(BluetoothInputHost.TAG, "onBluetoothStateChange: could not bind to HID Dev service: ", e);
                    } catch (SecurityException e2) {
                        Log.e(BluetoothInputHost.TAG, "onBluetoothStateChange: could not bind to HID Dev service: ", e2);
                    }
                } else {
                    Log.d(BluetoothInputHost.TAG, "Unbinding service...");
                    if (BluetoothInputHost.this.mService != null) {
                        BluetoothInputHost.this.mService = null;
                        try {
                            BluetoothInputHost.this.mContext.unbindService(BluetoothInputHost.this.mConnection);
                        } catch (IllegalArgumentException e3) {
                            Log.e(BluetoothInputHost.TAG, "onBluetoothStateChange: could not unbind service:", e3);
                        }
                    }
                }
            }
            return;
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(BluetoothInputHost.TAG, "onServiceConnected()");
            BluetoothInputHost.this.mService = IBluetoothInputHost.Stub.asInterface(service);
            if (BluetoothInputHost.this.mServiceListener != null) {
                BluetoothInputHost.this.mServiceListener.onServiceConnected(19, BluetoothInputHost.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(BluetoothInputHost.TAG, "onServiceDisconnected()");
            BluetoothInputHost.this.mService = null;
            if (BluetoothInputHost.this.mServiceListener != null) {
                BluetoothInputHost.this.mServiceListener.onServiceDisconnected(19);
            }
        }
    };
    private Context mContext;
    private IBluetoothInputHost mService;
    private ServiceListener mServiceListener;

    private static class BluetoothHidDeviceCallbackWrapper extends IBluetoothHidDeviceCallback.Stub {
        private BluetoothHidDeviceCallback mCallback;

        public BluetoothHidDeviceCallbackWrapper(BluetoothHidDeviceCallback callback) {
            this.mCallback = callback;
        }

        public void onAppStatusChanged(BluetoothDevice pluggedDevice, BluetoothHidDeviceAppConfiguration config, boolean registered) {
            this.mCallback.onAppStatusChanged(pluggedDevice, config, registered);
        }

        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            this.mCallback.onConnectionStateChanged(device, state);
        }

        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            this.mCallback.onGetReport(device, type, id, bufferSize);
        }

        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            this.mCallback.onSetReport(device, type, id, data);
        }

        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            this.mCallback.onSetProtocol(device, protocol);
        }

        public void onIntrData(BluetoothDevice device, byte reportId, byte[] data) {
            this.mCallback.onIntrData(device, reportId, data);
        }

        public void onVirtualCableUnplug(BluetoothDevice device) {
            this.mCallback.onVirtualCableUnplug(device);
        }
    }

    BluetoothInputHost(Context context, ServiceListener listener) {
        Log.v(TAG, "BluetoothInputHost");
        this.mContext = context;
        this.mServiceListener = listener;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "BluetoothInputHost failed to unregisterStateChangeCallback: " + e.getMessage());
            }
        }
        doBind();
    }

    boolean doBind() {
        Intent intent = new Intent(IBluetoothInputHost.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp == null || (this.mContext.bindServiceAsUser(intent, this.mConnection, 0, Process.myUserHandle()) ^ 1) != 0) {
            Log.e(TAG, "Could not bind to Bluetooth HID Device Service with " + intent);
            return false;
        }
        Log.d(TAG, "Bound to HID Device Service");
        return true;
    }

    void close() {
        Log.v(TAG, "close()");
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to unregisterStateChangeCallback: " + e.getMessage());
            }
        }
        synchronized (this.mConnection) {
            if (this.mService != null) {
                this.mService = null;
                try {
                    this.mContext.unbindService(this.mConnection);
                } catch (IllegalArgumentException e2) {
                    Log.e(TAG, "close: could not unbind HID Dev service: ", e2);
                }
            }
        }
        this.mServiceListener = null;
        return;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        Log.v(TAG, "getConnectedDevices()");
        if (this.mService != null) {
            try {
                return this.mService.getConnectedDevices();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return new ArrayList();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        Log.v(TAG, "getDevicesMatchingConnectionStates(): states=" + Arrays.toString(states));
        if (this.mService != null) {
            try {
                return this.mService.getDevicesMatchingConnectionStates(states);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return new ArrayList();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        Log.v(TAG, "getConnectionState(): device=" + device);
        if (this.mService != null) {
            try {
                return this.mService.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return 0;
        }
    }

    public boolean registerApp(BluetoothHidDeviceAppSdpSettings sdp, BluetoothHidDeviceAppQosSettings inQos, BluetoothHidDeviceAppQosSettings outQos, BluetoothHidDeviceCallback callback) {
        Log.v(TAG, "registerApp(): sdp=" + sdp + " inQos=" + inQos + " outQos=" + outQos + " callback=" + callback);
        boolean result = false;
        if (sdp == null || callback == null) {
            return false;
        }
        if (this.mService != null) {
            try {
                result = this.mService.registerApp(new BluetoothHidDeviceAppConfiguration(), sdp, inQos, outQos, new BluetoothHidDeviceCallbackWrapper(callback));
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
        }
        return result;
    }

    public boolean unregisterApp(BluetoothHidDeviceAppConfiguration config) {
        Log.v(TAG, "unregisterApp()");
        boolean result = false;
        if (this.mService != null) {
            try {
                return this.mService.unregisterApp(config);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return result;
            }
        }
        Log.w(TAG, "Proxy not attached to service");
        return result;
    }

    public boolean sendReport(BluetoothDevice device, int id, byte[] data) {
        boolean result = false;
        if (this.mService != null) {
            try {
                return this.mService.sendReport(device, id, data);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return result;
            }
        }
        Log.w(TAG, "Proxy not attached to service");
        return result;
    }

    public boolean replyReport(BluetoothDevice device, byte type, byte id, byte[] data) {
        Log.v(TAG, "replyReport(): device=" + device + " type=" + type + " id=" + id);
        boolean result = false;
        if (this.mService != null) {
            try {
                return this.mService.replyReport(device, type, id, data);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return result;
            }
        }
        Log.w(TAG, "Proxy not attached to service");
        return result;
    }

    public boolean reportError(BluetoothDevice device, byte error) {
        Log.v(TAG, "reportError(): device=" + device + " error=" + error);
        boolean result = false;
        if (this.mService != null) {
            try {
                return this.mService.reportError(device, error);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return result;
            }
        }
        Log.w(TAG, "Proxy not attached to service");
        return result;
    }

    public boolean unplug(BluetoothDevice device) {
        Log.v(TAG, "unplug(): device=" + device);
        boolean result = false;
        if (this.mService != null) {
            try {
                return this.mService.unplug(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return result;
            }
        }
        Log.w(TAG, "Proxy not attached to service");
        return result;
    }

    public boolean connect(BluetoothDevice device) {
        Log.v(TAG, "connect(): device=" + device);
        boolean result = false;
        if (this.mService != null) {
            try {
                return this.mService.connect(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return result;
            }
        }
        Log.w(TAG, "Proxy not attached to service");
        return result;
    }

    public boolean disconnect(BluetoothDevice device) {
        Log.v(TAG, "disconnect(): device=" + device);
        boolean result = false;
        if (this.mService != null) {
            try {
                return this.mService.disconnect(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return result;
            }
        }
        Log.w(TAG, "Proxy not attached to service");
        return result;
    }
}
