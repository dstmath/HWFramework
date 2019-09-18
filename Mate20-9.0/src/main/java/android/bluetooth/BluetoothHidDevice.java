package android.bluetooth;

import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothHidDevice;
import android.bluetooth.IBluetoothHidDeviceCallback;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public final class BluetoothHidDevice implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.hiddevice.profile.action.CONNECTION_STATE_CHANGED";
    public static final byte ERROR_RSP_INVALID_PARAM = 4;
    public static final byte ERROR_RSP_INVALID_RPT_ID = 2;
    public static final byte ERROR_RSP_NOT_READY = 1;
    public static final byte ERROR_RSP_SUCCESS = 0;
    public static final byte ERROR_RSP_UNKNOWN = 14;
    public static final byte ERROR_RSP_UNSUPPORTED_REQ = 3;
    public static final byte PROTOCOL_BOOT_MODE = 0;
    public static final byte PROTOCOL_REPORT_MODE = 1;
    public static final byte REPORT_TYPE_FEATURE = 3;
    public static final byte REPORT_TYPE_INPUT = 1;
    public static final byte REPORT_TYPE_OUTPUT = 2;
    public static final byte SUBCLASS1_COMBO = -64;
    public static final byte SUBCLASS1_KEYBOARD = 64;
    public static final byte SUBCLASS1_MOUSE = Byte.MIN_VALUE;
    public static final byte SUBCLASS1_NONE = 0;
    public static final byte SUBCLASS2_CARD_READER = 6;
    public static final byte SUBCLASS2_DIGITIZER_TABLET = 5;
    public static final byte SUBCLASS2_GAMEPAD = 2;
    public static final byte SUBCLASS2_JOYSTICK = 1;
    public static final byte SUBCLASS2_REMOTE_CONTROL = 3;
    public static final byte SUBCLASS2_SENSING_DEVICE = 4;
    public static final byte SUBCLASS2_UNCATEGORIZED = 0;
    /* access modifiers changed from: private */
    public static final String TAG = BluetoothHidDevice.class.getSimpleName();
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        public void onBluetoothStateChange(boolean up) {
            String access$000 = BluetoothHidDevice.TAG;
            Log.d(access$000, "onBluetoothStateChange: up=" + up);
            synchronized (BluetoothHidDevice.this.mConnection) {
                if (up) {
                    try {
                        if (BluetoothHidDevice.this.mService == null) {
                            Log.d(BluetoothHidDevice.TAG, "Binding HID Device service...");
                            BluetoothHidDevice.this.doBind();
                        }
                    } catch (IllegalStateException e) {
                        Log.e(BluetoothHidDevice.TAG, "onBluetoothStateChange: could not bind to HID Dev service: ", e);
                    } catch (SecurityException e2) {
                        Log.e(BluetoothHidDevice.TAG, "onBluetoothStateChange: could not bind to HID Dev service: ", e2);
                    }
                } else {
                    Log.d(BluetoothHidDevice.TAG, "Unbinding service...");
                    BluetoothHidDevice.this.doUnbind();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(BluetoothHidDevice.TAG, "onServiceConnected()");
            IBluetoothHidDevice unused = BluetoothHidDevice.this.mService = IBluetoothHidDevice.Stub.asInterface(service);
            if (BluetoothHidDevice.this.mServiceListener != null) {
                BluetoothHidDevice.this.mServiceListener.onServiceConnected(19, BluetoothHidDevice.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(BluetoothHidDevice.TAG, "onServiceDisconnected()");
            synchronized (BluetoothHidDevice.this.mConnection) {
                if (BluetoothHidDevice.this.mService != null) {
                    try {
                        IBluetoothHidDevice unused = BluetoothHidDevice.this.mService = null;
                        BluetoothHidDevice.this.mContext.unbindService(BluetoothHidDevice.this.mConnection);
                    } catch (Exception re) {
                        Log.e(BluetoothHidDevice.TAG, "", re);
                    }
                }
            }
            if (BluetoothHidDevice.this.mServiceListener != null) {
                BluetoothHidDevice.this.mServiceListener.onServiceDisconnected(19);
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public volatile IBluetoothHidDevice mService;
    /* access modifiers changed from: private */
    public BluetoothProfile.ServiceListener mServiceListener;

    public static abstract class Callback {
        private static final String TAG = "BluetoothHidDevCallback";

        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            Log.d(TAG, "onAppStatusChanged: pluggedDevice=" + pluggedDevice + " registered=" + registered);
        }

        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            Log.d(TAG, "onConnectionStateChanged: device=" + device + " state=" + state);
        }

        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            Log.d(TAG, "onGetReport: device=" + device + " type=" + type + " id=" + id + " bufferSize=" + bufferSize);
        }

        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            Log.d(TAG, "onSetReport: device=" + device + " type=" + type + " id=" + id);
        }

        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            Log.d(TAG, "onSetProtocol: device=" + device + " protocol=" + protocol);
        }

        public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) {
            Log.d(TAG, "onInterruptData: device=" + device + " reportId=" + reportId);
        }

        public void onVirtualCableUnplug(BluetoothDevice device) {
            Log.d(TAG, "onVirtualCableUnplug: device=" + device);
        }
    }

    private static class CallbackWrapper extends IBluetoothHidDeviceCallback.Stub {
        private final Callback mCallback;
        private final Executor mExecutor;

        CallbackWrapper(Executor executor, Callback callback) {
            this.mExecutor = executor;
            this.mCallback = callback;
        }

        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(pluggedDevice, registered) {
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.mCallback.onAppStatusChanged(this.f$1, this.f$2);
                }
            });
        }

        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device, state) {
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.mCallback.onConnectionStateChanged(this.f$1, this.f$2);
                }
            });
        }

        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            clearCallingIdentity();
            Executor executor = this.mExecutor;
            $$Lambda$BluetoothHidDevice$CallbackWrapper$Eyz_qG6mvTlh6a8Bp41ZoEJzQCQ r1 = new Runnable(device, type, id, bufferSize) {
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ byte f$2;
                private final /* synthetic */ byte f$3;
                private final /* synthetic */ int f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.mCallback.onGetReport(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            };
            executor.execute(r1);
        }

        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            clearCallingIdentity();
            Executor executor = this.mExecutor;
            $$Lambda$BluetoothHidDevice$CallbackWrapper$3bTGVlfKj7Y0SZdifW_Ya2myDKs r1 = new Runnable(device, type, id, data) {
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ byte f$2;
                private final /* synthetic */ byte f$3;
                private final /* synthetic */ byte[] f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.mCallback.onSetReport(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            };
            executor.execute(r1);
        }

        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device, protocol) {
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ byte f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.mCallback.onSetProtocol(this.f$1, this.f$2);
                }
            });
        }

        public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device, reportId, data) {
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ byte f$2;
                private final /* synthetic */ byte[] f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.mCallback.onInterruptData(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public void onVirtualCableUnplug(BluetoothDevice device) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device) {
                private final /* synthetic */ BluetoothDevice f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.mCallback.onVirtualCableUnplug(this.f$1);
                }
            });
        }
    }

    BluetoothHidDevice(Context context, BluetoothProfile.ServiceListener listener) {
        this.mContext = context;
        this.mServiceListener = listener;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "BluetoothInputHost failed to unregisterStateChangeCallback: " + e.getMessage());
            }
        }
        doBind();
    }

    /* access modifiers changed from: package-private */
    public boolean doBind() {
        Intent intent = new Intent(IBluetoothHidDevice.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp == null || !this.mContext.bindServiceAsUser(intent, this.mConnection, 0, this.mContext.getUser())) {
            String str = TAG;
            Log.e(str, "Could not bind to Bluetooth HID Device Service with " + intent);
            return false;
        }
        Log.d(TAG, "Bound to HID Device Service");
        return true;
    }

    /* access modifiers changed from: package-private */
    public void doUnbind() {
        if (this.mService != null) {
            this.mService = null;
            try {
                this.mContext.unbindService(this.mConnection);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Unable to unbind HidDevService", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void close() {
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "Failed to unregisterStateChangeCallback: " + e.getMessage());
            }
        }
        synchronized (this.mConnection) {
            doUnbind();
        }
        this.mServiceListener = null;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.getConnectedDevices();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return new ArrayList();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.getDevicesMatchingConnectionStates(states);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return new ArrayList();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return 0;
        }
    }

    public boolean registerApp(BluetoothHidDeviceAppSdpSettings sdp, BluetoothHidDeviceAppQosSettings inQos, BluetoothHidDeviceAppQosSettings outQos, Executor executor, Callback callback) {
        if (sdp == null) {
            throw new IllegalArgumentException("sdp parameter cannot be null");
        } else if (executor == null) {
            throw new IllegalArgumentException("executor parameter cannot be null");
        } else if (callback != null) {
            IBluetoothHidDevice service = this.mService;
            if (service != null) {
                try {
                    return service.registerApp(sdp, inQos, outQos, new CallbackWrapper(executor, callback));
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                    return false;
                }
            } else {
                Log.w(TAG, "Proxy not attached to service");
                return false;
            }
        } else {
            throw new IllegalArgumentException("callback parameter cannot be null");
        }
    }

    public boolean unregisterApp() {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.unregisterApp();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
    }

    public boolean sendReport(BluetoothDevice device, int id, byte[] data) {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.sendReport(device, id, data);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
    }

    public boolean replyReport(BluetoothDevice device, byte type, byte id, byte[] data) {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.replyReport(device, type, id, data);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
    }

    public boolean reportError(BluetoothDevice device, byte error) {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.reportError(device, error);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
    }

    public String getUserAppName() {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.getUserAppName();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return "";
        }
    }

    public boolean connect(BluetoothDevice device) {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.connect(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
    }

    public boolean disconnect(BluetoothDevice device) {
        IBluetoothHidDevice service = this.mService;
        if (service != null) {
            try {
                return service.disconnect(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
    }
}
