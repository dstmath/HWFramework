package android.bluetooth;

import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothHidDevice;
import android.bluetooth.IBluetoothHidDeviceCallback;
import android.content.Context;
import android.os.Binder;
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
    private static final String TAG = BluetoothHidDevice.class.getSimpleName();
    private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BluetoothProfileConnector<IBluetoothHidDevice> mProfileConnector = new BluetoothProfileConnector(this, 19, "BluetoothHidDevice", IBluetoothHidDevice.class.getName()) {
        /* class android.bluetooth.BluetoothHidDevice.AnonymousClass1 */

        @Override // android.bluetooth.BluetoothProfileConnector
        public IBluetoothHidDevice getServiceInterface(IBinder service) {
            return IBluetoothHidDevice.Stub.asInterface(Binder.allowBlocking(service));
        }
    };

    public static abstract class Callback {
        private static final String TAG = "BluetoothHidDevCallback";

        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            Log.d(TAG, "onAppStatusChanged: pluggedDevice=" + getPartAddress(pluggedDevice) + " registered=" + registered);
        }

        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            Log.d(TAG, "onConnectionStateChanged: device=" + getPartAddress(device) + " state=" + state);
        }

        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            Log.d(TAG, "onGetReport: device=" + getPartAddress(device) + " type=" + ((int) type) + " id=" + ((int) id) + " bufferSize=" + bufferSize);
        }

        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            Log.d(TAG, "onSetReport: device=" + getPartAddress(device) + " type=" + ((int) type) + " id=" + ((int) id));
        }

        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            Log.d(TAG, "onSetProtocol: device=" + getPartAddress(device) + " protocol=" + ((int) protocol));
        }

        public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) {
            Log.d(TAG, "onInterruptData: device=" + getPartAddress(device) + " reportId=" + ((int) reportId));
        }

        public void onVirtualCableUnplug(BluetoothDevice device) {
            Log.d(TAG, "onVirtualCableUnplug: device=" + getPartAddress(device));
        }

        private static String getPartAddress(BluetoothDevice device) {
            String address;
            if (device == null || (address = device.getAddress()) == null) {
                return "";
            }
            return address.substring(0, address.length() / 2) + ":**:**:**";
        }
    }

    /* access modifiers changed from: private */
    public static class CallbackWrapper extends IBluetoothHidDeviceCallback.Stub {
        private final Callback mCallback;
        private final Executor mExecutor;

        CallbackWrapper(Executor executor, Callback callback) {
            this.mExecutor = executor;
            this.mCallback = callback;
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(pluggedDevice, registered) {
                /* class android.bluetooth.$$Lambda$BluetoothHidDevice$CallbackWrapper$NFluHjT4zTfYBRXClu_2k6mPKFI */
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.lambda$onAppStatusChanged$0$BluetoothHidDevice$CallbackWrapper(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onAppStatusChanged$0$BluetoothHidDevice$CallbackWrapper(BluetoothDevice pluggedDevice, boolean registered) {
            this.mCallback.onAppStatusChanged(pluggedDevice, registered);
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device, state) {
                /* class android.bluetooth.$$Lambda$BluetoothHidDevice$CallbackWrapper$qtStwQVkGfOs2iJIiePWqJJpi0w */
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.lambda$onConnectionStateChanged$1$BluetoothHidDevice$CallbackWrapper(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onConnectionStateChanged$1$BluetoothHidDevice$CallbackWrapper(BluetoothDevice device, int state) {
            this.mCallback.onConnectionStateChanged(device, state);
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device, type, id, bufferSize) {
                /* class android.bluetooth.$$Lambda$BluetoothHidDevice$CallbackWrapper$Eyz_qG6mvTlh6a8Bp41ZoEJzQCQ */
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

                @Override // java.lang.Runnable
                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.lambda$onGetReport$2$BluetoothHidDevice$CallbackWrapper(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        public /* synthetic */ void lambda$onGetReport$2$BluetoothHidDevice$CallbackWrapper(BluetoothDevice device, byte type, byte id, int bufferSize) {
            this.mCallback.onGetReport(device, type, id, bufferSize);
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device, type, id, data) {
                /* class android.bluetooth.$$Lambda$BluetoothHidDevice$CallbackWrapper$3bTGVlfKj7Y0SZdifW_Ya2myDKs */
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

                @Override // java.lang.Runnable
                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.lambda$onSetReport$3$BluetoothHidDevice$CallbackWrapper(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        public /* synthetic */ void lambda$onSetReport$3$BluetoothHidDevice$CallbackWrapper(BluetoothDevice device, byte type, byte id, byte[] data) {
            this.mCallback.onSetReport(device, type, id, data);
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device, protocol) {
                /* class android.bluetooth.$$Lambda$BluetoothHidDevice$CallbackWrapper$ypkr5GGxsAkGSBiLjIRwgPzqCM */
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ byte f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.lambda$onSetProtocol$4$BluetoothHidDevice$CallbackWrapper(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onSetProtocol$4$BluetoothHidDevice$CallbackWrapper(BluetoothDevice device, byte protocol) {
            this.mCallback.onSetProtocol(device, protocol);
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device, reportId, data) {
                /* class android.bluetooth.$$Lambda$BluetoothHidDevice$CallbackWrapper$xW99tc95OmGApoKnpQ9q1TXb9k */
                private final /* synthetic */ BluetoothDevice f$1;
                private final /* synthetic */ byte f$2;
                private final /* synthetic */ byte[] f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.lambda$onInterruptData$5$BluetoothHidDevice$CallbackWrapper(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public /* synthetic */ void lambda$onInterruptData$5$BluetoothHidDevice$CallbackWrapper(BluetoothDevice device, byte reportId, byte[] data) {
            this.mCallback.onInterruptData(device, reportId, data);
        }

        @Override // android.bluetooth.IBluetoothHidDeviceCallback
        public void onVirtualCableUnplug(BluetoothDevice device) {
            clearCallingIdentity();
            this.mExecutor.execute(new Runnable(device) {
                /* class android.bluetooth.$$Lambda$BluetoothHidDevice$CallbackWrapper$jiodzbAJAcleQCwlDcBjvDddELM */
                private final /* synthetic */ BluetoothDevice f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BluetoothHidDevice.CallbackWrapper.this.lambda$onVirtualCableUnplug$6$BluetoothHidDevice$CallbackWrapper(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onVirtualCableUnplug$6$BluetoothHidDevice$CallbackWrapper(BluetoothDevice device) {
            this.mCallback.onVirtualCableUnplug(device);
        }
    }

    BluetoothHidDevice(Context context, BluetoothProfile.ServiceListener listener) {
        this.mProfileConnector.connect(context, listener);
    }

    /* access modifiers changed from: package-private */
    public void close() {
        this.mProfileConnector.disconnect();
    }

    private IBluetoothHidDevice getService() {
        return this.mProfileConnector.getService();
    }

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getConnectedDevices() {
        IBluetoothHidDevice service = getService();
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

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        IBluetoothHidDevice service = getService();
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

    @Override // android.bluetooth.BluetoothProfile
    public int getConnectionState(BluetoothDevice device) {
        IBluetoothHidDevice service = getService();
        if (service != null) {
            try {
                return service.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return 0;
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
            IBluetoothHidDevice service = getService();
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
        IBluetoothHidDevice service = getService();
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
        IBluetoothHidDevice service = getService();
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
        IBluetoothHidDevice service = getService();
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
        IBluetoothHidDevice service = getService();
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
        IBluetoothHidDevice service = getService();
        if (service != null) {
            try {
                return service.getUserAppName();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
                return "";
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            return "";
        }
    }

    public boolean connect(BluetoothDevice device) {
        IBluetoothHidDevice service = getService();
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
        IBluetoothHidDevice service = getService();
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
