package android.bluetooth;

import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothHidHost;
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

public final class BluetoothHidHost implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_HANDSHAKE = "android.bluetooth.input.profile.action.HANDSHAKE";
    public static final String ACTION_IDLE_TIME_CHANGED = "android.bluetooth.input.profile.action.IDLE_TIME_CHANGED";
    public static final String ACTION_PROTOCOL_MODE_CHANGED = "android.bluetooth.input.profile.action.PROTOCOL_MODE_CHANGED";
    public static final String ACTION_REPORT = "android.bluetooth.input.profile.action.REPORT";
    public static final String ACTION_VIRTUAL_UNPLUG_STATUS = "android.bluetooth.input.profile.action.VIRTUAL_UNPLUG_STATUS";
    private static final boolean DBG = true;
    public static final String EXTRA_IDLE_TIME = "android.bluetooth.BluetoothHidHost.extra.IDLE_TIME";
    public static final String EXTRA_PROTOCOL_MODE = "android.bluetooth.BluetoothHidHost.extra.PROTOCOL_MODE";
    public static final String EXTRA_REPORT = "android.bluetooth.BluetoothHidHost.extra.REPORT";
    public static final String EXTRA_REPORT_BUFFER_SIZE = "android.bluetooth.BluetoothHidHost.extra.REPORT_BUFFER_SIZE";
    public static final String EXTRA_REPORT_ID = "android.bluetooth.BluetoothHidHost.extra.REPORT_ID";
    public static final String EXTRA_REPORT_TYPE = "android.bluetooth.BluetoothHidHost.extra.REPORT_TYPE";
    public static final String EXTRA_STATUS = "android.bluetooth.BluetoothHidHost.extra.STATUS";
    public static final String EXTRA_VIRTUAL_UNPLUG_STATUS = "android.bluetooth.BluetoothHidHost.extra.VIRTUAL_UNPLUG_STATUS";
    public static final int INPUT_CONNECT_FAILED_ALREADY_CONNECTED = 5001;
    public static final int INPUT_CONNECT_FAILED_ATTEMPT_FAILED = 5002;
    public static final int INPUT_DISCONNECT_FAILED_NOT_CONNECTED = 5000;
    public static final int INPUT_OPERATION_GENERIC_FAILURE = 5003;
    public static final int INPUT_OPERATION_SUCCESS = 5004;
    public static final int PROTOCOL_BOOT_MODE = 1;
    public static final int PROTOCOL_REPORT_MODE = 0;
    public static final int PROTOCOL_UNSUPPORTED_MODE = 255;
    public static final byte REPORT_TYPE_FEATURE = 3;
    public static final byte REPORT_TYPE_INPUT = 1;
    public static final byte REPORT_TYPE_OUTPUT = 2;
    private static final String TAG = "BluetoothHidHost";
    private static final boolean VDBG = false;
    public static final int VIRTUAL_UNPLUG_STATUS_FAIL = 1;
    public static final int VIRTUAL_UNPLUG_STATUS_SUCCESS = 0;
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
        public void onBluetoothStateChange(boolean up) {
            Log.d(BluetoothHidHost.TAG, "onBluetoothStateChange: up=" + up);
            if (!up) {
                synchronized (BluetoothHidHost.this.mConnection) {
                    try {
                        IBluetoothHidHost unused = BluetoothHidHost.this.mService = null;
                        BluetoothHidHost.this.mContext.unbindService(BluetoothHidHost.this.mConnection);
                    } catch (Exception re) {
                        Log.e(BluetoothHidHost.TAG, "", re);
                    }
                }
                return;
            }
            synchronized (BluetoothHidHost.this.mConnection) {
                try {
                    if (BluetoothHidHost.this.mService == null) {
                        BluetoothHidHost.this.doBind();
                    }
                } catch (Exception re2) {
                    Log.e(BluetoothHidHost.TAG, "", re2);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(BluetoothHidHost.TAG, "Proxy object connected");
            IBluetoothHidHost unused = BluetoothHidHost.this.mService = IBluetoothHidHost.Stub.asInterface(Binder.allowBlocking(service));
            if (BluetoothHidHost.this.mServiceListener != null) {
                BluetoothHidHost.this.mServiceListener.onServiceConnected(4, BluetoothHidHost.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(BluetoothHidHost.TAG, "Proxy object disconnected");
            synchronized (BluetoothHidHost.this.mConnection) {
                if (BluetoothHidHost.this.mService != null) {
                    try {
                        IBluetoothHidHost unused = BluetoothHidHost.this.mService = null;
                        BluetoothHidHost.this.mContext.unbindService(BluetoothHidHost.this.mConnection);
                    } catch (Exception re) {
                        Log.e(BluetoothHidHost.TAG, "", re);
                    }
                }
            }
            if (BluetoothHidHost.this.mServiceListener != null) {
                BluetoothHidHost.this.mServiceListener.onServiceDisconnected(4);
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public volatile IBluetoothHidHost mService;
    /* access modifiers changed from: private */
    public BluetoothProfile.ServiceListener mServiceListener;

    BluetoothHidHost(Context context, BluetoothProfile.ServiceListener l) {
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
        Intent intent = new Intent(IBluetoothHidHost.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, this.mConnection, 0, this.mContext.getUser())) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth HID Service with " + intent);
        return false;
    }

    /* access modifiers changed from: package-private */
    public void close() {
        infolog("close()");
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

    public boolean connect(BluetoothDevice device) {
        log("connect(" + device + ")");
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.connect(device);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean disconnect(BluetoothDevice device) {
        log("disconnect(" + device + ")");
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.disconnect(device);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public List<BluetoothDevice> getConnectedDevices() {
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled()) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            if (this.mService.getConnectedDevices() == null) {
                return new ArrayList();
            }
            return service.getConnectedDevices();
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled()) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return service.getDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return 0;
        }
        try {
            return service.getConnectionState(device);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return 0;
        }
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        log("setPriority(" + device + ", " + priority + ")");
        IBluetoothHidHost service = this.mService;
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
                Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
    }

    public int getPriority(BluetoothDevice device) {
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return 0;
        }
        try {
            return service.getPriority(device);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return 0;
        }
    }

    private boolean isEnabled() {
        return this.mAdapter.getState() == 12;
    }

    private static boolean isValidDevice(BluetoothDevice device) {
        return device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress());
    }

    public boolean virtualUnplug(BluetoothDevice device) {
        log("virtualUnplug(" + device + ")");
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.virtualUnplug(device);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean getProtocolMode(BluetoothDevice device) {
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.getProtocolMode(device);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean setProtocolMode(BluetoothDevice device, int protocolMode) {
        log("setProtocolMode(" + device + ")");
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.setProtocolMode(device, protocolMode);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean getReport(BluetoothDevice device, byte reportType, byte reportId, int bufferSize) {
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.getReport(device, reportType, reportId, bufferSize);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean setReport(BluetoothDevice device, byte reportType, String report) {
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.setReport(device, reportType, report);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean sendData(BluetoothDevice device, String report) {
        log("sendData(" + device + "), report=" + report);
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.sendData(device, report);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean getIdleTime(BluetoothDevice device) {
        log("getIdletime(" + device + ")");
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.getIdleTime(device);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public boolean setIdleTime(BluetoothDevice device, byte idleTime) {
        log("setIdletime(" + device + "), idleTime=" + idleTime);
        IBluetoothHidHost service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.setIdleTime(device, idleTime);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    private static void infolog(String msg) {
        Log.i(TAG, msg);
    }
}
